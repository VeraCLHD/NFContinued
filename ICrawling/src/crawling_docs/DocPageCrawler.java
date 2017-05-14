/**
 * 
 */
package crawling_docs;

import io.Writer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import run.createQCLIRCorpus;

/**
 * A low level class that handles a single link without dependencies to other links. 
 * The content of this link is crawled as html or pdf.
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */
public abstract class DocPageCrawler {
	private Document html_content = null;
	private String mimeType = "";
	private String linkToSite;
	private String domain;
	private boolean hasContent;
	private String errorsOccuredDuringCrawling;
	private String pdf_link;
	
	//variables for content components: text, doi, title
	private String journal_name = "";
	private String references ="";
	private String abstract_article = "";
	private String text = "";
	private String doi ="";
	private String title ="";
	
	private String free_pmc_link;
	private ArrayList<String> full_text_links;
	
	public DocPageCrawler(String link) throws ExpectedConnectionException {
		this.setLinkToSite(link);
		this.extractDomain();
		createQCLIRCorpus.getDelay_manager().delay(domain);
		this.setConnectionAndGetHTML(link);
	}
	
	/**
	 * Crawls the content of any scientific document page: implemented by the special crawlers (e.g. pdf, pmc etc.)
	 */
	public abstract void crawlContent();
	
	public abstract void crawlAbstractOnly();
	
	
	public boolean hasHTML() {
		Document hmtl = this.getHTMLContent();
		return !((hmtl == null));
	}
	
	public boolean hasFreePMCLink() {
		String link = this.getFree_pmc_link();
		return !((link == null) || (link.isEmpty()));
	}
	
	public boolean hasFullTextLink() {
		ArrayList<String> links = this.getFull_text_links();
		return !((links == null) || (links.isEmpty()));
	}
	
	public boolean hasDOI() {
		String doi = this.getDoi();
		return !((doi == null) || (doi.isEmpty()));
	}
	
	public boolean hasTitle() {
		String title = this.getTitle();
		return !((title == null) || (title.isEmpty()));
	}

	public boolean hasAbstract() {
		String abs = this.getAbstract_article();
		return !(abs == null || abs.isEmpty());
	}

	public boolean hasText(){
		String text = this.getText();
		return !(text == null || text.isEmpty());
	}
	
	/**
	 * connects to any url and downloads the html content.
	 * @param link the link to which the connection is build
	 * @throws ExpectedConnectionException Exception for expected errors during crawling, e.g. HttpStatusException, MimeTypeException, etc.
	 */
	public void setConnectionAndGetHTML(String link) throws ExpectedConnectionException {
		Document doc = null;
		try {
			for (int i = 0; i < DocProperties.MAX_CONNECTION_TRIALS; i++) {
				try {
				Connection connection = Jsoup.connect(link).userAgent(DocProperties.USER_AGENT).followRedirects(true).timeout(DocProperties.TIMEOUT);
				doc = connection.get();
				break;
				} catch (java.net.SocketTimeoutException e) {}
			}
		} catch (org.jsoup.UnsupportedMimeTypeException e){
			this.setMimeType(e.getMimeType());
			System.err.println("MimeTypeException: setConnectionAndGetHTML (DocPageCrawler)" + " " + "LINK: " + link);
		} catch (org.jsoup.HttpStatusException | java.net.UnknownHostException | java.net.MalformedURLException e) {
			System.err.println("HttpStatusException: connection error in setConnectionAndGetHTML(DocPageCrawler);  LINK: " + this.getLinkToSite());
			// if the page is not found (error 404 or any other connection error) -> a sign that this document is not usable:
			// we can throw it away before writing in the doc dump
			this.setErrorsOccuredDuringCrawling(this.getErrorsOccuredDuringCrawling() + "\t" + "link: " + this.getLinkToSite() + "\t" + "StatusException");
			throw new ExpectedConnectionException();
		} catch (IOException e) {
			System.err.println("IOException");
		}
		this.setHTMLContent(doc);
	}
		
	public void collectErrors() {	
		String error_log = this.getLinkToSite() + "\t";
		boolean errors_occurred = false;
		Boolean[] booleans = {this.hasDOI(), this.hasTitle(), this.hasAbstract(), this.hasText()};
		String[] components = {"DOI", "title", "abstract", "full text"};
		for (int i = 0; i < components.length; i++) {
			if (booleans[i] == false) {
				errors_occurred = true;
				error_log += "no " + components[i] + "\t"; 
			}
		}
		if (errors_occurred == true) {
			this.setErrorsOccuredDuringCrawling(error_log);	
		}
	}
	
	public static boolean isNCBILink(String link){
		Matcher m = DocProperties.NCBI_PAGE_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;	
	}
	
	public static boolean isPMCLink(String link) {
		Matcher m = DocProperties.PMC_ARTICLE_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;
	}
	
	
	/**
	 * crawls the title from a journal page by getting the text of the title tag
	 */
	public void crawlTitleFromJournalPage(){
		Document doc = this.getHTMLContent();
		Elements title = doc.getElementsByTag("title");
		this.setTitle(title.text());
	}
	
	/**
	 * crawls the abstract from a journal page by getting the div tags with attributes matching a regular expression for the abstract.
	 * Can be used for NCBI as well as NonNCBIPages
	 */
	public void crawlAbstract(){
		Document doc = this.getHTMLContent();
		// abs as attribute value of id: missing completely -> check if really necessary
			Elements abstract_article = doc.select("div[id~=.*Abs.*|.*abstract.*|.*Abstract.*], div[class~=.*abstract.*|.*Abstract.*]");
			Elements content_els = abstract_article.select("h0, h1, h2, h3, h4, h5, h6, p, p[class]");
			content_els = DocPageCrawler.removeReferences(content_els);
			if(!content_els.isEmpty()){
				this.setAbstract_article(content_els.text());
			}
		}
		

