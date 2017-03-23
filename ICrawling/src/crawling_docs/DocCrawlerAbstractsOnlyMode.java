/**
 * 
 */
package crawling_docs;

import io.Editor;
import io.Reader;
import io.Writer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import crawling_queries.Properties;
import crawling_queries.QuestionsDietitianLinksCollector;

/**
 * A class that manages the crawling process of the scientific documents: 
 * serves like a main class calling all the other crawlers in abstract only mode: only abstracts of documents are crawled and no full texts.
 * As input for this class, a filtered dump (meaning non relevant document links were filtered) from the Nutriton Facts Crawler is required.
 * <p>
 * Based on different criteria, e.g. an NCBI (and or a PMC page) the crawler decides 
 * whether to crawl the html content directly or to use another special crawler (e.g. PMCArticleCrawler).
 * <p>
 * This class contains the so called document crawling decision tree.
 * 
 * @author Vera Boteva, Demian Gholipour
 */
public class DocCrawlerAbstractsOnlyMode extends DocCrawler {
	
	private static DocCrawlerAbstractsOnlyMode instance = null;
	
	public static DocCrawlerAbstractsOnlyMode getInstance(){
		 if(instance == null) {
			 instance = new DocCrawlerAbstractsOnlyMode();
		     }
		 return instance;
	   }
	
	/**
	 * Handles the crawling of one initial link: it applies only to abstracts.
	 * @param initial_link String the direct link from nutritionfacts.org to a scientific article.
	 * @param final_document a Document object that takes care of assembling the right content if more than one text is crawled for a single document. 
	 * @throws ExpectedConnectionException Exception for expected errors during crawling, e.g. HttpStatusException, MimeTypeException, etc.
	 */
	@Override
	public void crawlOneDocument(String initial_link, Document final_document) throws ExpectedConnectionException{
		//first click level
		DocPageCrawler doc_page_crawler = DocPageCrawlerFactory.createCrawler(initial_link);
		
		if (DocPageCrawler.isPMCLink(initial_link)) {
			this.setCrawlPath("PMC page");
			this.handlePMCPage(doc_page_crawler, final_document);
		} else if (DocPageCrawler.isNCBILink(initial_link)) {
			this.setCrawlPath("PubMed page");
			handlePubmedPage(final_document, doc_page_crawler);
		} 
		this.updateCrawlPathFrequencies();
	}
	
	/**
	 * A method that crawls the content of a document from a pubmed page as initial link, e.g. "http://www.ncbi.nlm.nih.gov/pubmed/23697707" (abstract only)
	 * The abstract from this page is always crawled and saved in the final_document object.
	 * @param final_document a Document object that takes care of assembling the right content if more than one text is crawled for a single document.
	 * @param doc_page_crawler a lower level object always created for a single link.
	 * @throws ExpectedConnectionException Exception for expected errors during crawling, e.g. HttpStatusException, MimeTypeException, etc.
	 */
	@Override
	public void handlePubmedPage(Document final_document, DocPageCrawler doc_page_crawler) throws ExpectedConnectionException{
		doc_page_crawler.crawlAbstractOnly();
		final_document.getContentFromCrawler(doc_page_crawler);
		
		if(doc_page_crawler.hasFreePMCLink()){
			this.addToCrawlPath("PMC page");
			String pmc_link = doc_page_crawler.getFree_pmc_link();
			DocPageCrawler doc_page_crawler_second_level = DocPageCrawlerFactory.createCrawler(pmc_link);
			this.handlePMCPage(doc_page_crawler_second_level, final_document);
		}
	}
	
	@Override
	public void writeStatusFile(){
		String status = "STATUS OF CRAWLING DOCUMENTS" + "\r\n" ;
		status += "START TIME: " + this.getStartTime().toString()  + "\r\n";
		status += "CURRENT TIME: " + this.getStatusTime() + "\r\n";
		status += "number of all attempted documents: " + (this.getNumSuccessfulDocuments() + this.getNumDocsNoTextFound()) + "\r\n";
		status += "number of failed documents: " + this.getNumDocsNoTextFound() + "\r\n";
		status += "number of successful documents: " + this.getNumSuccessfulDocuments() + "\r\n";
		status += "FREQUENCIES OF PATHS: " + "\r\n";
		for (String path: this.getCrawlingPathFrequencies().keySet()){
			status += path + ": " + this.getCrawlingPathFrequencies().get(path) +"\r\n";
		}
		Writer.overwriteFile(status, DocProperties.STATUS_PATH);
	}

	
	/**
	 * Reads the file for statistical purposes about the crawling status - after interruption.
	 */
	public void readStatusFile() {
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(DocProperties.STATUS_PATH), "UTF-8"))) {
			String line;
			String first_part = "";
			Integer frequency = 0;
			boolean line_is_crawl_path = false;
			while (br.ready()) {
				line = br.readLine();
				String[] components = line.split(":");
				try {
					first_part = components[0];
					frequency = Integer.parseInt(components[1].trim());
				} catch (Exception e) {}
				if (line.contains("FREQUENCIES OF PATHS:")) {
					line_is_crawl_path = true;
					continue;
				} else if (line_is_crawl_path) {
					String crawl_path = first_part;
					this.getCrawlingPathFrequencies().put(crawl_path, frequency);
				} else if (line.contains("number of failed documents")) {
					this.setNumDocsNoTextFound(frequency);
				} else if (line.contains("number of successful documents")) {
					this.setNumSuccessfulDocuments(frequency);
				}
			}
		} catch (IOException e) {
			System.err.println("IOException");
		}
	}

	@Override
	// anpassen an Abstract only mode
	public void handlePMCPage(DocPageCrawler doc_page_crawler, Document final_document) throws ExpectedConnectionException {
		doc_page_crawler.crawlAbstractOnly();
		final_document.getContentFromCrawler(doc_page_crawler);
		
	}
	



}
