
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
public abstract class DocCrawler {
	
	// an id for documents that don't have a digital object identifier (doi)
	protected Integer subst_id = 1;
	
	//a variable for statistical purposes: each time a document is crawled/ or an error occurs the path in the decision tree is shown.
	protected String crawlPath = "";
	// variables for statistical purposes
	protected Integer numDocsNoTextFound = 0;
	//private static Integer numAttemptedDocLinks = 0;
	protected Integer numSuccessfulDocuments = 0;
	
	protected LocalDateTime startTime;
	protected LocalDateTime statusTime;
	
	protected Map<String, Integer> crawlingPathFrequencies = new HashMap<String, Integer>();
	
	protected Integer numFinishedQueries = 0;
	protected Integer numFinishedDocumentsOfQuery = 0;
	
	protected boolean lastDocLinkSuccessful = false;
	
	/**
	 * Reads the NutritionFacts dump and for each link to a scientific document, 
	 * crawls the content of the document in different ways depending on the kind of link.
	 * This method also saves the additional information for statistical purposes during the process, e.g. number of successfully
	 * crawled documents, last query crawled, errors occured during crawling. This data can be found in the document crawling path/ output_files.
	 * @param file File containing the NutritionFacts Dump
	 * @throws Exception 
	 */
	public void traverseDumpAndCrawlDocuments(){
		this.setStartTime(LocalDateTime.now());
		File last_query_and_document = new File(DocProperties.LAST_QUERY_AND_DOCUMENT_PATH);
		/* If crawling process was interrupted, get numbers of finished queries
		 	and number of finished doc links in the last (not finished) query.
		 	For example "1 5" means that one query was finished and 5 doc links of the second query. 
		 	The next number in the last_query_and_document.txt file is a boolean: true if the last processed document
		 	was really finished or false if not. The last number is for memorizing the document ids before the interruption.*/
		if(last_query_and_document.exists()){
			// get information that are necessary to proceed correctly
			String[] last_q_d = Reader.readContentOfFile(DocProperties.LAST_QUERY_AND_DOCUMENT_PATH).split(" ");
			this.setNumFinishedQueries(Integer.parseInt(last_q_d[0])); // last number of finished queries
			this.setNumFinishedDocumentsOfQuery(Integer.parseInt(last_q_d[1])); // last number of finished doclinks of last query
			if (last_q_d[2].equals("true")) {  // information whether last processed doclink was finished
				Editor.deleteLastLine(DocProperties.DOC_DUMP_PATH); 
			}
			this.setSubst_id(Integer.parseInt(last_q_d[3]) + 1); // substitute ID before interruption
			this.readStatusFile();
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(DocProperties.FILTERED_NFDUMP_PATH), "UTF-8"));
			Integer query_index = 0;
			// going through all 
			while(br.ready()){
				Integer successfulDocLinksOfQuery = 0;
				System.out.println("QUERY INDEX: " + (query_index));
				String nextLine = br.readLine();
				String[] lineAsArray = nextLine.split("\\t");
				
				String doclinks = lineAsArray[12];
				if(doclinks.equals("-")) {
					if (query_index >= this.getNumFinishedQueries()) {
						this.setNumFinishedQueries(this.getNumFinishedQueries() + 1);
					}
					query_index += 1;
					continue;
				}
				// proceeding after interruption: skip queries 
				if(query_index < this.getNumFinishedQueries()){
					query_index += 1;
					continue;
				} 
				ArrayList<String> doc_links_for_query = new ArrayList<String>();
				doc_links_for_query.addAll(Arrays.asList(doclinks.split(",")));
				// going through doc links of current query
				for(Integer doc_index = 0; doc_index < doc_links_for_query.size(); doc_index ++){
					String doclink = doc_links_for_query.get(doc_index);
					// last crawled doc link has to be crawled again 
					// -> skip doc link until it is the last finished doc link
					if(doc_index + 1 < this.getNumFinishedDocumentsOFQuery()){
						continue;
					}
					System.out.println("crawling document from: " + doclink);
					Document final_document = new Document(doclink);
					try {
						//crawl Document for every link
						this.crawlOneDocument(doclink, final_document);
					} catch (ExpectedConnectionException e) {
						this.setLastDocLinkSuccessful(false);
						System.err.println("HttpStatusException | UnknownHostException");
					} catch (Exception e) {
						this.setLastDocLinkSuccessful(false);
						System.err.println("Exception caught at high level in DocCrawler");
					}
					final_document.writeToDump(this);
					if (this.isLastDocLinkSuccessful()) {
						successfulDocLinksOfQuery ++;
					}
					this.setStatusTime(LocalDateTime.now());	
					this.writeStatusFile();
					this.setNumFinishedDocumentsOfQuery(this.getNumFinishedDocumentsOFQuery() + 1);
					writeInterruptionInfo();	
				}
				// a query is finished here
				this.setNumFinishedQueries(this.getNumFinishedQueries() + 1);
				query_index += 1;
				this.setNumFinishedDocumentsOfQuery(0);
				writeInterruptionInfo();
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			System.err.println("UnsupportedEncodingException");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("IOException");
		}
	}
	/**
	 * Handles the crawling of one initial link 
	 * @param initial_link String the direct link from nutritionfacts.org to a scientific article/ or other intermediate site.
	 * @param final_document a Document object that takes care of assembling the right content if more than one text is crawled for a single document, 
	 * e.g. 1) abstract, 2) complete text
	 * @throws ExpectedConnectionException Exception for expected errors during crawling, e.g. HttpStatusException, MimeTypeException, etc.
	 */
	public abstract void crawlOneDocument(String initial_link, Document final_document) throws ExpectedConnectionException;
	/**
	 * A method that crawls the content of a document from a pubmed page as initial link, e.g. "http://www.ncbi.nlm.nih.gov/pubmed/23697707"
	 * The abstract from this page is always crawled and saved in the final_document object. If the page has full text links, the full text is crawled
	 * either from a pmc page or any other journal page. At the end, the text that is sufficiently long and most credible to be a scientific document is chosen and 
	 * written from the document object into the doc_dump.
	 * @param final_document a Document object that takes care of assembling the right content if more than one text is crawled for a single document, 
	 * e.g. 1) abstract, 2) complete text
	 * @param doc_page_crawler a lower level object always created for a single link.
	 * @throws ExpectedConnectionException Exception for expected errors during crawling, e.g. HttpStatusException, MimeTypeException, etc.
	 */
	public abstract void handlePubmedPage(Document final_document, DocPageCrawler doc_page_crawler) throws ExpectedConnectionException;
	
	
	/**
	 * A method that crawls the html content of a PMC page, e.g.http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3893150/
	 * @param doc_page_crawler a lower level object always created for a single link
	 * @param final_document final_document a Document object that takes care of assembling the right content if more than one text is crawled for a single document, 
	 * e.g. 1) abstract, 2) complete text
	 * @throws ExpectedConnectionException Exception for expected errors during crawling, e.g. HttpStatusException, MimeTypeException, etc.
	 */
	public abstract void handlePMCPage(DocPageCrawler doc_page_crawler, Document final_document) throws ExpectedConnectionException;

	
	/**
	 * Writes a file for statistical purposes which can be found in the output_documents path any time during and after crawling.
	 */
	public abstract void writeStatusFile();
	
	
	public void addToCrawlPath(String step) {
		this.setCrawlPath(this.getCrawlPath() + " -> " + step);
	}
	
	public void decrementCrawlPath() {
		List<String> crawl_path = Arrays.asList(this.getCrawlPath().split("->"));
		List<String> decremented_path =  crawl_path.subList(0, crawl_path.size() - 1);
		String path_string = String.join("->", decremented_path);
		this.setCrawlPath(path_string);
	}
	
	public void updateCrawlPathFrequencies() {
		String crawling_path = this.getCrawlPath();
		System.out.println("crawling path: " + crawling_path + "\n");
		this.getCrawlingPathFrequencies().putIfAbsent(crawling_path, 0);
		Integer freq = this.getCrawlingPathFrequencies().get(crawling_path);
		this.getCrawlingPathFrequencies().put(crawling_path, freq + 1);
	}
	
	/**
	 * Reads the file for statistical purposes about the crawling status - after interruption.
	 */
	public abstract void readStatusFile();
	
	/**
	 * Writes any information relevant for crawling after an expected or unexpected interruption into logging files.
	 */
	public void writeInterruptionInfo() {
		String interruption_info = "";
		interruption_info += this.getNumFinishedQueries() + " ";
		interruption_info += this.getNumFinishedDocumentsOFQuery() + " ";
		interruption_info += this.isLastDocLinkSuccessful() + " ";
		interruption_info += this.getSubst_id();
		Writer.overwriteFile(interruption_info, DocProperties.LAST_QUERY_AND_DOCUMENT_PATH);
	}
	
	
	public String getCrawlPath() {
		return crawlPath;
	}

	public void setCrawlPath(String crawl_path) {
		this.crawlPath = crawl_path;
	}

	public Integer getNumDocsNoTextFound() {
		return numDocsNoTextFound;
	}

	public void setNumDocsNoTextFound(Integer numDocsNoTextFound) {
		this.numDocsNoTextFound = numDocsNoTextFound;
	}
	
	public Map<String, Integer> getCrawlingPathFrequencies() {
		return crawlingPathFrequencies;
	}

	public void setCrawlingPathFrequencies(
			Map<String, Integer> crawlingPathFrequencies) {
		this.crawlingPathFrequencies = crawlingPathFrequencies;
	}

	public Integer getNumSuccessfulDocuments() {
		return numSuccessfulDocuments;
	}

	public void setNumSuccessfulDocuments(Integer numSuccessfulDocuments) {
		this.numSuccessfulDocuments = numSuccessfulDocuments;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getStatusTime() {
		return statusTime;
	}

	public void setStatusTime(LocalDateTime statusTime) {
		this.statusTime = statusTime;
	}



	public Integer getNumFinishedQueries() {
		return numFinishedQueries;
	}

	public void setNumFinishedQueries(Integer numFinishedQueries) {
		this.numFinishedQueries = numFinishedQueries;
	}

	public Integer getNumFinishedDocumentsOFQuery() {
		return numFinishedDocumentsOfQuery;
	}

	public void setNumFinishedDocumentsOfQuery(Integer numFinishedDocuments) {
		this.numFinishedDocumentsOfQuery = numFinishedDocuments;
	}

	public boolean isLastDocLinkSuccessful() {
		return lastDocLinkSuccessful;
	}

	public void setLastDocLinkSuccessful(boolean lastDocLinkSuccessful) {
		this.lastDocLinkSuccessful = lastDocLinkSuccessful;
	}
	
	public Integer getSubst_id() {
		return subst_id;
	}

	public void setSubst_id(Integer subst_id) {
		this.subst_id = subst_id;
	}
	
	public static void main(String[] args) throws ExpectedConnectionException {
		
		
	}
	
}
