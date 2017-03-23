/**
 * 
 */
package crawling_queries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.Writer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A class that exctracts the relevant content from a single topic page at nutritionfacts.org.
 * The topics are used later for indirect link to a document (and thus a lower relevance than a query linked directy to a document).
 * Other Queries (aritcles, videos, questions) found on the topic page have the same relevance - regardless if they are found in the text or under "related"
 * <p>
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class TopicPageCrawler extends QueryPageCrawler {
	// a markup for the id of a topic_page in the initial nutrition facts dump
	private static final String TOPIC_MARKUP = "T-";
	// the related topics of a topic
	private String topics;
	
	// linked articles on a topic page
	private Set<String> linkedArticlesSet = new HashSet<String>();
	// linked topics on topic page
	private Set<String> linkedTopicsSet = new HashSet<String>();
	// linked videos on topic page
	private Set<String> linkedVideosSet = new HashSet<String>();
	// linked question pages on a topic page
	private Set<String> linkedQuestionsSet = new HashSet<String>();
	// linked documents on a topic page: just a precaution -> will probably be empty because documents are usually not linked directly 
	// on a topic page
	private Set<String> linkedDocumentsSet = new HashSet<String>();
	
	
	/**
	 * Constructor: calls the constructor of a QueryPageCrawler and takes care of giving the Topic Page a suitable id.
	 * @param querylink String a link to a topic page
	 */
	public TopicPageCrawler(String querylink) {
		super(querylink);
		NFCrawler.setId(NFCrawler.getId()+1);
		this.setQueryId(TopicPageCrawler.getTopicMarkup() +  NFCrawler.getId());
	}
	
	/**
	 * crawls the text of a topic page and takes out the last sentence: "To help out on the site, please consider volunteering."
	 * @see QueryPageCrawler
	 */
	public void crawlText(){
		String text = "";
		Document doc = this.getHtml();
		Elements divTags = doc.getElementsByAttributeValue("class", "entry-content");
		
		Element textElements = divTags.get(0);
		Elements pTagsInText = textElements.getElementsByTag("p");
		int sizeTELements = pTagsInText.size();
		// some topic pages don't have any text -> text would be a "-"
		if (sizeTELements == 1){
			this.setText("-");
		}
		else{
			for(int i = 0; i<sizeTELements-1; i++){
				String paragraphInText = pTagsInText.get(i).text();
				text += paragraphInText;
			}
			this.setText(text);
		}

	}

	
	/**
	 * Crawls related VideoLinks for a given topic page. Requires a document argument because this is done for different pages: one or several related videos pages on the topic page.
	 * @param doc the Document in html format
	 */
	public void crawlRelatedVideoLinks(Document doc){
		Elements references = doc.select("a[rel]");
		Integer refSize = references.size();
		// this loop gets the video Links from the text
		for(int i = 0; i < refSize; i++){
			String link = references.get(i).absUrl("href");
			if(QueryPageCrawler.isVideoLink(link)){
				this.getLinkedVideosSet().add(link);
			}
	}
	}
	
	/**
	 * Crawls all video links to the set of VideoLinks for a given topic page.
	 * <p>
	 * Related videos can be found either directly in the text or in the section "related videos" (equally relevant) -> This method gets the video links from the text.
	 */
	public void crawlVideoLinksFromText(){
		Document doc = this.getHtml();
		Elements references = doc.select("div[class=entry-content]").first().select("a[href]");
		Integer refSize = references.size();
		// this loop gets the video Links from the text
		for(int i = 0; i < refSize; i++){
			String link = references.get(i).absUrl("href");
			if(QueryPageCrawler.isVideoLink(link)){
				this.getLinkedVideosSet().add(link);
			}
	}
	}
	
	
	/**
	 * Crawls the videoLinks from the section "Watch more videos about...": there could be more than one page containing such videos.
	 * <p>
	 * Related videos can be found either directly in the text or in the section "related videos" (equally relevant) -> This method gets the video links from the related-section.
	 */
	public void crawlLinkedVideoLinksFromRelated(){
		Document doc = this.getHtml();
		// here the videos of the first "Watch videos about..." page are crawled
		this.crawlRelatedVideoLinks(doc);
		// this is the element of the second page if there is one
		Element nextPage = doc.getElementsByAttributeValue("class", "page larger").first();
		
		// if there is no next page, this is it -> we've already got the videos from the first and in this case single page
		if(nextPage != null){
			// the link to the next page
			String linkToNextPage = nextPage.getElementsByAttribute("href").first().absUrl("href");
			try {
				// we use the second related videos page to extract the max. number of related video pages; this information is only on the second page
				Document secondPage;
				secondPage = Jsoup.connect(linkToNextPage).userAgent("Student Project Uni Heidelberg (gholipour@stud.uni-heidelberg.de)").timeout(TIMEOUT).get();
			    TimeUnit.SECONDS.sleep(DELAY_SECONDS);
			    String title = secondPage.getElementsByTag("title").text();
			    String[] titleArray = title.split("\\|");
			    assert(titleArray.length == 4);
			    String[] pageXOfX = titleArray[1].split("\\s+");
			    assert(pageXOfX.length == 5);
			    Integer lastPage = Integer.parseInt(pageXOfX[4].trim());
			    // while we are at it, we crawl the related video links from the second pages as well
			    this.crawlRelatedVideoLinks(secondPage);
			    try{
			    	// if there are only two pages, they are already processed (related videos are already crawled) at this point
			    	if(lastPage >= 3){
			    		
			    		for(int i = 3; i <= lastPage; i++){
			    			// a connection is set up for every further related videos page and a Document object is created
			    			Document furtherPage;
			    			furtherPage = Jsoup.connect(this.getQuerylink() + "/" + "page" + "/" + String.valueOf(i)).userAgent("Student Project Uni Heidelberg (gholipour@stud.uni-heidelberg.de)").timeout(TIMEOUT).get();
						    TimeUnit.SECONDS.sleep(DELAY_SECONDS);
						    // crawling the links from every further page
						    this.crawlRelatedVideoLinks(furtherPage);
				    	}
			    	}
			    	
			    }
			    
			    catch (org.jsoup.HttpStatusException e) {
					System.err.println("HttpStatusException");
				} catch (IOException e) {
					System.err.println("IOException");
				} catch (InterruptedException e) {
					System.err.println("InterruptedException");
				} 
			}
			
			catch (org.jsoup.HttpStatusException e) {
				System.err.println("HttpStatusException");
			} catch (IOException e) {
				System.err.println("IOException");
			} catch (InterruptedException e) {
				System.err.println("InterruptedException");
			}
			
		}

	
	}
	
	/**
	 * Crawls the video links from a topic page: 1) from the text itself, 2) from under the section: "Watch videos about...(topic)"
	 */
	public void crawlLinkedVideoLinks(){
		// crawl the video links from the text
		this.crawlVideoLinksFromText();
		// crawl the video links from related videos
		this.crawlLinkedVideoLinksFromRelated();
		// at the end all are joined to a string
		String videolinks = String.join(",", this.getLinkedVideosSet());
		this.setVideolinks(videolinks);
		
	}
	
	/**
	 * Crawls all kinds of query links from a single topic page: implementation of the abstract method from QueryPageCrawler.
	 */
	@Override
	public void crawlLinkedQueryLinks() {
		this.crawlLinkedArticleLinks();
		this.crawlLinkedQuestionLinks();
		this.crawlLinkedVideoLinks();
		this.crawlLinkedTopicLinks();

	}
	/**
	 * Crawls linked article links on a topic page: implementation of the abstract method from QueryPageCrawler
	 */
	@Override
	public void crawlLinkedArticleLinks(){
		Document doc = this.getHtml();
		Elements references = doc.select("a[href]");
		Integer refSize = references.size();
		// this loop gets the video Links from the text
		for(int i = 0; i < refSize; i++){
			String link = references.get(i).absUrl("href");
			if(QueryPageCrawler.isArticleLink(link)){
				this.getLinkedArticlesSet().add(link);
			}
	}		
			String articlelinks = String.join(",", this.getLinkedArticlesSet());
			this.setArticlelinks(articlelinks);
	}
	
	/**
	 * Crawls the linked documents from a topic page if there are any.
	 */
	@Override
	public void crawlDocumentLinks() {
		Document doc = this.getHtml();
		Element divTags = doc.getElementsByAttributeValue("class", "entry-content").get(0);
		Elements linksInContent = divTags.select("a[href]");
		for(Element link: linksInContent){
			String li = link.absUrl("href");
			// if the link is an absolute url, then it is an external page -> document
			if(QueryPageCrawler.isUrl(li)) {
				this.getLinkedDocumentsSet().add(li);
			}	
		}
		String doclinks = String.join(",", this.getLinkedDocumentsSet());
		this.setDoclinks(doclinks);
	}
	
	/**
	 * Crawls the question links from a topic page.
	 */
	@Override
	public void crawlLinkedQuestionLinks(){
		Document doc = this.getHtml();
		Elements links = doc.select("a[href]");
		for(Element link: links){
			if(QueryPageCrawler.isQuestionLink(link.absUrl("href"))){
				this.getLinkedQuestionsSet().add(link.absUrl("href"));
			}
		}
		String questions = String.join(",", this.getLinkedQuestionsSet());
		this.setTopics(questions);
		
	}
	
	/**
	 * Crawls related topics from the sidebar that contains only topics.
	 */
	public void crawlTopicsFromRelated() {
		ArrayList<String> topiclist = new ArrayList<String>();
		// the related topics 
		Element tags_element = this.getHtml().getElementsByAttributeValue("class", "small-12 columns content-container-sidebar related-topics").get(0);
		
		if (tags_element == null) {
			this.setTopics("-");
		} else {
			for (Element topic: tags_element.getElementsByTag("li")){
				topiclist.add(topic.text());
			}
			String tags = String.join(",", topiclist);
			this.setTopics(tags);	
		}
	}
	
	/**
	 * Crawls the linked topic links from the text of a topic page.
	 */
	public void crawlLinkedTopicLinks(){
		Elements queryReferences = this.getHtml().select("a");
		int size = queryReferences.size();
		for (int i = 0; i<size-1; i++){
			String rLink = queryReferences.get(i).absUrl("href");
			// this is a reference to the general topics page
			if(QueryPageCrawler.isTopicLink(rLink)){
				this.getLinkedTopicsSet().add(rLink);
			}
		}
	}
	
	
	/**
	 * Writes the content of a topic page to the dump. All of the empty elements/ elements not present on a topic page (e.g. comments) are replaced with
	 * "-" and processed accordingly after the dump is written.
	 */
	public void write(){
		String[] components = {
				String.valueOf(this.getQueryId()), 
				this.getQuerylink(), 
				this.getTitle(), 
				this.getText(), 
				"-",  // empty comments 
				this.getTopics(),  // empty topics 
				"-",  // empty description
				"-", // empty Doctor's Note
				this.getArticlelinks(), 
				this.getQuestionlinks(),
				this.getTopiclinks(),
				this.getVideolinks(), 
				this.getDoclinks()};
		
		String line = "";
		for (String component: components){
			// a component could also be without content, e.g. a topic page doesn't have any text.
			if(component == null || component.isEmpty()){
				line += "-" + "\t";
			} else {
				line += component + "\t";
			}
		}
		Writer.appendLineToFile(line, Properties.PATHS_TO_QUERYDUMPS[3]);
		String forecast_line = components[0] + "\t" + components[0].split("-")[0] + "\t" + components[0].split("-")[1] + "\t" + this.getDate();
		io.Writer.appendLineToFile(forecast_line, Properties.PATH_TO_QUERY_FORECAST);
}
	
	/**
	 * Crawls the content of a topic page after setting up a connection.
	 */
	@Override
	public void crawlQueryPage() {
		this.setConnection(this.getQuerylink());
		this.crawlTitle();
		this.crawlCreationDate();
		this.crawlText();
		this.crawlLinkedQueryLinks();
		this.crawlTopicsFromRelated();
		this.crawlDocumentLinks();
		
	}
	
	public static String getTopicMarkup() {
		return TOPIC_MARKUP;
	}
	
	public Set<String> getLinkedArticlesSet() {
		return linkedArticlesSet;
	}

	public void setLinkedArticlesSet(Set<String> linkedArticlesSet) {
		this.linkedArticlesSet = linkedArticlesSet;
	}

	public Set<String> getLinkedVideosSet() {
		return linkedVideosSet;
	}

	public void setLinkedVideosSet(Set<String> linkedVideosSet) {
		this.linkedVideosSet = linkedVideosSet;
	}

	public Set<String> getLinkedDocumentsSet() {
		return linkedDocumentsSet;
	}

	public void setLinkedDocumentsSet(Set<String> linkedDocumentsSet) {
		this.linkedDocumentsSet = linkedDocumentsSet;
	}

	public Set<String> getLinkedTopicsSet() {
		return linkedTopicsSet;
	}

	public void setLinkedTopicsSet(Set<String> linkedTopicsSet) {
		this.linkedTopicsSet = linkedTopicsSet;
	}

	public Set<String> getLinkedQuestionsSet() {
		return linkedQuestionsSet;
	}

	public void setLinkedQuestionsSet(Set<String> linkedQuestionsSet) {
		this.linkedQuestionsSet = linkedQuestionsSet;
	}

	public String getTopics() {
		return topics;
	}

	public void setTopics(String topics) {
		this.topics = topics;
	}
}
