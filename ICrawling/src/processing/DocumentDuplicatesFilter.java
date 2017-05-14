package processing;

import io.Editor;
import io.Writer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import crawling_docs.DocProperties;
/**
 * A class that processes the docdump by deleting duplicate documents from it.
 * A document is considered to be a duplicate if the its id and link are the same.
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class DocumentDuplicatesFilter {

	/**
	 * Filters duplicate documents with the same id and link from the initial docdump.
	 */
	public static void filterDuplicates() {
		Editor.deleteFile(ProcessingProperties.DOCDUMP_WITHOUT_DUPLICATES_PATH);
		Map<String, String> id_to_link = new HashMap<String, String>();
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(DocProperties.DOC_DUMP_PATH), "UTF-8"))){
			while (br.ready()) {
				
				String line = br.readLine();
				String[] components = line.split("\t");
				String id = components[0];
				String link = components[1];
				
				if (id_to_link.containsKey(id)) {
					if (id_to_link.get(id).equals(link)) {
						continue;
					}
				} else {
					id_to_link.put(id, link);	
				}
				Writer.appendLineToFile(line, ProcessingProperties.DOCDUMP_WITHOUT_DUPLICATES_PATH);
			}
			
			
			Editor.transferFileName(DocProperties.DOC_DUMP_PATH, ProcessingProperties.DOCDUMP_WITHOUT_DUPLICATES_PATH);
			
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
