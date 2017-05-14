/**
 * 
 */
package processing;

/**
 * A class for paths and files for processing.
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class ProcessingProperties {
	
	public static final String PROCESSING_FILES_PATH = "processing/";
	public static final String DATASETS_PATH = "datasets/";
	public static final String QUERIES_WITHOUT_COMMENTS_PATH = PROCESSING_FILES_PATH + "without_comments.queries";
	
	public static final String QUERY_TEXTS_PATH = PROCESSING_FILES_PATH + "all.queries";
	public static final String DOC_TEXTS_PATH = PROCESSING_FILES_PATH + "all.docs";
	public static final String RELEVANCE_PATH = PROCESSING_FILES_PATH + "all.qrel";
	
	public static final String PREPROCESSED_DOCS_PATH = PROCESSING_FILES_PATH + "doc_preprocessed.txt";
	public static final String PREPROCESSED_QUERIES_PATH = PROCESSING_FILES_PATH + "queries_preprocessed.txt";
	public static final String PREPROCESSED_QUERIES_WC_PATH = PROCESSING_FILES_PATH + "wc_queries_preprocessed.txt";
	
	public static final String CONNECTIONS_PATH = PROCESSING_FILES_PATH + "connections.txt";
	public static final String CONNECTIONS_WITH_TOPICS_PATH = PROCESSING_FILES_PATH + "connections_with_topics.txt";
	
	public static final String DOC_IDENTITY_PATH = PROCESSING_FILES_PATH + "doc_identity.txt";
	public static final String QUERY_IDENTITY_PATH = PROCESSING_FILES_PATH + "query_identity.txt";
	public static final String DOCDUMP_WITHOUT_DUPLICATES_PATH = PROCESSING_FILES_PATH + "docdump_without_duplicates.txt";
	
	public static final String DOCDUMP_NEW_IDS_PATH = PROCESSING_FILES_PATH + "docdump_new_ids.txt";
	public static final String NFDUMP_NEW_IDS_PATH = PROCESSING_FILES_PATH + "nfdump_new_ids.txt";
	
	public static final String PATH_TO_STOPPWORDSLIST = "/stopwords.large";

}
