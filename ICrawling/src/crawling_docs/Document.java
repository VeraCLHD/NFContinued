/**
 * 
 */
package crawling_docs;

import java.util.ArrayList;
import java.util.List;

import io.Writer;
import crawling_queries.Properties;

/**
 * A class that assembles the document from all its different sources - an NCBI summary page, a pdf, a NONNCBI Page etc.
 * Due to the high variety of crawling paths and pages, it is often the case that throughout these paths, different texts for
 * the same scientific documents are saved. This class is responsible for deciding how the final document will look like.
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class Document {
	private String doi;
	private String abstr;
	// initial link from the query page
	private String linkToDocument;
	private String title;
	private String text;
	private final Integer minFullTextSize = 600; 
	
	
	public Document(String initial_link) {
		this.setLinkToDocument(initial_link);
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public String getLinkToDocument() {
		return linkToDocument;
	}

	public void setLinkToDocument(String linkToDocument) {
		this.linkToDocument = linkToDocument;
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

	public String getAbstr() {
		return abstr;
	}

	public void setAbstr(String abstr) {
		this.abstr = abstr;
	}
	
	/**
	 * decides if a text crawled from a non-ncbi page/ full text link has the sufficient length to be a credible text.
	 * For example, sometimes the access to an article is denied, so the text that is supposed to be the article looks like this:
	 * "Subscribe to get access to the full text.". This method prohibits such texts of becoming a full text written in the doc_dump.
	 * <p>
	 * A text can be taken into account only if it is longer than the abstract or if there is no abstract, longer than a minimum length of 600 characters.
	 * 
	 * @param abstr the abstract text
	 * @param text the possible full text
	 * @return false, if the text length is not sufficient; true otherwise
	 */
	public boolean isSufficientTextSize(String abstr, String text){
		Integer text_len = text.length();
		if (abstr == null || abstr.isEmpty()) {
			return (text_len >= this.getMinFullTextSize());
		}
		else{
			Integer abstr_len = abstr.length();
			return (text_len > abstr_len || text_len >= this.getMinFullTextSize());
		}
		
	}
	
	/**
	 * Chooses the largest possible text if multiple complete texts are gathered for a single document.
	 * @param possible_texts a List of all possible texts saved throughout the crawling process for a single document.
	 */
	public void chooseLargestSufficientText(List<String> possible_texts) {
		String largest_text = "";
		for (String text: possible_texts) {
			if (isSufficientTextSize(this.getAbstr(), text)) {
				if (text.length() > largest_text.length()) {
					largest_text = text;
				}
			}
		}
		this.setText(largest_text);
	}
	
	/**
	 * Gets the current content for a document: called multiple times at different steps of the crawling process.
	 * Thus, in the end it can be decided if a content gets in the dump or not.
	 * @param doc_page_crawler a lower level object always created for a single link
	 */
	public void getContentFromCrawler(DocPageCrawler doc_page_crawler){
		if(doc_page_crawler.hasDOI()){
			this.setDoi(doc_page_crawler.getDoi());
		}
		if(doc_page_crawler.hasTitle()){
			String title = doc_page_crawler.getTitle();
			title = title.replaceAll("\\s+", " ");
			this.setTitle(title);
		}
		if(doc_page_crawler.hasAbstract()){
			String abstr = doc_page_crawler.getAbstract_article();
			abstr = abstr.replaceAll("\\s+", " ");
			// not set if it only consists of whitespaces
			if (abstr.length() > 1) {
				this.setAbstr(abstr);
			}	
		}
		if(doc_page_crawler.hasText()){
			String text = doc_page_crawler.getText();
			text = text.replaceAll("\\s+", " ");
			if (text.length() > 1) {
				this.setText(text);
			}
		}

	}
	
	public boolean hasAbstract() {
		String abs = this.getAbstr();
		return !(abs == null || abs.isEmpty());
	}
	
	
	public boolean hasText(){
		String text = this.getText();
		return !(text == null || text.isEmpty());
	}
	
	/**
	 * After assembling the right content for a single entry in the dump, this method writes it in the dump file.
	 * Here, also other information is written in logging files, e.g. number of documents that have no content (and are thus useless for the corpus).
	 */
	public void writeToDump(DocCrawler doc_crawler){
		ArrayList<String> doc_dump_line = new ArrayList<String>();
		if (this.getDoi() == null || this.getDoi().isEmpty()) {
			doc_dump_line.add(String.valueOf(doc_crawler.getSubst_id()));
			doc_crawler.setSubst_id(doc_crawler.getSubst_id() + 1);
		} else {
			doc_dump_line.add(this.getDoi());	
		}
		doc_dump_line.add(this.getLinkToDocument());
		doc_dump_line.add(this.getTitle());
		// has a full text
		if (!(this.getText() == null || this.getText().isEmpty())){
			doc_dump_line.add(this.getText());
		// has abstract and no full text
		} else if (!(this.getAbstr() == null || this.getAbstr().isEmpty())){
			doc_dump_line.add(this.getAbstr());
		// has neither abstract nor full text -> this document is useless
		} else {
			doc_crawler.setLastDocLinkSuccessful(false);
			Writer.appendLineToFile(this.getLinkToDocument() + "\t" + "DOI: " + this.getDoi() + "\t" +  "no text found", DocProperties.USELESS_DOCLINKS_PATH);
			doc_crawler.setNumDocsNoTextFound(doc_crawler.getNumDocsNoTextFound() + 1);
			return;
		}
		String line = "";
		for (String component: doc_dump_line){
			if (component == null || component.isEmpty()) {
				line += "-" + "\t";
			} else {
				line += component + "\t";
			}
		}
		
		Writer.appendLineToFile(line, DocProperties.DOC_DUMP_PATH);
		doc_crawler.setNumSuccessfulDocuments(doc_crawler.getNumSuccessfulDocuments() + 1);
		doc_crawler.setLastDocLinkSuccessful(true);
	}

	public Integer getMinFullTextSize() {
		return minFullTextSize;
	}
	
}