	/**
	 * Finds the doi (Digital Object Identifier) of the article, if available. 
	 */
	
	public void crawlDOI(){
		Document doc = this.getHTMLContent();
		try{
			String doi = doc.select("meta[name~=.*doi.*| .*citation_doi.*|.*identifier.*]").first().getElementsByAttribute("content").attr("content");
			doi = doi.replaceAll("doi\\:?", "");
			this.setDoi(doi);
		}
		
		catch(NullPointerException nullpointer){
			System.err.println("NullPointerException: No DOI in crawlDOI (DocPageCrawler)");
			this.setErrorsOccuredDuringCrawling(this.getErrorsOccuredDuringCrawling() + "\t" + this.getLinkToSite() + "\t" + "crawlDOI" + "\t" + "no doi" +"\r\n");
		}
			
	}
	
	
	public boolean hasPDFLink() {
		String links = this.getPdf_link();
		return !(links == null || links.isEmpty());
	}
	
	/**
	 * Extracts the domain from the initial link. Used for tracking the delay within a domain when two or more pages of the same domain
	 * are crawled after one another.
	 * @see DelayManager
	 */
	public void extractDomain() {
		String link = this.getLinkToSite();
		String domain = "http://" + link.split("/")[2];
		this.setDomain(domain);
	}
	
	/**
	 * Crawls the pdf link of an initial link.
	 * @see PDFHandler
	 * @throws ExpectedConnectionException Exception for expected errors during crawling, e.g. HttpStatusException, MimeTypeException, etc.
	 */
	public void crawlPDFLink() throws ExpectedConnectionException {
		this.setPdf_link(PDFHandler.crawlPDFLink(this));
	}
	
	/**
	 * Crawles and saves the text from a pdf link using two different methods: online or locally.
	 */
	public void crawlTextFromPDF() throws ExpectedConnectionException{
		String text = PDFCrawler.crawlTextFromPDFLink(this);
		if(text.isEmpty()){
			PDFCrawler.getInputStreamFromLink(this);
			text = PDFCrawler.readStreamAsPDF();
		}
		this.setText(text);
		DocCrawlerFullTextMode.setNumSuccessfulPDFs(DocCrawlerFullTextMode.getNumSuccessfulPDFs() +1 );
	}
	
	/**
	 * Strips the references of the text of a journal article: they can be seen as less relevant than the actual content
	 * @param elements the Elements (as jsoup Object) of an html page
	 * @return
	 */
	public static Elements removeReferences(Elements elements) {
		Elements result_elements = new Elements();
		for (Element e: elements) {
			Elements e_and_parents = e.parents();
			e_and_parents.add(e);
			if (e_and_parents.not(DocProperties.EXCLUDE_REFERENCES).size() == e_and_parents.size()) {
				result_elements.add(e);
			} else {
				
				
			}
		}
		return result_elements;
	}

	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public String getDoi() {
		return doi;
	}


	public void setDoi(String doi) {
		this.doi = doi;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getReferences() {
		return references;
	}


	public void setReferences(String references) {
		this.references = references;
	}
	
	/**
	 * sets the hasContent variable to false if the no content could be crawled and to true otherwise
	 */
	
	public void containsContent(){
		String abs = this.getAbstract_article();
		String text = this.getText();
		if( (abs == null || abs.isEmpty()) && (text == null || text.isEmpty()) ){
			this.setHasContent(false);
		}
		else{
			this.setHasContent(true);
		}
	}

	public String getAbstract_article() {
		return abstract_article;
	}


	public void setAbstract_article(String abstract_article) {
		this.abstract_article = abstract_article;
	}


	public String getJournal_name() {
		return journal_name;
	}


	public void setJournal_name(String journal_name) {
		this.journal_name = journal_name;
	}


	public boolean isHasContent() {
		return hasContent;
	}


	public void setHasContent(boolean hasContent) {
		this.hasContent = hasContent;
	}
	
	public Document getHTMLContent() {
		return html_content;
	}

	public void setHTMLContent(Document content) {
		this.html_content = content;
	}
	
	public String getLinkToSite() {
		return linkToSite;
	}

	public void setLinkToSite(String linkToSite) {
		this.linkToSite = linkToSite;
	}


	public String getErrorsOccuredDuringCrawling() {
		return errorsOccuredDuringCrawling;
	}


	public void setErrorsOccuredDuringCrawling(
			String errorsOccuredDuringCrawling) {
		this.errorsOccuredDuringCrawling = errorsOccuredDuringCrawling;
	}


	public String getPdf_link() {
		return pdf_link;
	}


	public void setPdf_link(String pdf_links) {
		this.pdf_link = pdf_links;
	}


	public String getDomain() {
		return domain;
	}


	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getFree_pmc_link() {
		return free_pmc_link;
	}

	public void setFree_pmc_link(String free_pmc_link) {
		this.free_pmc_link = free_pmc_link;
	}

	public ArrayList<String> getFull_text_links() {
		return full_text_links;
	}

	public void setFull_text_links(ArrayList<String> full_text_links) {
		this.full_text_links = full_text_links;
	}
	

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public static void main(String[] args) throws ExpectedConnectionException {
		DocPageCrawler doc = DocPageCrawlerFactory.createCrawler("http://www.ncbi.nlm.nih.gov/pubmed/484528");
		doc.crawlAbstract();
		System.out.println(doc.getAbstract_article());
	}

}
