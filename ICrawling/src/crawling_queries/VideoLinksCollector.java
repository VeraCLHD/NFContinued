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
 * Searches the links of all videos on nutritionfacts.org. 
 * If the method was already used before the links are just taken from videolinks.txt.
 * Otherwise they are crawled and saved both in the file and the instance variable videolinks.
 * 
 * @author Vera Boteva, Demian Gholipour
 */

public class VideoLinksCollector extends LinksCollector {
	
	
	private static VideoLinksCollector instance = null;
		
	private VideoLinksCollector(){ }
	
	/**
	 * A method that creates the VideoLinksCollector object: singleton pattern
	 */
	public static VideoLinksCollector getInstance(){
		if(instance == null) {
			instance = new VideoLinksCollector();
			instance.setOutput_path(Properties.VIDEO_LINKS_PATH);
		}
	return instance;
		   } 
	
	/**
	 * Crawls the video links from nutritionfacts.org. Uses the main video pages where all of the nutrition videos are listed.
	 */
	@Override
	public void crawlQueryLinks() {
			ArrayList<String> videolinks_list = new ArrayList<String>(); 
			// save links in set since one video can be linked several times 
			HashSet<String> videolinks_set = new HashSet<String>();
			Integer page = 1;
			boolean further_page = true;
			while(further_page == true){
			//for (int i = 0; i <= max_page; i++) {
				try {
				    Document doc;
					doc = Jsoup.connect(Properties.VIDEO_PAGES_LINK + page.toString() + "/").userAgent("Student Project Uni Heidelberg (gholipour@stud.uni-heidelberg.de)").timeout(TIMEOUT).get();
					TimeUnit.SECONDS.sleep(DELAY_SECONDS);
					Elements navigations = doc.select("a[class=page larger]");
					if(navigations.isEmpty()){
						further_page = false;
						break;
					}
					Elements links = doc.select("a[href]");
					// go through all urls on one page
					for (Element link_element : links) {
						String link = link_element.attr("href");
						// filter video links with regex
						if (QueryPageCrawler.isVideoLink(link) == true && videolinks_set.contains(link) == false) {
							videolinks_list.add(link);
							videolinks_set.add(link);
							// write video link in file by the way
							io.Writer.appendLineToFile(link, Properties.VIDEO_LINKS_PATH);
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
				System.out.println("Crawling links from page " + page.toString());
				page += 1;
			}
			this.setLinks(videolinks_list);
		}
	
	/**
	 * Updates the links if the crawler is repeatedly used: this way, it doesn't start from scratch every time.
	 * If the file where the links are written exists, it gets only the new ones: videos added since the last crawling proccess.
	 * Since nutritionfacts uses a chronological order, links are crawled only until the first video in the file is reached.
	 */
	@Override
	public void updateLinks(){
		this.readQueryLinks();
		String old_first_link = this.getLinks().get(0);
		ArrayList<String> new_videolinks_list = new ArrayList<String>(); 
		HashSet<String> new_videolinks_set = new HashSet<String>();
		Integer page = 1;
		
		boolean further_page = true;
		outer_loop:
		while(further_page == true){
			try {
			    Document doc;
				doc = Jsoup.connect(Properties.VIDEO_PAGES_LINK + page.toString() + "/").userAgent("Student Project Uni Heidelberg (gholipour@stud.uni-heidelberg.de)").timeout(TIMEOUT).get();
				TimeUnit.SECONDS.sleep(DELAY_SECONDS);
				Elements navigations = doc.select("a[class=page larger]");
				if(navigations.isEmpty()){
					further_page = false;
					break;
				}
				Elements links = doc.select("a[href]");
				// go through all urls on one page
				for (Element link_element : links) {
					String link = link_element.attr("href");
					if (link.equals(old_first_link)) {
						break outer_loop;
					}
					// filter video links with regex
					if (QueryPageCrawler.isVideoLink(link) == true && new_videolinks_set.contains(link) == false) {
						new_videolinks_list.add(link);
						new_videolinks_set.add(link);
						System.out.println("Collecting... "+ link);
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
		this.setNew_links_for_update(new ArrayList<String> (new_videolinks_list));
		new_videolinks_list.addAll(this.getLinks());
		this.setLinks(new_videolinks_list);
		io.Editor.deleteFile(Properties.VIDEO_LINKS_PATH);
		for (String link : this.getLinks()) {
			io.Writer.appendLineToFile(link, Properties.VIDEO_LINKS_PATH);
		}
		
	}
	
	public Integer getMaxNumOfPages() {
		Integer last_page_number = 1;
		try{
			Document doc;
			doc = Jsoup.connect(Properties.VIDEO_MAIN_PAGE).userAgent("Student Project Uni Heidelberg (gholipour@stud.uni-heidelberg.de)").timeout(TIMEOUT).get();
			TimeUnit.SECONDS.sleep(DELAY_SECONDS);
			//right now working with only the first 10 pages
			Elements links = doc.select("a[class=page larger]");
			
			last_page_number = Integer.parseInt(links.last().text());
		}
		catch (org.jsoup.HttpStatusException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	return last_page_number;
	}




}
