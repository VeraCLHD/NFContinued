
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
import java.util.Scanner;

import org.jsoup.HttpStatusException;

/**
 * A class that manages the crawling process of the scientific documents: 
 * serves like a main class calling all the other crawlers.
 * As input for this class, a filtered dump (meaning non relevant document links were filtered) from the Nutriton Facts Crawler is required.
 * <p>
 * Based on different criteria, e.g. direct pdf link, an NCBI (and or a PMC page) the crawler decides 
 * whether to crawl the pdf/html content directly or to use another special crawler (e.g. PMCArticleCrawler).
 * <p>
 * This class contains the so called document crawling decision tree.
 * 
 * @author Vera Boteva, Demian Gholipour
 */
public class DocCrawlerFullTextMode extends DocCrawler {
	
	private static Integer numSuccessfulPDFs = 0;
	
	private static DocCrawlerFullTextMode instance = null;
	
	public static DocCrawlerFullTextMode getInstance(){
		 if(instance == null) {
			 instance = new DocCrawlerFullTextMode();
		     }
		 return instance;
	   }
	
	@Override
	public void crawlOneDocument(String initial_link, Document final_document) throws ExpectedConnectionException{
		//first click level
		DocPageCrawler doc_page_crawler = DocPageCrawlerFactory.createCrawler(initial_link);
		
		if (PDFHandler.isPDFPage(doc_page_crawler)) {
			this.setCrawlPath("PDF link");
			handlePDFLink(doc_page_crawler, final_document);
		} else if (DocPageCrawler.isPMCLink(initial_link)) {
			this.setCrawlPath("PMC page");
			this.handlePMCPage(doc_page_crawler, final_document);
		} else if (DocPageCrawler.isNCBILink(initial_link)) {
			this.setCrawlPath("PubMed page");
			handlePubmedPage(final_document, doc_page_crawler);
		} else {
			this.setCrawlPath("non-NCBI page");
			this.handleNonNCBIPage(final_document, doc_page_crawler);
		}
		this.updateCrawlPathFrequencies();
	}
	
	@Override
	public void handlePubmedPage(Document final_document, DocPageCrawler doc_page_crawler) throws ExpectedConnectionException{
		doc_page_crawler.crawlContent();
		final_document.getContentFromCrawler(doc_page_crawler);
		
		if(doc_page_crawler.hasFreePMCLink()){
			this.addToCrawlPath("PMC page");
			String pmc_link = doc_page_crawler.getFree_pmc_link();
			DocPageCrawler doc_page_crawler_second_level = DocPageCrawlerFactory.createCrawler(pmc_link);
			this.handlePMCPage(doc_page_crawler_second_level, final_document);
		}
		
		else if(doc_page_crawler.hasFullTextLink()){
			List<String> possible_texts = new ArrayList<String>();
			for(String fullTextLink: doc_page_crawler.getFull_text_links()){
				this.setCrawlPath("PubMed page");
				this.addToCrawlPath("full text link");
				DocPageCrawler ftl_page_crawler = DocPageCrawlerFactory.createCrawler(fullTextLink);
				if (PDFHandler.isPDFPage(ftl_page_crawler)) {
					this.addToCrawlPath("PDF link");
					this.handlePDFLink(ftl_page_crawler, final_document);
					return;
				} else {
					this.handleNonNCBIPage(final_document, ftl_page_crawler);
					if(ftl_page_crawler.hasPDFLink() && ftl_page_crawler.hasText()) {
						return;
					}
					else if(ftl_page_crawler.hasText()){
						possible_texts.add(ftl_page_crawler.getText());
					}
				}
			}
			// we choose the largest text longer than abstract and longer than 600 characters
			// or the abstract itself if there is no text
			if(!possible_texts.isEmpty()){
				final_document.chooseLargestSufficientText(possible_texts);
			} 
		}	
	}
	
	
	/**
	 * A method that crawls the content from any other page than direct pdf, pmc or pubmed. Further it searches for pdf links this page
	 * might have and if there are any, it finds the correct one and preferably crawls the pdf content. Otherwise, any html content on the page itself is crawled.
	 * @param final_document final_document a Document object that takes care of assembling the right content if more than one text is crawled for a single document, 
	 * e.g. 1) abstract, 2) complete text
	 * @param doc_page_crawler a lower level object always created for a single link.
	 * @throws ExpectedConnectionException Exception for expected errors during crawling, e.g. HttpStatusException, MimeTypeException, etc.
	 */
	public void handleNonNCBIPage(Document final_document, DocPageCrawler doc_page_crawler) throws ExpectedConnectionException{
		doc_page_crawler.crawlPDFLink();
		if(doc_page_crawler.hasPDFLink()) {
			this.addToCrawlPath("PDF link");
			String link_to_pdf_document = PDFHandler.crawlPDFLink(doc_page_crawler);
			doc_page_crawler.setPdf_link(link_to_pdf_document);
			DocPageCrawler doc_page_crawler_second_level = DocPageCrawlerFactory.createCrawler(link_to_pdf_document);
			handlePDFLink(doc_page_crawler_second_level, final_document);
		} else {
			doc_page_crawler.crawlContent();
			this.addToCrawlPath("HMTL text");
			final_document.getContentFromCrawler(doc_page_crawler);
			if(final_document.hasText()){
				if(!final_document.isSufficientTextSize(final_document.getAbstr(), final_document.getText())){
					final_document.setText(null);
				}
			}
		}
	}
	
