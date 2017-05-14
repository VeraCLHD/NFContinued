package processing;

import io.Editor;
import io.Writer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import crawling_docs.DocProperties;

/**
 * Adds links between queries in a new connections file based on shared topics. This applies only to video and article entries.
 * <p>
 * Required files: connections file, filtered_nfdump file
 * <p>
 * It takes some time to run this class because it needs |doc dump| x |doc dump| steps.
 */
public class TopicLinksAdder {

	/**
	 * Checks if topics of two queries overlap sufficiently by using this measure: <br>
	 * overlap(X, Y) = |intersection(X, Y)| / |X| <br>
	 * X is the query that gets new links from Y.
	 */
	
	public static boolean topicsMatch(String[] topics1, String[] topics2, Double min_overlap) {
		if ((topics1.length == 1 && topics1[0].equals("-")) || (topics2.length == 1 && topics2[0].equals("-"))) {
			return false;
		}
		Set<String> set1 = new HashSet<String>(Arrays.asList(topics1));
		Set<String> set2 = new HashSet<String>(Arrays.asList(topics2));
		Double size1 = (double) set1.size();
		// set1 becomes the intersection
		set1.retainAll(set2);
		Double size_shared = (double) set1.size();
		Double overlap = size_shared / size1;
		return (overlap >= min_overlap);
	}
	
	/**
	 * Adds links to documents queries when they have an overlap between their topics >= 70%
	 */
	public static void addTopicLinks() {
		Editor.deleteFile(ProcessingProperties.CONNECTIONS_WITH_TOPICS_PATH);
		try {
			BufferedReader br_nf = new BufferedReader(new InputStreamReader(new FileInputStream(DocProperties.FILTERED_NFDUMP_PATH), "UTF-8"));
			BufferedReader br_c = new BufferedReader(new InputStreamReader(new FileInputStream(ProcessingProperties.CONNECTIONS_PATH), "UTF-8"));
			
			// going through NF dump and connections file
			while(br_c.ready()) {
				// get data from connection line
				String c_line = br_c.readLine();
				String[]c_components = c_line.split("\t");
				String query_id = c_components[0];
				
				// get data from NF dump line
				String nf_line = br_nf.readLine();
				String[]nf_components = nf_line.split("\t");
				String[] topics = nf_components[5].split(",");
				
				// skip if it has no topics
				if (topics[0].equals("-")) {
					Writer.appendLineToFile(c_line, ProcessingProperties.CONNECTIONS_WITH_TOPICS_PATH);
					continue;
				}
				Set<String> linked_query_ids = new HashSet<String> ();
				// only videos and articles
				linked_query_ids.addAll(Arrays.asList(c_components[1]));

				// going through NF dump another time to compare all pairs of videos or articles
				
				try {
					BufferedReader br_nf2 = new BufferedReader(new InputStreamReader(new FileInputStream(DocProperties.FILTERED_NFDUMP_PATH), "UTF-8"));
					while(br_nf2.ready()){
						String nf_line2 = br_nf2.readLine();
						String[] nf_components2 = nf_line2.split("\t");
						String query_id2 = nf_components2[0];
						String[] topics2 = nf_components2[5].split(",");
						if (query_id.equals(query_id2) || linked_query_ids.contains(query_id2)) {
							continue;
						}
						if (topics2.length == 1 && topics2[0].equals("-")) {
							continue;
						}
						if (!TopicLinksAdder.topicsMatch(topics, topics2, 0.7)) {
							continue;
						}
						// both queries share topics -> add second one as a link of the first one
						linked_query_ids.add(query_id2  + "-topics");
					}	
					br_nf2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// write entry of new connections file with additional ids
				c_components[1] = String.join(",", linked_query_ids);
				String connections_line = String.join("\t", c_components);
				Writer.appendLineToFile(connections_line, ProcessingProperties.CONNECTIONS_WITH_TOPICS_PATH);
			}
			br_nf.close();
			br_c.close();
			Editor.transferFileName(ProcessingProperties.CONNECTIONS_PATH, ProcessingProperties.CONNECTIONS_WITH_TOPICS_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
