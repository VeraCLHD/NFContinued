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
import java.util.HashSet;
import java.util.Set;

/**
 * A class that applies the relevance grading scheme for connections between queries and documents.
 * (3): Query->Document
 * (2): Query->Query->Document
 * (1): Query(topic link)->Query->Document
 * For further reference about the relevance grading scheme, see the README file.
 * @author Vera Boteva, Demian Gholipour
 *
 */

public class RelevanceGrader {

	public static void writeRelevanceEntry(String query_id, String doc_id, Integer relevance) {
		String entry = query_id + "\t"+ doc_id + "\t" + relevance;
		if (!doc_id.equals("-")) {
			Writer.appendLineToFile(entry, ProcessingProperties.RELEVANCE_PATH);		
		}
	}
	
	/**
	 * Writes the relevance grading file.
	 */
	public static void writeRelevanceFile() {
			 
			Editor.deleteFile(ProcessingProperties.RELEVANCE_PATH);
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ProcessingProperties.CONNECTIONS_PATH), "UTF-8"));
				while(br.ready()) {
					
					String line = br.readLine();
					String[]  components = line.split("\t");
					String query_id = components[0];
					String[] query_link_id_array = components[1].split(",");
					Set<String> query_link_ids = new HashSet<String>();
					query_link_ids.addAll(Arrays.asList(query_link_id_array));
					Set<String> doc_link_ids = new HashSet<String>();
					String[] doc_ids = components[2].split(",");
					doc_link_ids.addAll(Arrays.asList(doc_ids));
					
					for (String doc: doc_ids) {
						RelevanceGrader.writeRelevanceEntry(query_id, doc, 3);			
					}
					
					BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(ProcessingProperties.CONNECTIONS_PATH), "UTF-8"));
					
					while(br2.ready()) {
						String line2 = br2.readLine();
						String[]  components2 = line2.split("\t");
						String query_id2 = components2[0];
						if (query_id2.equals(query_id)) {
							continue;
						}
						String id2_topics = query_id2 + "-topics";
						String[] doc_ids2 = components2[2].split(",");
						if (query_link_ids.contains(query_id2)) {
							for (String doc: doc_ids2) {
								if (!doc_link_ids.contains(doc)) {
									RelevanceGrader.writeRelevanceEntry(query_id, doc, 2);	
									doc_link_ids.add(doc);
								}								
							}
						} else if (query_link_ids.contains(id2_topics)) {
							for (String doc: doc_ids2) {
								if (!doc_link_ids.contains(doc)) {
									RelevanceGrader.writeRelevanceEntry(query_id, doc, 1);	
									doc_link_ids.add(doc);
								}								
							}
						}
						
					}
					
					br2.close();
				}
				
				br.close();
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			
	}

}
