/**
 * 
 */
package crawling_docs;

import java.util.regex.Pattern;


/**
 * A class that contains variables that are used during the whole crawling process: paths, delay variables, etc.
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */

public class DocProperties {
	final static long PUBMED_DELAY_NANOSECONDS = 5000000000L;
	final static long DEFAULT_DELAY_NANOSECONDS = 4000000000L; // 1 sec = 1.000.000.000 nanoseconds
	final static int TIMEOUT = 6000; // milliseconds
	final static int MAX_CONNECTION_TRIALS = 3;
	
	public static final String FILTERED_NFDUMP_PATH = "filtered_nfdump.txt";
	
	// output files of doc crawling process:
	public static final String DOC_CRAWLING_OUTPUT_PATH = "crawling_docs/output_files/";
	public static final String DOC_PROCESSING_OUTPUT_PATH = "crawling_docs/docs_processing/";
	public static final String DOC_DUMP_PATH = "doc_dump.txt";
	public static final String TEMP_PDF_PATH = DOC_CRAWLING_OUTPUT_PATH + "temporary_stream_for_pdf";
	public static final String STATUS_PATH = DOC_CRAWLING_OUTPUT_PATH + "document_crawling_status.txt";
	public static final String LAST_QUERY_AND_DOCUMENT_PATH = DOC_CRAWLING_OUTPUT_PATH + "last_query_and_document.txt";
	public static final String USELESS_DOCLINKS_PATH = DOC_CRAWLING_OUTPUT_PATH + "useless_doclinks.txt";
	
	public static final String NBCI_MAIN_PAGE = "http://www.ncbi.nlm.nih.gov";
	public static final Pattern NCBI_PAGE_PATTERN = Pattern.compile("http:\\/\\/(www\\.)?ncbi.nlm.nih.gov\\/.*");
	public static final Pattern PMC_ARTICLE_PATTERN = Pattern.compile("http:\\/\\/(www\\.)?ncbi.nlm.nih.gov\\/pmc\\/articles\\/.*");
	public static final Pattern POSSIBLE_PDF_LINK_PATTERN = Pattern.compile("http:\\/\\/(www\\.)?.*pdf.*");
	public static final Pattern DOC_ID_PATTERN = Pattern.compile("(\\d*(-)?)+\\d+");
	public static final String EXCLUDE_REFERENCES = "div[id~=.*references.*|.*References.*|.*cit.*], div[class~=.*ref.*|.*references.*|.*References.*|.*cit.*], dt[class~=.*ref.*|.*references.*|.*References.*|.*cit.*], dt[id~=class~=.*ref.*|.*references.*|.*References.*|.*cit.*], dd[id~=class~=.*ref.*|.*references.*|.*References.*|.*cit.*]";
	

	
	public static final String NCBI_DOMAIN = "http://www.ncbi.nlm.nih.gov";
	
	public static final String USER_AGENT = "Student Project Uni Heidelberg (boteva@cl.uni-heidelberg.de, gholipour@stud.uni-heidelberg.de)";
	

}
