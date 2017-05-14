package processing;
/**
 * A class that handles the normalization of ids. Queries have the uniform ids of the form: "PLAIN-" +[number].
 * Documents have uniform ids of the form: "MED-" +[number].
 * @author Vera Boteva, Demian Gholipour
 */
import io.Editor;
import io.Writer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import crawling_docs.DocProperties;

/**
 * 
 * Replaces the IDs of the NF Dump or Doc Dump with uniform IDs.
 *
 */

public class IDNormalizer {

	static final String DOC_MARKUP = "MED-";
	static final String QUERY_MARKUP = "PLAIN-";
	
	public static void replaceDumpIDs(String id_markup, String path, String new_file_path) {
		Integer id_number = 1;
		Editor.deleteFile(new_file_path);
		try {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
			while(br.ready()){
				String line = br.readLine();
				String[] lineElements = line.split("\t");
				lineElements[0] = id_markup + id_number;	
				String newLine = String.join("\t", lineElements);
				Writer.appendLineToFile(newLine, new_file_path);
				id_number ++;
			}	
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static void replaceNFDumpIds() {
		IDNormalizer.replaceDumpIDs(IDNormalizer.QUERY_MARKUP, DocProperties.FILTERED_NFDUMP_PATH, ProcessingProperties.NFDUMP_NEW_IDS_PATH);
		Editor.transferFileName(DocProperties.FILTERED_NFDUMP_PATH, ProcessingProperties.NFDUMP_NEW_IDS_PATH);
	}
	
	public static void replaceDocDumpIds() {
		IDNormalizer.replaceDumpIDs(IDNormalizer.DOC_MARKUP, DocProperties.DOC_DUMP_PATH, ProcessingProperties.DOCDUMP_NEW_IDS_PATH);
		Editor.transferFileName(DocProperties.DOC_DUMP_PATH, ProcessingProperties.DOCDUMP_NEW_IDS_PATH);
	}
}
