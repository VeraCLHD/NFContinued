package io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * A class containing functions for reading files.
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class Reader {
	
	/**
	 * Reads a file line-wise and returns a list of the lines.
	 */
	
	public static ArrayList<String> readLinesList(String filename) {
		ArrayList<String> lines = new ArrayList<String>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"))) {
			String line = new String();
			while (br.ready()) {
				line = br.readLine();
				lines.add(line);
			}
			
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	
	/**
	 * Reads an prints lines of a file.
	 */
	
	public static void readAndPrintLinesFromFile(String filename) {
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(Reader.class.getResourceAsStream(filename), "UTF-8"))) {
			while (br.ready()) {
				System.out.println(br.readLine());
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Returns complete content of a file as a string.
	 */
	
	public static String readContentOfFile(String filename){
		String content = new String();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"))) {
			
			while (br.ready()) {
				String line = br.readLine();
				content += line;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return content;
		
	}
	
	/**
	 * Reads the last line of a file by detecting the last "\r".
	 * Should be tested with "\n" so that "\r" can be omitted in the whole project.
	 */
	
	public static String readLastLine(String filename) {
		RandomAccessFile raf;
		byte content;
		String lastline = "";
		try {
			raf = new RandomAccessFile(filename, "r");
			long position = raf.length() - 1;
			raf.seek(position);
			while (true) {
				position -= 1;
				content = raf.readByte();
				// 13: ascii code for /r
				if (content == 13) {
					raf.readByte();
					break;
				}
				raf.seek(position);
			}
			lastline = raf.readLine();
			raf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lastline;
	}
	
}
