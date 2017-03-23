package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import crawling_docs.DocProperties;
import crawling_queries.Properties;
import processing.ProcessingProperties;
/**
 * A class that handles deletion and modification of files.
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class Editor {
	/**
	 * Deletes the last line of a file. Needed when crawling after interruption.
	 * 
	 * @param filename the file from which the last line is deleted
	 */
	public static void deleteLastLine(String filename) {
		
		RandomAccessFile f;
		try {
			f = new RandomAccessFile(filename, "rw");
			long length = f.length() - 1;
			byte b;
			do {                     
			  length -= 1;
			  f.seek(length);
			  b = f.readByte();
			} while(b != 10);
			f.setLength(length-1);
			f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteFile(String filename) {
		File f = new File(filename);
		f.delete();
	}
	
	/**
	 * Transfers name from one file to another and gives the first one a new name.
	 * @param giver_name file that gives the name
	 * @param receiver_name file that receives the name from giver
	 * @param new_giver_name new file for content of giver
	 */
	public static void transferFileName(String giver_name, String receiver_name, String new_giver_name) {
		File giver = new File(giver_name);
		File receiver = new File(receiver_name);
		File giver_new = new File(new_giver_name);
		giver.renameTo(giver_new);
		giver.delete();
		File new_receiver = new File(giver_name);
		receiver.renameTo(new_receiver);
		receiver.delete();
	}
	
	/**
	 * Transfers name from one file to another and deletes the first file.
	 * @param giver_name file that gives the name and gets deleted
	 * @param receiver_name file that receives the name from giver
	 */
	public static void transferFileName(String giver_name, String receiver_name) {
		File giver = new File(giver_name);
		File receiver = new File(receiver_name);
		giver.delete();
		File new_receiver = new File(giver_name);
		receiver.renameTo(new_receiver);
		receiver.delete();
	}
	
	/**
	 * Creates the folders needed to create and structure the corpus, e.g. crawling queries, docs, processing...
	 */
	public static void createFileStructureForProject(){
		
		File c_docs = new File(DocProperties.DOC_CRAWLING_OUTPUT_PATH);
		File c_querydumps = new File(Properties.PATH_TO_QUERYDUMPS);
		File c_links = new File("crawling_queries/querylinks/");
		File datasets_all = new File("datasets/");
		File complete_queries_shared_docs = new File(ProcessingProperties.DATASETS_PATH + "complete_queries_shared_docs/");
		File no_comments_shared_docs = new File(ProcessingProperties.DATASETS_PATH + "no_comments_shared_docs/");
		File complete_queries_no_shared_docs = new File(ProcessingProperties.DATASETS_PATH + "complete_queries_no_shared_docs/");
		File no_comments_no_shared_docs = new File(ProcessingProperties.DATASETS_PATH + "no_comments_no_shared_docs");
		
		File processing = new File("processing/");
		c_docs.mkdirs();
		c_querydumps.mkdirs();
		c_links.mkdirs();
		datasets_all.mkdirs();
		processing.mkdirs();
		complete_queries_shared_docs.mkdirs();
		no_comments_shared_docs.mkdirs();
		complete_queries_no_shared_docs.mkdirs();
		no_comments_no_shared_docs.mkdirs();
	}


}
