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
 * A class that collects all of the links to topic pages from nutritionfacts.org
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class TopicLinksCollector extends LinksCollector {

	
	private final static String TOPICS_STARTPAGE = "http://nutritionfacts.org/topics/";
	private static TopicLinksCollector instance = null;
	
	public static TopicLinksCollector getInstance(){
		 if(instance == null) {
			 instance = new TopicLinksCollector();
			 instance.setOutput_path(Properties.TOPIC_LINKS_PATH);
		     }
		 return instance;
	   } 
	
	/**
	 * private constructor serving the singleton pattern -> TopicLinksCollector can be instantiated only once
	 */
	private TopicLinksCollector(){ }
	
	/**
	 * Starting at the topics main page, collects all links to topics and writes them in a file.
	 */
	@Override
	public void crawlQueryLinks() {
			
		HashSet<String> topiclinks_set = new HashSet<String>();
		ArrayList<String> topiclinks_list = new ArrayList<String>(); 
		try {
		    Document doc;
			doc = Jsoup.connect(TopicLinksCollector.TOPICS_STARTPAGE).userAgent("Student Project Uni Heidelberg (boteva@cl.uni-heidelberg.de)").timeout(TIMEOUT).get();
			TimeUnit.SECONDS.sleep(DELAY_SECONDS);
			Elements links = doc.select("a[href]");
			// go through all urls on one page
			for (Element link_element : links) {
				String link = link_element.attr("href");		
				// filter topic links with regex
				if (Properties.TOPICS_PAGE_PATTERN.matcher(link).matches() && !topiclinks_set.contains(link)){
					topiclinks_set.add(link);
					topiclinks_list.add(link);
					// write topic link in file by the way
					io.Writer.appendLineToFile(link, Properties.TOPIC_LINKS_PATH);
					System.out.println("Collecting... " + link);	
				}	
			}
		} catch (org.jsoup.HttpStatusException e) {
			System.err.println("HttpStatusException");
		} catch (IOException e) {
			System.err.println("IOException");
		} catch (InterruptedException e) {
			System.err.println("InterruptedException");
		}
		
		this.setLinks(topiclinks_list);
	}
	
	/**
	 * Updates older link files with only the chronologically newest topic links.
	 */
	@Override
	public void updateLinks() {
		this.readQueryLinks();
		HashSet<String> old_topiclinks_set = new HashSet<String>(this.getLinks());
		HashSet<String> new_topiclinks_set = new HashSet<String>();
		ArrayList<String> new_topiclinks_list = new ArrayList<String>(); 
		try {
		    Document doc;
			doc = Jsoup.connect(TopicLinksCollector.TOPICS_STARTPAGE).userAgent("Student Project Uni Heidelberg (boteva@cl.uni-heidelberg.de)").timeout(TIMEOUT).get();
			TimeUnit.SECONDS.sleep(DELAY_SECONDS);
			Elements links = doc.select("a[href]");
			// go through all urls on one page
			for (Element link_element : links) {
				String link = link_element.attr("href");		
				// filter topic links with regex
				if (Properties.TOPICS_PAGE_PATTERN.matcher(link).matches() && !new_topiclinks_set.contains(link) && !old_topiclinks_set.contains(link)){
					new_topiclinks_set.add(link);
					new_topiclinks_list.add(link);
					System.out.println("Collecting... " + link);
				}	
			}
		} catch (org.jsoup.HttpStatusException e) {
			System.err.println("HttpStatusException");
		} catch (IOException e) {
			System.err.println("IOException");
		} catch (InterruptedException e) {
			System.err.println("InterruptedException");
		} 
		
		this.setNew_links_for_update(new ArrayList<String> (new_topiclinks_list));
		new_topiclinks_list.addAll(this.getLinks());
		this.setLinks(new_topiclinks_list);
		io.Editor.deleteFile(Properties.TOPIC_LINKS_PATH);
		for (String link : this.getLinks()) {
			io.Writer.appendLineToFile(link, Properties.TOPIC_LINKS_PATH);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TopicLinksCollector tl = TopicLinksCollector.getInstance();
		tl.updateLinks();
	}

}
