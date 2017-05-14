/**
 * 
 */
package processing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import io.Editor;
import io.Writer;
import crawling_docs.DocProperties;
import crawling_queries.Properties;

/**
 * A class that writes an additional variant of the queries in the corpus: the query text without the comments of users below each
 * article/ video / question. Comments are a huge part of video and article texts in the default variant of the corpus ("all_content" variant)
 * but could not always be of the same relevance as the other query components (as off-topic comments are permitted on nutritionfacts.org).
 * By excluding them and thus taking only the most relevant parts of the text - the article text itself, the doctor's note and transcript
 * of videos - it is possible to compare the results of these two variants when doing experiments with information retrieval systems.
 * The variants writer thus allows more flexibility in deciding if reader's comments should really be part of the corpus.
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class CorpusVariantsWriter {
	
	public static Set<String> final_query_ids = new HashSet<String>();
	
	public CorpusVariantsWriter(){
		
	}

	/**
	 * Collects the query ids that finally get into the corpus. At the point where this method is called, some queries have already
	 * been deleted because they turned out to have no useful documents.
	 * The other methods of this class work with the filtered_nfdump, which still contain these queries. This, this method helps
	 * find them first and not write them in the variant without comments.
	 */
	public static void collectFinalQueryIds(){
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ProcessingProperties.QUERY_TEXTS_PATH), "UTF-8"));
			while(br.ready()){
				String nextLine = br.readLine();
				String[] lineAsArray = nextLine.split("\t");
				CorpusVariantsWriter.getFinal_query_ids().add(lineAsArray[0].trim());
			}	
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes the contentfile of the corpus variant without comments.
	 */
	public static void collectAndWriteVariantWithoutComments(){
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(DocProperties.FILTERED_NFDUMP_PATH), "UTF-8"));
			while(br.ready()){
				String nextLine = br.readLine();
				String[] lineAsArray = nextLine.split("\t");
				if(CorpusVariantsWriter.getFinal_query_ids().contains(lineAsArray[0])){
					CorpusVariantsWriter.sortAndWriteElementsOfLineQCorpus(lineAsArray);
				}
				
			}	
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sorts one line of the filtered nfdump and excludes comments from it.
	 * @param lineElements the elements of one query as String []
	 */
	public static void sortAndWriteElementsOfLineQCorpus(String[] lineElements){
		String query_id = lineElements[0];
		String querytext = query_id + "\t";
		/* 0th: element is the id
		 * 1th: link
		 * 2th: title
		 * 3th: text
		 * 4th: comments
		 * 5th: topics
		 * 6th: description
		 * 7th: DoctorsNote 
		 * elements 8-11: query links
		 * 12th element: doclinks
		 * 
		 * Needed text elements, if available: 2, 3, (5, ?) 6, 7
		 * Write in file: id (tab) text 
		 */
		if (lineElements[3].equals("-")) {
			return;
		}
		for(int i = 2; i < 8; i++){
			if (i == 4 || lineElements[i].equals("-")) {
				continue;
			}
			querytext += lineElements[i] + " ";	
		}
		Writer.appendLineToFile(querytext, ProcessingProperties.QUERIES_WITHOUT_COMMENTS_PATH);
	}
	
	public static void createVariantWC(){
		Editor.deleteFile(ProcessingProperties.QUERIES_WITHOUT_COMMENTS_PATH);
		CorpusVariantsWriter.collectAndWriteVariantWithoutComments();
	}
	
	public static Set<String> getFinal_query_ids() {
		return final_query_ids;
	}


	public static void setFinal_query_ids(Set<String> final_query_ids) {
		CorpusVariantsWriter.final_query_ids = final_query_ids;
	}

}