	/**
	 * Crawls content from a direct or an indirect pdf link.
	 * @param doc_page_crawler a lower level object always created for a single link.
	 * @param final_document a Document object that takes care of assembling the right content if more than one text is crawled for a single document, 
	 * e.g. 1) abstract, 2) complete text
	 * @throws ExpectedConnectionException Exception for expected errors during crawling, e.g. HttpStatusException, MimeTypeException, etc.
	 */
	public static void handlePDFLink(DocPageCrawler doc_page_crawler, Document final_document) throws ExpectedConnectionException{
		doc_page_crawler.crawlTextFromPDF();
		final_document.getContentFromCrawler(doc_page_crawler);
	}
	
	/**
	 * A method that crawls the html content of a PMC page, e.g.http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3893150/
	 * @param doc_page_crawler a lower level object always created for a single link
	 * @param final_document final_document a Document object that takes care of assembling the right content if more than one text is crawled for a single document, 
	 * e.g. 1) abstract, 2) complete text
	 * @throws ExpectedConnectionException Exception for expected errors during crawling, e.g. HttpStatusException, MimeTypeException, etc.
	 */
	public void handlePMCPage(DocPageCrawler doc_page_crawler, Document final_document) throws ExpectedConnectionException {
		doc_page_crawler.crawlContent();
		final_document.getContentFromCrawler(doc_page_crawler);
	}
	
	@Override
	public void writeStatusFile(){
		String status = "STATUS OF CRAWLING DOCUMENTS" + "\r\n" ;
		status += "START TIME: " + this.getStartTime().toString()  + "\r\n";
		status += "CURRENT TIME: " + this.getStatusTime() + "\r\n";
		status += "number of all attempted documents: " + (this.getNumSuccessfulDocuments() + this.getNumDocsNoTextFound()) + "\r\n";
		status += "number of failed documents: " + this.getNumDocsNoTextFound() + "\r\n";
		status += "number of successful documents: " + this.getNumSuccessfulDocuments() + "\r\n";
		status += "number of successful PDFs: " + DocCrawlerFullTextMode.getNumSuccessfulPDFs() + "\r\n" ;
		status += "FREQUENCIES OF PATHS: " + "\r\n";
		for (String path: this.getCrawlingPathFrequencies().keySet()){
			status += path + ": " + this.getCrawlingPathFrequencies().get(path) +"\r\n";
		}
		Writer.overwriteFile(status, DocProperties.STATUS_PATH);
	}
	
	/**
	 * Reads the file for statistical purposes about the crawling status - after interruption.
	 */
	@Override
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
				} else if (line.contains("number of successful PDFs")) {
					DocCrawlerFullTextMode.setNumSuccessfulPDFs(frequency);
				}
			}
		} catch (IOException e) {
			System.err.println("IOException");
		}
	}
	
	public static Integer getNumSuccessfulPDFs() {
		return numSuccessfulPDFs;
	}

	public static void setNumSuccessfulPDFs(Integer numAttemptedPDFs) {
		DocCrawlerFullTextMode.numSuccessfulPDFs = numAttemptedPDFs;
	}
	

}
