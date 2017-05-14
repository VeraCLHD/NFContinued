package crawling_queries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * This class is responsible for crawling the relevant data of one blog article page from nutritionfacts.org
 * and writing them into the dump file.
 * 
 * Crawled data: 
 * - (own link)
 * - title
 * - text
 * - topics(tags)
 * - comments
 * - links to other articles, videos, question, topic pages
 * - other links
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */

public class ArticlePageCrawler extends QueryPageCrawler {
	 
	final static int FIRST_ARTICLE_YEAR = 2011;
	final static String ARTICLE_MARKUP  ="A-";
	private String topics;
	
	
	/**
	 * Constructor that uses a querylink to produce an instance of this class.
	 * @param querylink
	 */
	public ArticlePageCrawler(String querylink) {
		super(querylink);
		NFCrawler.setId(NFCrawler.getId()+1);
		this.setQueryId(ArticlePageCrawler.getArticleMarkup() + NFCrawler.getId());
	}		
	
	/**
	 * Visits article site, gets the html and calls all particular crawling methods.
	 */
	@Override
	public void crawlQueryPage() {
			this.setConnection(this.getQuerylink());
			this.crawlTitle();
			this.crawlCreationDate();
			this.crawlText();
			this.crawlTopics();
			this.crawlComments();
			this.crawlLinkedQueryLinks();
			this.crawlDocumentLinks();
	}
	
	/**
	 * Crawls the text of an article on nutritionfacts.org.
	 */
	public void crawlText() {
		if (this.getHtml().getElementsByAttributeValue("class", "entry-content") == null) {
			this.setText("-");
		} else {
			String text = "";
			Elements els = this.getHtml().getElementsByAttributeValue("class", "entry-content").get(0).getElementsByTag("p");
			List<Element> relevant_elements = new ArrayList<Element> (els);
			if (els.size() > 3) {
				relevant_elements = els.subList(0, els.size() - 3);
			}
			for (Element e: relevant_elements) {
				text += e.text() + " ";
			}
			this.setText(text);
		}
	}
	
	/**
	 * Crawls the topics as text from an article page.
	 */
	public void crawlTopics() {
		if (this.getHtml().getElementsByAttributeValue("class", "entry-tags") == null) {
			this.setTopics("-");
		} else {
			ArrayList<String> taglist = new ArrayList<String>();
			Element tags_element = this.getHtml().getElementsByAttributeValue("class", "entry-tags").get(0);
			for (Element e: tags_element.getElementsByAttributeValue("rel", "tag")) {
				taglist.add(e.text());
			}
			String tags = String.join(",", taglist);
			this.setTopics(tags);
		}
	}
	
	/**
	 * Crawls linked video links from within the article text.
	 */
	@Override
	public void crawlLinkedVideoLinks() {
		HashSet<String> videolinks_set = new HashSet<String>();
		
		// not searching links in the whole html, only in the text
		Elements links = this.getHtml().getElementsByAttributeValue("class", "entry-content").select("p").select("a[href]");
		for (Element link_element : links) {			
			String link = link_element.attr("href");
			if(QueryPageCrawler.isVideoLink(link)){
				videolinks_set.add(link);
			}
			
		}
		ArrayList<String> videolinks_list = new ArrayList<String>(videolinks_set); 
		String videolinks = String.join(",", videolinks_list);
		this.setVideolinks(videolinks);
		
	}
	
	/**
	 * Crawls linked article links from within the article text.
	 */
	@Override
	public void crawlLinkedArticleLinks(){
		HashSet<String> articlelinks_set = new HashSet<String>();
		Elements links = this.getHtml().getElementsByAttributeValue("class", "entry-content").select("p").select("a[href]");
		for (Element link_element : links) {
			String link = link_element.attr("href");
			if (QueryPageCrawler.isArticleLink(link) == true) {
				articlelinks_set.add(link);
			}
		}
		
		ArrayList<String> articlelinks_list = new ArrayList<String>(articlelinks_set); 
		String articlelinks = String.join(",", articlelinks_list);
		this.setArticlelinks(articlelinks);
	}
	
