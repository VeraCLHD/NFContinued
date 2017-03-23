package crawling_queries;

import java.io.IOException;
import java.util.regex.Matcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * An abstract class that crawls the content of any query page.
 * @author Vera Boteva, Demian Gholipour
 *
 */
public abstract class QueryPageCrawler {
	protected final static int TIMEOUT = 6000; // milliseconds
	protected final int DELAY_SECONDS = 4;
	
	private String queryId;
	private String querylink;
	private Document html;
	private String title;
	private String text;
	private String date;
	
	private String comments;
	private String articlelinks;
	private String questionlinks;
	private String topiclinks;
	private String videolinks;
	private String doclinks;
	
	
	
	
	public QueryPageCrawler(String querylink){
		this.setQuerylink(querylink);
	}
	
	
	public String getQueryId(){
		return this.queryId;
	}
	
	public void setQueryId(String id){
		this.queryId = id;
	}
	

	public String getQuerylink() {
		return querylink;
	}

	public void setQuerylink(String querylink) {
		this.querylink = querylink;
	}

	public Document getHtml() {
		return html;
	}

	public void setHtml(Document html) {
		this.html = html;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getArticlelinks() {
		return articlelinks;
	}

	public void setArticlelinks(String articlelinks) {
		this.articlelinks = articlelinks;
	}

	public String getVideolinks() {
		return videolinks;
	}

	public void setVideolinks(String videolinks) {
		this.videolinks = videolinks;
	}

	public String getDoclinks() {
		return doclinks;
	}

	public void setDoclinks(String doclinks) {
		this.doclinks = doclinks;
	}
	
	public String getQuestionlinks() {
		return questionlinks;
	}


	public void setQuestionlinks(String questionLinks) {
		this.questionlinks = questionLinks;
	}
	

	public String getTopiclinks() {
		return topiclinks;
	}


	public void setTopiclinks(String topiclinks) {
		this.topiclinks = topiclinks;
	}
	
	public String getDate() {
		return date;
	}


	public void setDate(String date) {
		this.date = date;
	}
	
	/**
	 * Checks if a link is a video link
	 * @param link the link to be checked
	 * @return true if video link, false if not.
	 */
	public static boolean isVideoLink(String link) {
		Matcher m = Properties.VIDEO_PAGE_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;
	}
	
	/**
	 * Checks if a link is a article link
	 * @param link the link to be checked
	 * @return true if article link, false if not.
	 */
	public static boolean isArticleLink(String link) {
		Matcher m = Properties.ARTICLE_PAGE_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;
	}
	
	/**
	 * Checks if a link is a questions link
	 * @param link the link to be checked
	 * @return true if questions link, false if not.
	 */
	public static boolean isQuestionLink(String link) {
		Matcher m = Properties.QUESTIONS_PAGE_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;
	}
	
	/**
	 * Checks if a link is a questions of dietitian link
	 * @param link the link to be checked
	 * @return true if questions of dietitian link, false if not.
	 */
	public static boolean isQuestionDietitianLink(String link) {
		Matcher m = Properties.QUESTIONS_DIETITIAN_PAGE_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;
	}
	
	/**
	 * Checks if a link is a questions of doctor link
	 * @param link the link to be checked
	 * @return true if questions of doctor link, false if not.
	 */
	public static boolean isQuestionDoctorLink(String link) {
		Matcher m = Properties.QUESTIONS_DOCTOR_PAGE_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;
	}
	
	/**
	 * Checks if a link is a nutritionfacts link
	 * @param link the link to be checked
	 * @return true if nutritionfacts link, false if not.
	 */
	public static boolean isNFLink(String link) {
		Matcher m = Properties.NF_PAGE_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;
	}
	
	/**
	 * Checks if a link is a topic description link
	 * @param link the link to be checked
	 * @return true if  topic description link, false if not.
	 */
	public static boolean isTopicLink(String link){
		Matcher m = Properties.TOPICS_PAGE_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;
	}
	
	public static boolean isUrl(String link) {
		Matcher m = Properties.URL_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;
	}
	
	/**
	 * Checks if a link is the root of a domain structure.
	 * @param link link to be checked
	 * @return true, if main page; false else
	 */
	public static boolean isMainPage(String link) {
		Matcher m = Properties.MAIN_PAGE_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;
	}
	
	/**
	 * For crawling any content, a connection to the page has to be built. Here, jsoup is used to build the connection and 
	 * crawl the content.
	 * @param querylink the link to which the connection is built.
	 */
	public void setConnection(String querylink){
		Document doc = null;
		try {
			doc = Jsoup.connect(querylink).userAgent(Properties.USER_AGENT).timeout(TIMEOUT).get();
		} catch (org.jsoup.HttpStatusException e) {
			System.err.println("HttpStatusException");
		} catch (IOException e) {
			System.err.println("IOException");
		} 
		this.setHtml(doc);
	}
	
	/**
	 * Crawls the title of a query page.
	 */
	public void crawlTitle() {
		if (this.getHtml().getElementsByTag("title") == null) {
			this.setTitle("-");
		} else {
			String title_complete = this.getHtml().getElementsByTag("title").get(0).text();
			String title = title_complete.split("\\s\\|")[0];
			this.setTitle(title);
		}
	}
	
	/**
	 * Crawls the comments of a video, questions, or article page - only these have comments.
	 */
	public void crawlComments() {
		if (this.getHtml().getElementById("dsq-comments") == null) {
			this.setComments("-");
		} else {
			Elements comment_elements = this.getHtml().getElementById("dsq-comments").select("p");
			String comments = "";
			for (Element e: comment_elements) {
				comments += e.text();
				}
			comments = comments.replaceAll("\\s+", " ");
			this.setComments(comments);
		}
	}
	
	/**
	 * crawls the date of creation or last update of a page: used for forecasting the amount of articles,
	 * videos, questions, topics per month/year.
	 */
	public void crawlCreationDate(){
		if (this.getHtml().getElementsByTag("time").get(0).getElementsByAttribute("datetime") == null){
			this.setDate("-");
		} else {
			String date = this.getHtml().getElementsByTag("time").get(0).text();
			this.setDate(date);
		}
	}

	
	/**
	 * crawls all articles and videos linked directly from a query page
	 */
	
	public abstract void crawlLinkedQueryLinks();
	
	/**
	 *  crawls all medical articles linked directly from a query page
	 */
	/**
	 * Crawls the document links from a query text. Implemented by the special crawlers.
	 */
	public abstract void crawlDocumentLinks();
	/**
	 * Crawls the linked video links from a query text. Implemented by the special crawlers.
	 */
	public abstract void crawlLinkedVideoLinks();
	/**
	 * Crawls the linked article links from a query text. Implemented by the special crawlers.
	 */
	public abstract void crawlLinkedArticleLinks();
	/**
	 * Crawls the linked question links from a query text.Implemented by the special crawlers.
	 */
	public abstract void crawlLinkedQuestionLinks();
	
	/**
	 * writes the crawled content into the nfdump. Implemented by the special crawlers.
	 */
	public abstract void write();
	
	/** 
	 * Calls all the concrete crawling methods/components. Implemented by the special crawlers.
	 */
	public abstract void crawlQueryPage();

}


