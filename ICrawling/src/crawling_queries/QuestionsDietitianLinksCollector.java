/**
 * 
 */
package crawling_queries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Searches the links of all question pages on nutritionfacts.org. 
 * If the method was already used before the links are just taken from questionlinks.txt.
 * Otherwise they are crawled and saved both in the file and the instance variable questionlinks.
 * 
 * @author Vera Boteva, Demian Gholipour
 */

public class QuestionsDietitianLinksCollector extends LinksCollector {

	private static QuestionsDietitianLinksCollector instance = null;
	private final String QUESTION_STARTPAGE = "http://nutritionfacts.org/rdquestions/";
	
	private QuestionsDietitianLinksCollector(){ }
	
	public static QuestionsDietitianLinksCollector getInstance(){
		 if(instance == null) {
			 instance = new QuestionsDietitianLinksCollector();
			 instance.setOutput_path(Properties.QUESTION_DIETITIAN_LINKS_PATH);
		     }
		 return instance;
	   } 
	
	/**
	 * Starting at the ask the dietitian questions main page, collects all links to questions and writes them in a file.
	 */
	@Override
	public void crawlQueryLinks() {
		HashSet<String> questionlinks_set = new HashSet<String>();
		ArrayList<String> questionlinks_list = new ArrayList<String>();
		// 'ask the dietrician' pages
			assert(Properties.QUESTIONS_DIETITIAN_PAGE_PATTERN.matcher(QUESTION_STARTPAGE).matches());
			// save links in set since one question page can be linked several times 
			Integer page = 1;
			while (true) {
				try {
				    Document doc;
					doc = Jsoup.connect(QUESTION_STARTPAGE + "page/" + page.toString() + "/").userAgent(Properties.USER_AGENT).timeout(TIMEOUT).get();
					TimeUnit.SECONDS.sleep(DELAY_SECONDS);
					Elements links = doc.select("a[href]");
					// go through all urls on one page
					for (Element link_element : links) {
						String link = link_element.attr("href");
						// filter question links with regex
						if (QueryPageCrawler.isQuestionDietitianLink(link) == true && questionlinks_set.contains(link) == false) {
							questionlinks_set.add(link);
							questionlinks_list.add(link);
							// write question link in file by the way
							io.Writer.appendLineToFile(link, Properties.QUESTION_DIETITIAN_LINKS_PATH);
							System.out.println("Collecting... " + link);
						}
					}
				} catch (org.jsoup.HttpStatusException e) {
					System.err.println("HttpStatusException");
					break;
				} catch (IOException e) {
					System.err.println("IOException");
				} catch (InterruptedException e) {
					System.err.println("InterruptedException");
				} 
				page += 1;
			}		
		this.setLinks(questionlinks_list);
		}
	
	/**
	 * Updates older link files with only the chronologically newest dietitian question links.
	 */
	public void updateLinks() {
		this.readQueryLinks();
		String old_first_link = this.getLinks().get(0);
		HashSet<String> new_questionlinks_set = new HashSet<String>();
		ArrayList<String> new_questionlinks_list = new ArrayList<String>();
		// 'ask the dietrician' pages
			assert(Properties.QUESTIONS_DIETITIAN_PAGE_PATTERN.matcher(QUESTION_STARTPAGE).matches());
			// save links in set since one question page can be linked several times 
			Integer page = 1;
			outer_loop: while (true) {
				try {
				    Document doc;
					doc = Jsoup.connect(QUESTION_STARTPAGE + "page/" + page.toString() + "/").userAgent("Student Project Uni Heidelberg (gholipour@stud.uni-heidelberg.de)").timeout(TIMEOUT).get();
					TimeUnit.SECONDS.sleep(DELAY_SECONDS);
					Elements links = doc.select("a[href]");
					// go through all urls on one page
					for (Element link_element : links) {
						String link = link_element.attr("href");
						if (link.equals(old_first_link)) {
							break outer_loop;
						}
						// filter question links with regex
						if (QueryPageCrawler.isQuestionLink(link) == true && new_questionlinks_set.contains(link) == false) {
							new_questionlinks_set.add(link);
							new_questionlinks_list.add(link);
							System.out.println("Collecting... " + link);
						}
					}
				} catch (org.jsoup.HttpStatusException e) {
					System.err.println("HttpStatusException");
					break;
				} catch (IOException e) {
					System.err.println("IOException");
				} catch (InterruptedException e) {
					System.err.println("InterruptedException");
				} 
				page += 1;
			}	
			this.setNew_links_for_update(new ArrayList<String> (new_questionlinks_list));
			new_questionlinks_list.addAll(this.getLinks());
			this.setLinks(new_questionlinks_list);
			io.Editor.deleteFile(Properties.QUESTION_DIETITIAN_LINKS_PATH);
			for (String link : this.getLinks()) {
				io.Writer.appendLineToFile(link, Properties.QUESTION_DIETITIAN_LINKS_PATH);
			}
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		QuestionsDietitianLinksCollector ql = QuestionsDietitianLinksCollector.getInstance();
		
		ql.crawlQueryLinks();
	}

}
