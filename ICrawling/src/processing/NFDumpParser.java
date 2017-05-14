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
 * A class that divides the final nutrition facts dump into two components: one file with only the querylink + queryid
 * and a second file with a queryid + querycontent.
 * These files and formats are needed by the ConnectionsWriter.
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class NFDumpParser {
	private static NFDumpParser instance = null;
	
	
	private NFDumpParser(){ }
	
	public static NFDumpParser getInstance(){
		 if(instance == null) {
			 instance = new NFDumpParser();
		     }
		 return instance;
	   } 
	
	
	/**
	 * reads the Nutrition facts Dump consisting of: id, link to the query, title, text, comments, topics, description, 
	 * doctor's note, article links, question links, topic links, video links, document links (references), and rewrites
	 * it into two new files: one for only identification and the second for the content of the queries.
	 * 
	 * @param file the File that contains the nf dump
	 * @throws IOException
	 */
	public static void readAndRewriteNFDump() throws IOException{
		// delete files if they exist
		Editor.deleteFile(ProcessingProperties.QUERY_IDENTITY_PATH);
		Editor.deleteFile(ProcessingProperties.QUERY_TEXTS_PATH);
		try {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(DocProperties.FILTERED_NFDUMP_PATH), "UTF-8"));
			while(br.ready()){
				String nextLine = br.readLine();
				String[] lineAsArray = nextLine.split("\t");
				NFDumpParser.sortAndWriteElementsOfLine(lineAsArray);
			}	
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * processes the elements of one row and writes the link + id to the query_id file and the id + all the rest of the content to the query_content file
	 * @param lineElements String[] the array that contains one line of the nf dump file
	 */
	public static void sortAndWriteElementsOfLine(String[] lineElements){
		String querytext = lineElements[0] + "\t" + lineElements[2];

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
		 * Needed text elements, if available: 2, 3, 4, (5, ?) 6, 7
		 * Write in file: id (tab) text 
		 */
		String maintext = "";
		for(int i = 3; i < 8; i++){
			if (lineElements[i].equals("-")) {
				continue;
			}
			maintext += lineElements[i] + " ";
		}
		if (maintext.isEmpty()) {
			return;
		}
		querytext +=  " " + maintext;
		String newDumpLine = String.join("\t", lineElements);
		Writer.appendLineToFile(lineElements[1].trim() + "\t"+ lineElements[0].trim(), ProcessingProperties.QUERY_IDENTITY_PATH);
		Writer.appendLineToFile(querytext, ProcessingProperties.QUERY_TEXTS_PATH);
		Writer.appendLineToFile(newDumpLine, "nfdump_only_with_text.txt");
	}
	
	
	/**
	 * Parses the nf dump and rewrites it by splitting into two files: identity file and content file.
	 * Calls all the other methods of the class.
	 */
	public static void process(){
		try {
			NFDumpParser.readAndRewriteNFDump();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Editor.transferFileName("filtered_nfdump.txt", "nfdump_only_with_text.txt");
	}
}
