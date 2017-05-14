/**
 * 
 */
package processing;

import io.Editor;
import io.Writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import crawling_docs.DocProperties;
import crawling_queries.ArticleLinksCollector;
import crawling_queries.Properties;

/**
 * A class that divides the final documents dump into two components: one file with only the document_link + document_id
 * and a second file with a document_id + document_link.
 * These files and formats are needed by the ConnectionsWriter.
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class DocDumpParser {

private static DocDumpParser instance = null;
			
private DocDumpParser(){ }
		
public static DocDumpParser getInstance(){
		if(instance == null) {
			instance = new DocDumpParser();
		}
		return instance;
	} 
		
		
	/**
	* reads the Documents Dump consisting of: id (doi or  number id), link to the document, title, and text 
	* and rewrites each row of the dump splitting it into two components (that are written into different files): 
	* 1) link + id
	* 2) id + content
	* @param file the File that contains the doc dump
	* @throws IOException
	*/
	public static void readAndRewriteDocDump(String file) throws IOException{
		// delete files if they exist
		Editor.deleteFile(ProcessingProperties.DOC_IDENTITY_PATH);
		Editor.deleteFile(ProcessingProperties.DOC_TEXTS_PATH);
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
			while(br.ready()){
				String nextLine = br.readLine();
				String[] lineAsArray = nextLine.split("\t");
				if(!nextLine.isEmpty()){
					DocDumpParser.sortAndWriteElementsOfLine(lineAsArray);
				}
			}
			br.close();
		}
		
		catch (IOException e) {
			e.printStackTrace();
		}
	}
		
		/**
		 * processes the elements of one row and writes the link + id to the doc_id file and the id + all the rest of the content to the doc_content file
		 * @param lineElements String[] the array that contains one row of the doc dump file
		 */
		public static void sortAndWriteElementsOfLine(String[] lineElements){
			/*
			 * 0: id/doi
			 * 1: link
			 * 2: title
			 * 3: text
			 * 
			 * needed: id (tab) title + text 
			 */
			String doctext = lineElements[0] + "\t";
			if (!lineElements[2].equals("-")) {
				doctext += lineElements[2] + " ";	
			}
			doctext += lineElements[3];
			
			Writer.appendLineToFile(lineElements[1].trim() + "\t"+ lineElements[0].trim(), ProcessingProperties.DOC_IDENTITY_PATH);
			Writer.appendLineToFile(doctext, ProcessingProperties.DOC_TEXTS_PATH);
		}
		
		
		/**
		 * calls all of the other methods to process the doc dump.
		 */
		public static void process(){
			try {
				DocDumpParser.readAndRewriteDocDump(DocProperties.DOC_DUMP_PATH);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

}
