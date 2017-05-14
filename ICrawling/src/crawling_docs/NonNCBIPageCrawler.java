/**
 * 
 */
package crawling_docs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.*;
/**
 * 
 * A class that downloads the content of a non NCBI page (most frequently: a page that is referred by NCBI as a full text link).
 * In most cases when a referenced document is found, it is accessible in pdf form as well.
 * <p>
 * Serves the rare case of downloading the abstract and full text of such a page that doesn't provide a pdf.
 * 
 * @author Vera Boteva, Demian Gholipour
 */
public class NonNCBIPageCrawler extends DocPageCrawler {
	
	/**
	 * Constructor: sets the connection and the link
	 * @param link the link to the document
	 */
	public NonNCBIPageCrawler(String link)  throws ExpectedConnectionException {
		super(link);
		
	}
	
	/**
	 * Crawls the content of a non-ncbi page: doi, title, abstract, text and collects any errors that occured while crawling.
	 */
	@Override
	public void crawlContent() {
		if (this.hasHTML()) {
			this.crawlDOI();
			this.crawlTitleFromJournalPage();
			this.crawlAbstract();
			this.crawlTextFromJournalPage();
			this.collectErrors();
		} else {
			this.setErrorsOccuredDuringCrawling(getErrorsOccuredDuringCrawling() + "\t" + "link:" + "\t" + this.getLinkToSite() + "no HTML"); 
		}
	}
	
	/**
	 * crawls the article text including title, abstract, text from a journal page
	 */
	public void crawlTextFromJournalPage(){
		Document doc = this.getHTMLContent();
		try{
			Elements contentDiv = doc.select("div[id~=.*content.*|.*Content.*], div[class~=.*JournalFullText.*|.*fulltext.*|.*content.*|.*Content.*]");
			
			Elements content_els = contentDiv.select("h0, h1, h2, h3, h4, h5, h6, p, p[class]");
			content_els = DocPageCrawler.removeReferences(content_els);
			// from the div + id=content element, we select all h1...h4 tags as well as the paragraph tags
			if(!content_els.isEmpty()){
				this.setText(content_els.text());
			}		
		}		
		catch(NullPointerException nullptr){
			nullptr.printStackTrace();
		}	
	}
	
	public void crawlJournalName(){
		Document doc = this.getHTMLContent();
		String journal_name = doc.select("meta[name~=.*journal.*]").first().getElementsByAttribute("content").attr("content");
		this.setJournal_name(journal_name);
	}
	

	/**
	 * Crawls the references from a journal page separately.
	 * @deprecated
	 */
	public void crawlReferencesFromJournalPage(){
		
		Document doc = this.getHTMLContent();
		try{
			Elements abstract_article = doc.select("div[id~=.*references.*|.*References.*], div[class~=.*ref.*|.*references.*|.*References.*]");
			for(Element el: abstract_article){
				if(el.text() != null || !el.text().isEmpty()){
					this.setReferences(this.getReferences() + el.text() + "\r\n");
				}
			}
		}
		
		catch(NullPointerException nullpointer){
			nullpointer.printStackTrace();
			this.setErrorsOccuredDuringCrawling(this.getErrorsOccuredDuringCrawling() + "\t" + this.getLinkToSite() + "\t" + "crawlReferencesFromJournalPage" + "\r\n");
		}
	}

	@Override
	public void crawlAbstractOnly() {
		// dummy, not yet relevant
		
	}
	

}
