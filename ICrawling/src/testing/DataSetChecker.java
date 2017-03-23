package testing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

/**
 * Ensures for a triple or .queries, .docs, .qrel files that: <br>
 * - all queries of .queries file also appear in qrel file AND vice-versa  <br>
 * - all documents of .docs files also appear in qrel file AND vice-versa
 * <p>
 * The files are ok if nothing is printed. Missing entries are printed otherwise.
 * @author Vera Boteva, Demian Gholipour
 */

public class DataSetChecker {
	
	
	/**
	 * Reads a file that contains IDs and returns a set of these.
	 * @param index position of the ID in an entry of the file
	 */
	
	public static Set<String> readIdSet(String path, Integer index) {
		Set<String> ids = new HashSet<String>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))){
			while (br.ready()) {
				String line = br.readLine();
				String id = line.split("\t")[index];
				ids.add(id);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ids;
	}
	
	/**
	 * Prints all entries of Set 1 that are not in Set 2.
	 */
	
	public static void compareIdSets(Set<String> set1, Set<String> set2, String error_message) {
		for (String id: set1) {
			if (!set2.contains(id)) {
				System.err.println(error_message + ":	" + id);
			}
		}
	}
	
	/**
	 * Goes through queries, docs, qrel files and prints missing entries.
	 * The files are ok if nothing is printed.
	 */
	
	public static void checkDataSetCompleteness(String queries_path, String docs_path, String qrel_path) {
		Set<String> query_ids_in_queries = readIdSet(queries_path, 0);
		Set<String> query_ids_in_qrel = readIdSet(qrel_path, 0);
		compareIdSets(query_ids_in_queries, query_ids_in_qrel, "id from queries not in qrel");
		compareIdSets(query_ids_in_qrel, query_ids_in_queries, "id from qrel not in queries");
		query_ids_in_queries.clear();
		query_ids_in_qrel.clear();
		Set<String> doc_ids_in_docs = readIdSet(docs_path, 0);
		Set<String> doc_ids_in_qrel = readIdSet(qrel_path, 1);		
		compareIdSets(doc_ids_in_docs, doc_ids_in_qrel, "id from docs not in qrel");
		compareIdSets(doc_ids_in_qrel, doc_ids_in_docs, "id from qrel not in docs");
	}
	
	public static void main(String[] args) {
		String queries_path = "datasets/train.queries";
		String docs_path = "datasets/train.docs";
		String qrel_path = "datasets/train.qrel";
		DataSetChecker.checkDataSetCompleteness(queries_path, docs_path, qrel_path);
	}
}
