package crawling_queries;

import io.Writer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * This class is responsible for crawling the relevant data of one blog article page from nutritionfacts.org
 * and writing them into the dump file.
 * 
 * Crawled data: 
 * - (own link)
 * - title
 * - transcript
 * - topics(tags)
 * - comments
 * - description
 * - doctor's note
 * - links to other articles, videos, question, topic pages
 * - other links
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class VideoPageCrawler extends QueryPageCrawler {
	

	private static final String VIDEO_MARKUP = "V-";
	
	private String description;
	private Element doctorsNoteAsHtml;
	private String doctorsNoteAsString;
	private String topics;
	private Set<String> linkedArticlesSet = new HashSet<String>();
	// if this list is empty at the end, then there weren't any direct links from a video to an article
	private Set<String> linkedVideosSet = new HashSet<String>();
	private Set<String> linkedQuestionsSet = new HashSet<String>();
	private Set<String> linkedTopicsSet = new HashSet<String>();
	private Set<String> linkedDocumentsSet = new HashSet<String>();
	
	/**
	 * Constructor: calls the constructor of a QueryPageCrawler and takes care of giving the Video Page a suitable id.
	 * @param querylink String a link to a video page
	 */
	public VideoPageCrawler(String querylink) {
		super(querylink);
		NFCrawler.setId(NFCrawler.getId()+1);
		this.setQueryId(VideoPageCrawler.getVideoMarkup() +  NFCrawler.getId());
	}
	
	/**
	 * Crawls the content of a video page after setting up a connection to it. Calls all the other crawling methods.
	 */
	@Override
	public void crawlQueryPage() {
		this.setConnection(this.getQuerylink());
		this.crawlTitle();
		this.crawlCreationDate();
		this.crawlDescription();
		this.crawlTranscript();
		this.crawlDoctorsNote();
		this.crawlComments();
		this.crawlTopics();
		this.crawlLinkedQueryLinks();
		this.crawlDocumentLinks();
		this.crawlLinkedTopicLinks();
		
	}
	
	/**
	 * Writes the content of a video page to the dump. All of the empty elements/ elements not present on a video page (e.g. comments) are replaced with
	 * "-" and processed accordingly after the dump is written.
	 */
	@Override
	public void write(){
			String[] components = {
					String.valueOf(this.getQueryId()), 
					this.getQuerylink(), 
					this.getTitle(), 
					this.getText(), 
					this.getComments(), 
					this.getTopics(), 
					this.getDescription(), 
					this.getDoctorsNoteAsString(), 
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
			Writer.appendLineToFile(line, Properties.PATHS_TO_QUERYDUMPS[4]);
			String forecast_line = components[0] + "\t" + components[0].split("-")[0] + "\t" + components[0].split("-")[1] + "\t" + this.getDate();
			io.Writer.appendLineToFile(forecast_line, Properties.PATH_TO_QUERY_FORECAST);
	}

	/**
	 * Crawls the short description of a video.
	 * @param
	 */
	public void crawlDescription(){
		Document doc = this.getHtml();
		Elements metaelements = doc.select("meta[name=description]");
		if (metaelements == null) {
			this.setDescription("-");
		} else {
			String description = metaelements.first().attr("content").trim().toString();
			this.setDescription(description);	
		}
	}
	
	/**
	 * Crawls the transcript of a nutrition video
	 */
	public void crawlTranscript(){
		String transcript = "";
		Document doc = this.getHtml(); 
		if (doc.getElementById("transcript") == null) {
			this.setText("-");
		} else {
			Elements elementsOfTranscript = doc.getElementById("transcript").getElementsByTag("p");
			int lengthElements = elementsOfTranscript.size();
			for(int i = 1; i<lengthElements-3; i++){
				transcript += elementsOfTranscript.get(i).text();
			}
			
			this.setText(transcript.replaceAll("\\s+", " "));	
		}
	}
	
	/**
	 * Crawls the text of the Doctor's Note on a video page.
	 */
	public void crawlDoctorsNote(){
		Document doc = this.getHtml();
		String doctorsNote = "";
		// in the current structure the tag with id=content has the children that contain the Doctor's Note and comments
		Element elementsOfContent = doc.getElementById("content");
		if (elementsOfContent == null) {
			this.setDoctorsNoteAsString("-");
		} else {
			Elements elementsOfDN = elementsOfContent.child(0).getElementsByTag("p");
			int lengthElements = elementsOfDN.size();
			// the last of the children -> paragraphs is a subscription invitation, which we don't need.
			for(int i = 0; i<lengthElements-1; i++){
				doctorsNote += elementsOfDN.get(i).text();
			}
			this.setDoctorsNoteAsString(doctorsNote.replaceAll("\\s+", " "));
			this.setDoctorsNoteAsHtml(elementsOfContent.child(0));
		}
	}
	
	/**
	 * Crawls the links of related videos from the doctor's note
	 */
	@Override
	public void crawlLinkedVideoLinks() {
		Elements queryReferences = this.getDoctorsNoteAsHtml().select("a");
		int size = queryReferences.size();
		for (int i = 0; i<size-1; i++){
			String rLink = queryReferences.get(i).absUrl("href");
			if(QueryPageCrawler.isVideoLink(rLink)){
				this.getLinkedVideosSet().add(rLink);
			}
		}
		String videolinks = String.join(",", this.getLinkedVideosSet());
		this.setVideolinks(videolinks);
	}
	
	/**
	 * Crawls the links of related articles from the Doctor's note.
	 */
	@Override
	public void crawlLinkedArticleLinks() {
		Elements queryReferences = this.getDoctorsNoteAsHtml().select("a");
		int size = queryReferences.size();
		for (int i = 0; i<size-1; i++){
			String rLink = queryReferences.get(i).absUrl("href");
			if(QueryPageCrawler.isArticleLink(rLink)){
				this.getLinkedArticlesSet().add(rLink);
			}
		}
		String articlelinks = String.join(",", this.getLinkedArticlesSet());
		this.setArticlelinks(articlelinks);
	}
	
	/**
	 * Crawls the related question links from a Doctor's Note
	 */
	@Override
	public void crawlLinkedQuestionLinks() {
		Elements queryReferences = this.getDoctorsNoteAsHtml().select("a");
		int size = queryReferences.size();
		for (int i = 0; i<size-1; i++){
			String rLink = queryReferences.get(i).absUrl("href");
			if(QueryPageCrawler.isQuestionLink(rLink)){
				this.getLinkedQuestionsSet().add(rLink);
			}
		}
		
		String questionlinks = String.join(",", this.getLinkedQuestionsSet());
		this.setQuestionlinks(questionlinks);
	}
	
	
	/**
	 * Crawls the links to topics from the whole document -> could be present in the doctor's note but are always present in the section topics.
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
		
		String topiclinks = String.join(",", this.getLinkedTopicsSet());
		this.setTopiclinks(topiclinks);
		
	}
	
	
	/**
	 * Crawls the links to queries from a video page
	 */
	public void crawlLinkedQueryLinks(){
		this.crawlLinkedArticleLinks();
		this.crawlLinkedQuestionLinks();
		this.crawlLinkedVideoLinks();
		this.crawlLinkedTopicLinks();
	}
	
	/**
	 * Crawls the links to scientific documents from a video page: from the section "cited sources" and from the Doctor's note.
	 */
	public void crawlDocumentLinks(){
		Document doc = this.getHtml();
		Elements cited = doc.getElementsByTag("cite");
		//crawl document links from cited sources
		for(Element citedElement: cited){
			for(Element reference: citedElement.select("a")){
				if(reference != null){
					this.getLinkedDocumentsSet().add(reference.absUrl("href"));
				}
			}
		}  
		
		Elements references = this.getDoctorsNoteAsHtml().select("a");
		int size = references.size();
		
		// crawl document links from doctors note if any
		for (int i = 0; i<size-1; i++){
			String rLink = references.get(i).absUrl("href");
			if (QueryPageCrawler.isUrl(rLink)) {
				this.getLinkedDocumentsSet().add(rLink);
			}
		}
		
		String doclinks = String.join(",", this.getLinkedDocumentsSet());
		this.setDoclinks(doclinks);
	}
	
	/**
	 * Crawls the topics as text from the topics section.
	 */
	public void crawlTopics() {
		ArrayList<String> topiclist = new ArrayList<String>();
		Element tags_element = this.getHtml().getElementsByAttributeValue("class", "inline-list").get(0);
		if (this.getHtml().getElementsByAttributeValue("class", "inline-list") == null) {
			this.setTopics("-");
		} else {
			for (Element e: tags_element.getElementsByAttributeValue("rel", "tag")){
				topiclist.add(e.text());
			}
			String tags = String.join(",", topiclist);
			this.setTopics(tags);	
		}
	}

	
	public static void main(String[] args) {
		VideoPageCrawler vpc = new VideoPageCrawler("http://nutritionfacts.org/video/peppermint-oil-for-irritable-bowel-syndrome/");
		vpc.setConnection(vpc.getQuerylink());
		vpc.crawlQueryPage();
	}
	

	public static String getVideoMarkup() {
		return VIDEO_MARKUP;
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
	
	public Set<String> getLinkedArticlesSet() {
		return linkedArticlesSet;
	}

	public void setLinkedArticlesSet(Set<String> linkedArticles) {
		this.linkedArticlesSet = linkedArticles;
	}

	public Set<String> getLinkedVideosSet() {
		return linkedVideosSet;
	}

	public void setLinkedVideosSet(Set<String> linkedVideos) {
		this.linkedVideosSet = linkedVideos;
	}

	public Set<String> getLinkedDocumentsSet() {
		return linkedDocumentsSet;
	}

	public void setLinkedDocumentsSet(Set<String> linkedDocuments) {
		this.linkedDocumentsSet = linkedDocuments;}
	
	public String getDoctorsNoteAsString() {
		return doctorsNoteAsString;
	}

	public void setDoctorsNoteAsString(String doctorsNoteAsString) {
		this.doctorsNoteAsString = doctorsNoteAsString;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Element getDoctorsNoteAsHtml() {
		return doctorsNoteAsHtml;
	}

	public void setDoctorsNoteAsHtml(Element doctorsNoteAsHtml) {
		this.doctorsNoteAsHtml = doctorsNoteAsHtml;
	}
	
	public String getTopics() {
		return topics;
	}

	public void setTopics(String topics) {
		this.topics = topics;
	}
	
	
	
}