	/**
	 * Crawls linked question links from within the article text.
	 */
	@Override
	public void crawlLinkedQuestionLinks(){
		HashSet<String> questionlinks_set = new HashSet<String>();
		Elements links = this.getHtml().getElementsByAttributeValue("class", "entry-content").select("p").select("a[href]");
		for (Element link_element : links) {
			String link = link_element.attr("href");
			if (QueryPageCrawler.isQuestionLink(link) == true) {
				questionlinks_set.add(link);
			}
		}
		ArrayList<String> questionlinks_list = new ArrayList<String>(questionlinks_set);
		String questionlinks = String.join(",", questionlinks_list);
		this.setQuestionlinks(questionlinks);
	}
	
	/**
	 * Crawls linked topic links from within the article text.
	 */
	public void crawlLinkedTopicLinks(){
		HashSet<String> topiclinks_set = new HashSet<String>();
		Elements links = this.getHtml().select("a[href]");
		for (Element link_element : links) {
			String link = link_element.attr("href");
			if (QueryPageCrawler.isTopicLink(link) == true) {
				topiclinks_set.add(link);
			}
		}
		ArrayList<String> topiclinks_list = new ArrayList<String>(topiclinks_set);
		String topiclinks = String.join(",", topiclinks_list);
		this.setTopiclinks(topiclinks);
	}
	
	/**
	 * Crawls all other query links linked on the article page.
	 */
	@ Override
	public void crawlLinkedQueryLinks() {
		this.crawlLinkedArticleLinks();
		this.crawlLinkedQuestionLinks();
		this.crawlLinkedTopicLinks();
		this.crawlLinkedVideoLinks();	
	}

	/**
	 * Crawls all document links from the text of the article.
	 */
	@Override
	public void crawlDocumentLinks() {
		HashSet<String> doclinks_set = new HashSet<String>();
		Elements els = this.getHtml().getElementsByAttributeValue("class", "entry-content").select("p").select("a[href]");
		List<Element> relevant_elements = new ArrayList<Element> (els);
		if (els.size() > 3) {
			relevant_elements = els.subList(0, els.size() - 3);
		}
		for (Element link_element : relevant_elements) {
			String link = link_element.attr("href");
			if (QueryPageCrawler.isUrl(link)){
				doclinks_set.add(link);
			}
		}
		ArrayList<String> doclinks_list = new ArrayList<String>(doclinks_set);
		String doclinks = String.join(",", doclinks_list);
		this.setDoclinks(doclinks);
	}
	
	/**
	 * Writes the crawled content to the nfdump.
	 */
	@Override
	public void write() {
		
		String[] components = {
				String.valueOf(this.getQueryId()), 
				this.getQuerylink(), 
				this.getTitle(), 
				this.getText(), 
				this.getComments(), 
				this.getTopics(), 
				"-",  // empty description
				"-", // empty Doctor's Note
				this.getArticlelinks(), 
				this.getQuestionlinks(),
				this.getTopiclinks(),
				this.getVideolinks(), 
				this.getDoclinks()};
		
		String line = "";
		for (String component: components){
			if(component == null || component.isEmpty()){
				line += "-" + "\t";
			} else {
				line += component + "\t";
			}
		}
	io.Writer.appendLineToFile(line, Properties.PATHS_TO_QUERYDUMPS[0]);
	String forecast_line = components[0] + "\t" + components[0].split("-")[0] + "\t" + components[0].split("-")[1] + "\t" + this.getDate();
	io.Writer.appendLineToFile(forecast_line, Properties.PATH_TO_QUERY_FORECAST);
	}
	
	public static void main(String[] args) {
		
	}
	
	public static String getArticleMarkup() {
		return ARTICLE_MARKUP;
	}


	public String getTopics() {
		return topics;
	}

	public void setTopics(String topics) {
		this.topics = topics;
	}
	
}
