package crawling_queries;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import run.createQCLIRCorpus;

/**
 * This class manages the process of crawling all required data from nutritionfacts.org,
 * i.e. all query texts for the retrieval task. Types of Pages:
 * <p>
 * - blog articles <br>
 * - 'ask the dietitian' pages <br>
 * - 'ask the doctor' pages <br>
 * - video pages <br>
 * - topic pages
 * <p>
 * The sites are visited while the data are written in a dump file for each page type. 
 * These files are then concatenated in one final dump (nfdump.txt) where, as in the
 * particular dump files, each line corresponds to one query unit and contains these 
 * tab-separated entries:
 * <p>
 * - id <br>
 * - own link <br>
 * - title <br>
 * - main text <br>
 * - comments  <br>
 * - topics/tags  <br>
 * - description <br>
 * - doctor's note <br>
 * - article links <br>
 * - question links
 * - topic links
 * - video links
 * - document links
 * <p>
 * Empty entries are designated by "-".
 * 
 * If there is a ReadTimeOut or any other problem occurs during the crawling, just start is again. It will proceed from the last crawled
 * document by crawling it again and moving on with the next.
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */

public class NFCrawler {
	
	final static long DELAY_NANOSECONDS = 3000000000L; // 1 sec = 1.000.000.000 nanoseconds
	final int TIMEOUT = 6000; // milliseconds
	private static Integer Id = 0;
	private static Integer query_count = 0;

	/**
	 * Crawls a list of links that are of one type (e.g. article pages).
	 * For each link a QueryPageCrawler object is created that gets
	 * the data from the particular page and writes it in the dump file.
	 * 
	 * @param linkslist links to be visited
	 * @param startlink indicates where in the list to start in the case of interruption
	 */
	public static void crawlQueryPages(ArrayList<String> linkslist, String startlink){
		
		for (int i = linkslist.indexOf(startlink); i < linkslist.size(); i ++) {
			if(createQCLIRCorpus.isTestBool() == true && NFCrawler.getQuery_count() < createQCLIRCorpus.getTestMax() || createQCLIRCorpus.isTestBool() == false){
				long start_time = System.nanoTime();    
				String link = linkslist.get(i);
				// crawler factory decides which crawler is needed based on the link
				CrawlerFactory cf = new CrawlerFactory();
				QueryPageCrawler crawler = cf.createCrawler(link);
				System.out.println("crawling " + link + " ...");
				crawler.crawlQueryPage();
				// write data of this page in respective dump file (append mode)
				crawler.write();
				NFCrawler.setQuery_count(NFCrawler.getQuery_count()+1);
				
				// ensure that the next page is not visited before the delay time has passed
				long passed_time = System.nanoTime() - start_time;
				try {
					if (passed_time < DELAY_NANOSECONDS) {TimeUnit.NANOSECONDS.sleep(DELAY_NANOSECONDS - passed_time);}
				} catch (InterruptedException e) {
					System.err.println("InterruptedException");
				}
			} else if(createQCLIRCorpus.isTestBool() == true && NFCrawler.getQuery_count() >= createQCLIRCorpus.getTestMax()){
				break;
			}
			
		}
	}
	
