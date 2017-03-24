package crawling_queries;

import java.util.ArrayList;
import java.util.HashSet;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * This class is responsible for crawling the relevant data of an "Ask the Doctor" or "Ask the Dietritian" page from nutritionfacts.org
 * and writing them into the dump file.
 * 
 * Crawled data: 
 * - (own link)
 * - title
 * - text
 * - comments
 * - links to other articles, videos, question pages
 * - other links
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */

public class QuestionPageCrawler extends QueryPageCrawler {
	
	
	/**
	 * A singleton constructor
	 * @param querylink the link from which the content is crawled
	 */
	public QuestionPageCrawler(String querylink) {
		super(querylink);
		NFCrawler.setId(NFCrawler.getId()+1);
		if(QueryPageCrawler.isQuestionDoctorLink(querylink)){
			this.setQueryId("Q_DOC-" +  NFCrawler.getId());
		}
		else if(QueryPageCrawler.isQuestionDietitianLink(querylink)){
			this.setQueryId("Q_DIET-" +  NFCrawler.getId());
		}
	}

	public void crawlText() {
		if (this.getHtml().getElementsByAttributeValue("class", "entry-content") == null) {
			this.setText("-");
		} else {
			String text = "";
			Elements els = this.getHtml().getElementsByAttributeValue("class", "entry-content").get(0).getElementsByTag("p");
			// last element irrelevant
			for (Element e: els.subList(0, els.size()-1)) {
				text += e.text() + " ";
			}
			this.setText(text.replaceAll("\\s+", " "));			
		}
	}
	
	@Override
	public void crawlLinkedVideoLinks() {
		HashSet<String> videolinks_set = new HashSet<String>();
		Elements links = this.getHtml().getElementsByAttributeValue("class", "entry-content").select("p").select("a[href]");
		if (links.isEmpty()) {return;}
		for (Element link_element : links.subList(0, links.size()-1)) {
			String link = link_element.attr("href");
			if (QueryPageCrawler.isVideoLink(link) == true) {
				videolinks_set.add(link);
			}
		}
		
		ArrayList<String> videolinks_list = new ArrayList<String>(videolinks_set);
		String videolinks = String.join(",", videolinks_list);
		this.setVideolinks(videolinks);
	}

	@Override
	public void crawlLinkedArticleLinks() {
		HashSet<String> articlelinks_set = new HashSet<String>();
		Elements links = this.getHtml().getElementsByAttributeValue("class", "entry-content").select("p").select("a[href]");
		if (links.isEmpty()) {return;}
		for (Element link_element : links.subList(0, links.size()-1)) {
			String link = link_element.attr("href");
			if (QueryPageCrawler.isArticleLink(link) == true) {
				articlelinks_set.add(link);
			} 
		}
		
		ArrayList<String> articlelinks_list = new ArrayList<String>(articlelinks_set); 
		String articlelinks = String.join(",", articlelinks_list);
		this.setArticlelinks(articlelinks);
	}

	@Override
	public void crawlLinkedQuestionLinks() {
		HashSet<String> questionlinks_set = new HashSet<String>();
		// not searching links in the whole html, only in the text
		Elements links = this.getHtml().getElementsByAttributeValue("class", "entry-content").select("p").select("a[href]");
		if (links.isEmpty()) {return;}
		for (Element link_element : links.subList(0, links.size()-1)) {
			String link = link_element.attr("href");
			 if (QueryPageCrawler.isQuestionLink(link) == true) {
				questionlinks_set.add(link);
			}
		}

		ArrayList<String> questionlinks_list = new ArrayList<String>(questionlinks_set);
		String questionlinks = String.join(",", questionlinks_list);
		this.setQuestionlinks(questionlinks);
		
	}
	
	
	public void crawlLinkedTopicLinks() {
		HashSet<String> topiclinks_set = new HashSet<String>();
		// not searching links in the whole html, only in the text
		Elements links = this.getHtml().getElementsByAttributeValue("class", "entry-content").select("p").select("a[href]");
		if (links.isEmpty()) {return;}
		for (Element link_element : links.subList(0, links.size()-1)) {
			String link = link_element.attr("href");
			 if (QueryPageCrawler.isTopicLink(link) == true) {
				 topiclinks_set.add(link);
			}
		}

		ArrayList<String> topiclinks_list = new ArrayList<String>(topiclinks_set);
		String topiclinks = String.join(",", topiclinks_list);
		this.setTopiclinks(topiclinks);
		
	}
	
	@Override
	public void crawlLinkedQueryLinks() {
		this.crawlLinkedArticleLinks();
		this.crawlLinkedQuestionLinks();
		this.crawlLinkedTopicLinks();
		this.crawlLinkedVideoLinks();
	}
	
	@Override
	public void crawlDocumentLinks() {
		HashSet<String> doclinks_set = new HashSet<String>();
		Elements links = this.getHtml().getElementsByAttributeValue("class", "entry-content").select("p").select("a[href]");
		if (links.isEmpty()) {return;}
		for (Element link_element : links.subList(0, links.size()-1)) {
			String link = link_element.attr("href");
			if (QueryPageCrawler.isUrl(link)){
				doclinks_set.add(link);
			}
		}
		ArrayList<String> doclinks_list = new ArrayList<String>(doclinks_set);
		String doclinks = String.join(",", doclinks_list);
		this.setDoclinks(doclinks);
	}


	@Override
	public void crawlQueryPage() {
		
		this.setConnection(this.getQuerylink());
		this.crawlTitle();
		this.crawlCreationDate();
		this.crawlText();
		this.crawlComments();
		this.crawlLinkedQueryLinks();
		this.crawlDocumentLinks();
		
	}
	
	@Override
	public void write() {
		String[] components = {
				String.valueOf(this.getQueryId()), 
				this.getQuerylink(), 
				this.getTitle(), 
				this.getText(), 
				this.getComments(), 
				"-", // empty topics 
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
		/*if (QueryPageCrawler.isQuestionDoctorLink(this.getQuerylink())) {
			io.Writer.appendLineToFile(line, Properties.PATHS_TO_QUERYDUMPS[2]);
		}else  if(QueryPageCrawler.isQuestionDietitianLink(this.getQuerylink())){
			io.Writer.appendLineToFile(line, Properties.PATHS_TO_QUERYDUMPS[1]);
		}*/
		
		String forecast_line = components[0] + "\t" + components[0].split("-")[0] + "\t" + components[0].split("-")[1] + "\t" + this.getDate();
		io.Writer.appendLineToFile(forecast_line, Properties.PATH_TO_QUERY_FORECAST);
	}

	
}
