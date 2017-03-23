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
 * Searches the links of all articles on nutritionfacts.org.
 * If the method was already used before the links are just taken from articlelinks.txt.
 * Otherwise they are crawled and saved both in the file and the instance variable articlel inks.
 * 
 * @author Vera Boteva, Demian Gholipour
 */


public class ArticleLinksCollector extends LinksCollector{
	final int FIRST_ARTICLE_YEAR = 2011;
	private static ArticleLinksCollector instance = null;
	
	private ArticleLinksCollector(){}
	
	/**
	 * Singleton Constructor
	 *
	 */
	public static ArticleLinksCollector getInstance(){
		 if(instance == null) {
			 instance = new ArticleLinksCollector();
			 instance.setOutput_path(Properties.ARTICLE_LINKS_PATH);
		     }
		 return instance;
	   }
	
	
	/**
	 * Starting at the blog articles main page, collects all links to articles and writes them in a file.
	 */
	@Override
	public void crawlQueryLinks() {
		ArrayList<String> articlelinks_list = new ArrayList<String>(); 
		HashSet<String> articlelinks_set = new HashSet<String>();
		Integer page;
		try {
			Document doc;
			doc = Jsoup.connect(Properties.ARTICLE_MAINPAGE).userAgent(Properties.USER_AGENT).timeout(TIMEOUT).get();
			Elements month_elements = doc.getElementsByTag("select").select("option");
			ArrayList<String> month_links = new ArrayList<String>();
			// first entry is empty
			for (Element element: month_elements.subList(1, month_elements.size())) {
				month_links.add(element.attr("value"));
			}
			
			// going through months
			for (String month_link: month_links) {
				
				page = 1;
				page_loop: 
				while (true) {
					try {
						Document pagedoc;
						pagedoc = Jsoup.connect(month_link + "/page/" + page.toString() + "/").userAgent(Properties.USER_AGENT).timeout(TIMEOUT).get();
						TimeUnit.SECONDS.sleep(DELAY_SECONDS);
						Elements links = pagedoc.getElementsByAttributeValue("role", "main").select("a[href]");
						// go through all urls on one page
						for (Element link_element : links) {
							String link = link_element.attr("href");
							// filter article links with regex
							if (QueryPageCrawler.isArticleLink(link) == true && articlelinks_set.contains(link) == false) {
								articlelinks_set.add(link);
								articlelinks_list.add(link);
								System.out.println("Collecting..." + link);
								// write article link in file by the way
								io.Writer.appendLineToFile(link, Properties.ARTICLE_LINKS_PATH);
								
								
							}
						}
					} catch (org.jsoup.HttpStatusException e) {
						System.err.println("HttpStatusException");
						break page_loop;
					} catch (InterruptedException e) {
						System.err.println("InterruptedException");
					}
					page += 1;
				}
				
			}
		
		} catch (IOException e) {
			System.err.println("IOException");
		}
			
		this.setLinks(articlelinks_list);		
	}
	
	/**
	 * Updates older link files with only the chronologically newest article links.
	 */
	@Override
	public void updateLinks() {
		this.readQueryLinks();
		String old_first_link = this.getLinks().get(0);
		ArrayList<String> new_articlelinks_list = new ArrayList<String>(); 
		HashSet<String> new_articlelinks_set = new HashSet<String>();
		Integer page;
		try {
			Document doc;
			doc = Jsoup.connect(Properties.ARTICLE_MAINPAGE).userAgent(Properties.USER_AGENT).timeout(TIMEOUT).get();
			Elements month_elements = doc.getElementsByTag("select").select("option");
			ArrayList<String> month_links = new ArrayList<String>();
			// first entry is empty
			for (Element element: month_elements.subList(1, month_elements.size())) {
				month_links.add(element.attr("value"));
			}
			// going through months
			month_loop:
			for (String month_link: month_links) {
				page = 1;
				page_loop: 
				while (true) {
					try {
						Document pagedoc;
						pagedoc = Jsoup.connect(month_link + "/page/" + page.toString() + "/").userAgent("Student Project Uni Heidelberg (gholipour@stud.uni-heidelberg.de)").timeout(TIMEOUT).get();
						TimeUnit.SECONDS.sleep(DELAY_SECONDS);
						Elements links = pagedoc.getElementsByAttributeValue("role", "main").select("a[href]");
						// go through all urls on one page
						for (Element link_element : links) {
							String link = link_element.attr("href");
							if (link.equals(old_first_link)) {
								break month_loop;
							}
							// filter article links with regex
							if (QueryPageCrawler.isArticleLink(link) == true && new_articlelinks_set.contains(link) == false) {
								new_articlelinks_set.add(link);
								new_articlelinks_list.add(link);
								System.out.println("Collecting... " + link);
							}
						}
					} catch (org.jsoup.HttpStatusException e) {
						System.err.println("HttpStatusException");
						break page_loop;
					} catch (InterruptedException e) {
						System.err.println("InterruptedException");
					}
					page += 1;
				}
				
				
			}
		
		} catch (IOException e) {
			System.err.println("IOException");
		}
			
		this.setNew_links_for_update(new ArrayList<String> (new_articlelinks_list));
		new_articlelinks_list.addAll(this.getLinks());
		this.setLinks(new_articlelinks_list);
		io.Editor.deleteFile(Properties.ARTICLE_LINKS_PATH);
		for (String link : this.getLinks()) {
			io.Writer.appendLineToFile(link, Properties.ARTICLE_LINKS_PATH);
		}		
	}
	
	public static void main(String[] args){
		ArticleLinksCollector ALC = ArticleLinksCollector.getInstance();
		ALC.collectQueryLinks();
	}

}
