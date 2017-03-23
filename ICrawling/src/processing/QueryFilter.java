package processing;

import io.Editor;
import io.Writer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import testing.DataSetChecker;
import crawling_docs.DocProperties;


/**
 * 
 * Takes the connections file and writes a new one with only those queries that have a sufficient number of documents.
 * This requires a connections file that has already been processed by the ConnectionsWriter and TopicLinksAdder classes.
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class QueryFilter {
	
	private Integer minDocuments = 1;
	private Map<String, Integer> numOwnDocs = new HashMap<String, Integer>();
	
	public QueryFilter(Integer min_documents) {
		this.setMinDocuments(min_documents);
	}
	
	public static Integer numOfDocs(String[] docs_array) {
		if (docs_array.length == 1 && docs_array[0].equals("-")) {
			return 0;
		} else {
			return docs_array.length;	
		}
	}
	
	/**
	 * Isolates the lines with obsolete query ids.
	 * @param path the path to the file from which the queries are read
	 * @param index the idex of the item to filter. Used if corpus formats have to be adjusted to different tools.
	 * @param ids a set of ids that are no longer going to be used.
	 */
	public static void filterLinesWithObsoleteIds(String path, Integer index, Set<String> ids) {
		Editor.deleteFile("temp.txt");
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))){
			while (br.ready()) {
				String line = br.readLine();
				String id = line.split("\t")[index];
				if (ids.contains(id)) {
					Writer.appendLineToFile(line, "temp.txt");
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Editor.transferFileName(path, "temp.txt");
	}
	
	/**
	 * Reads and returns all doc ids from the connections file.
	 * @return
	 */
	public static Set<String> getDocIdsInConnections() {
		Set<String> doc_ids = new HashSet<String>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ProcessingProperties.CONNECTIONS_PATH), "UTF-8"))){
			while (br.ready()) {
				String line = br.readLine();
				String[] line_doc_ids = line.split("\t")[2].split(",");
				doc_ids.addAll(Arrays.asList(line_doc_ids));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc_ids;
	}
	
	/**
	 * Collects the number of useful documents for a query.
	 */
	public void collectDocumentNumbers() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ProcessingProperties.CONNECTIONS_PATH), "UTF-8"));
			while(br.ready()){
				String line = br.readLine();
				String[] components = line.split("\t");
				String query_id = components[0];
				String[] doclinks = components[components.length - 1].split(",");
				Integer num_docs = numOfDocs(doclinks);
				this.getNumOwnDocs().put(query_id, num_docs);
			}	
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes all query ids that have no connections to documents from the nf dump.
	 */
	public void filterQueries() {
		Editor.deleteFile("filtered_connections.txt");
		// step 1: collect the number of direct/own documents for each query
		this.collectDocumentNumbers();
		// step 2: add doc numbers of linked queries for each query
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ProcessingProperties.CONNECTIONS_PATH), "UTF-8"));
			while(br.ready()){
				String line = br.readLine();
				String[] components = line.split("\t");
				String query_id = components[0];
				Integer num_own_docs = this.getNumOwnDocs().get(query_id);
				
				String[] linked_query_ids = components[1].split(",");
				

				for (String id: linked_query_ids) {
					Integer num_docs = this.getNumOwnDocs().get(id);
					if (num_docs == null) {
						continue;
					}
					num_own_docs += num_docs;
				}
				
				if (num_own_docs >= this.getMinDocuments()) {
					//System.out.println("HAS DOCS: " + line);
					Writer.appendLineToFile(line, "filtered_connections.txt");
				} else {
					//System.out.println("NO DOCS: " + line);
				}
			}	
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Editor.transferFileName(ProcessingProperties.CONNECTIONS_PATH, "filtered_connections.txt");
		
		// filter queries file
		Set<String> query_ids_in_connections = DataSetChecker.readIdSet(ProcessingProperties.CONNECTIONS_PATH, 0);
		filterLinesWithObsoleteIds(ProcessingProperties.QUERY_TEXTS_PATH, 0, query_ids_in_connections);
		// filter docs file
		Set<String> doc_ids_in_connections = getDocIdsInConnections();
		filterLinesWithObsoleteIds(ProcessingProperties.DOC_TEXTS_PATH, 0, doc_ids_in_connections);
	}
	
	public Integer getMinDocuments() {
		return minDocuments;
	}
	public void setMinDocuments(Integer minDocuments) {
		this.minDocuments = minDocuments;
	}

	public Map<String, Integer> getNumOwnDocs() {
		return numOwnDocs;
	}

	public void setNumOwnDocs(Map<String, Integer> numOwnDocs) {
		this.numOwnDocs = numOwnDocs;
	}
}