	/**
	 * Manages the process of crawling all data required from nutritionfacts.org.
	 * This includes collecting link lists of all relevant pages and then calling the method 
	 * 'crawlQueryPages' for each list of links (for each page type, e.g. articles).
	 * The results are saved in dump files for each page type meanwhile. 
	 * At the end, these files are put together into one dumpfile (nfdump.txt).
	 * <p>
	 * This method is also used to continue crawling if the process has been interrupted before.
	 */
	public static void crawl() {
		// build LinksCollector objects for each type of page
		ArticleLinksCollector ALC = ArticleLinksCollector.getInstance();
		QuestionsDoctorLinksCollector QDoctorLC = QuestionsDoctorLinksCollector.getInstance();
		QuestionsDietitianLinksCollector QDietLC = QuestionsDietitianLinksCollector.getInstance();
		//TopicLinksCollector TLC = TopicLinksCollector.getInstance();
		VideoLinksCollector VLC =  VideoLinksCollector.getInstance();
		LinksCollector[] linkscollectors = {VLC};
		
		/* collect all links that have to be visited:
		 * if the respective link file  don't exist the links are crawled
		 */
		
		for (LinksCollector collector : linkscollectors) {
			collector.collectQueryLinks();
		}
		
		// prepare the particular dump files, not knowing if they already exist
		
		File articledump = new File(Properties.PATHS_TO_QUERYDUMPS[0]);
		File q_diet = new File(Properties.PATHS_TO_QUERYDUMPS[1]);
		File q_doctor = new File(Properties.PATHS_TO_QUERYDUMPS[2]);
		File topicdump = new File(Properties.PATHS_TO_QUERYDUMPS[3]);
		File videodump = new File(Properties.PATHS_TO_QUERYDUMPS[4]);
		File[] dumpfiles = {articledump, q_diet, q_doctor, topicdump, videodump};
		
		// decide to crawl or continue crawling for each link list (each page type)
		
		for (int i=0; i < dumpfiles.length; i++) {
			// case: previous crawling process has been interrupted
			File dumpfile = dumpfiles[i];
			if (dumpfile.exists()) {
				// get Query ID from the last line in the dump to proceed with the correct IDs
				String[] lastline = io.Reader.readLastLine(Properties.PATHS_TO_QUERYDUMPS[i]).split("\t");
				Integer nextid = Integer.parseInt(lastline[0].split("-")[1]) - 1;
				String lastlink = lastline[1];
				// delete and crawl again the last line, it is possibly uncompleted
				io.Editor.deleteLastLine(Properties.PATHS_TO_QUERYDUMPS[i]);
				NFCrawler.setId(nextid);
				// crawl missing part of the link list
				crawlQueryPages(linkscollectors[i].getLinks(),  lastlink);
			// case: no dump exists from previous crawling -> crawl all pages of this query type
			} else {
				NFCrawler.setId(0);
				String startlink = linkscollectors[i].getLinks().get(0);
				crawlQueryPages(linkscollectors[i].getLinks(),  startlink);
			}
		}
		// put all dump files together
		io.Writer.concatenateFiles(Properties.PATHS_TO_QUERYDUMPS, Properties.NFDUMP_PATH);
	}
	
	/**
	 * 
	 * Crawls pages that are new since the last time the pages were collected, 
	 * not including new modified versions of pages that have been crawled already.
	 * <p>
	 * This requires that the last crawling process has been finished, i.e.
	 * that the data of all links in the links files have been crawled and 
	 * written into nfdump.txt. Otherwise, NFCrawler has to run again, until
	 * it is finished.
	 * 
	 */
	public static void update() {
		
		// build LinkCollector objects
		
		ArticleLinksCollector ALC = ArticleLinksCollector.getInstance();
		QuestionsDoctorLinksCollector QDoctorLC = QuestionsDoctorLinksCollector.getInstance();
		QuestionsDietitianLinksCollector QDietLC = QuestionsDietitianLinksCollector.getInstance();
		TopicLinksCollector TLC = TopicLinksCollector.getInstance();
		VideoLinksCollector VLC =  VideoLinksCollector.getInstance();
		LinksCollector[] linkscollectors = {ALC, QDietLC, QDoctorLC, TLC, VLC};
		
		// LinkCollectors find all new links that are not in the link list files
		
		for (LinksCollector collector : linkscollectors) {
			collector.updateLinks();
		}
		
		// preparing dump files
		
		File articledump = new File(Properties.PATHS_TO_QUERYDUMPS[0]);
		File q_diet = new File(Properties.PATHS_TO_QUERYDUMPS[1]);
		File q_doctor = new File(Properties.PATHS_TO_QUERYDUMPS[2]);
		File topicdump = new File(Properties.PATHS_TO_QUERYDUMPS[3]);
		File videodump = new File(Properties.PATHS_TO_QUERYDUMPS[4]);
		File[] dumpfiles = {articledump, q_diet, q_doctor, topicdump, videodump};
		
		// append new entries to the dump files
		
		for (int i=0; i < dumpfiles.length; i++) {

			String[] lastline = io.Reader.readLastLine(Properties.PATHS_TO_QUERYDUMPS[i]).split("\t");
			Integer nextid = Integer.parseInt(lastline[0].split("-")[1]) - 1;
			NFCrawler.setId(nextid + 1);
			ArrayList<String> new_links = linkscollectors[i].getNew_links_for_update();
			if (new_links.size() == 0) {
				continue;
			}
			String startlink = new_links.get(0);
			crawlQueryPages(linkscollectors[i].getNew_links_for_update(),  startlink);
		}
		
		// write new version of the final dump file
		io.Editor.deleteFile(Properties.NFDUMP_PATH);
		io.Writer.concatenateFiles(Properties.PATHS_TO_QUERYDUMPS, Properties.NFDUMP_PATH);
		
	}

	public static Integer getId() {
		return Id;
	}

	public static void setId(Integer id) {
		Id = id;
	}
	
	public static Integer getQuery_count() {
		return query_count;
	}

	public static void setQuery_count(Integer count) {
		NFCrawler.query_count = count;
	}

}
