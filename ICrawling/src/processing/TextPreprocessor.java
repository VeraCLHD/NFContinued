package processing;

import io.Editor;
import io.Writer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * A class responsible for processing the texts of queries and documents:
 * - Stop words removal
 * - Punctuation removal
 * - Number replacement with „NUM“
 * - Lowercasing
 * - Multiple Spaces removal

 * @author Vera Boteva, Demian Gholipour
 *
 */
public class TextPreprocessor {
	/*
	 * a pattern that matches all non-readable and not relevant characters characters.
	 * Relevant characters are punctuation, words and numbers.
	 * Examples for non-readable characters are: ...
	 */
	public static final Pattern permittedCharactersForDocument = Pattern.compile("[\\s*\\p{Punct}*\\s*\\w*\\s*\\p{Punct}*\\s*\\w*\\s*]*");
	public static final String punctuationBeforeWord ="(\\s+)(\\p{Punct}+)(\\w+)";
	public static final String punctuationAfterWord ="(\\w+)(\\p{Punct}+)(\\s+)";
	public static final String whitespaceBeforePunctuation = "(\\s+)(\\p{Punct}+)";
	public static final String whitespaceAfterPunctuation = "(\\p{Punct}+)(\\s+)";
	public static final String numReplacement = "(\\p{Punct}*)(\\s*)(\\p{Punct}*)(\\d+.?,?\\d*)(\\p{Punct}*)(\\s*)";
	public static Set<String> stoppwords = new HashSet<String>();
	
	public static Set<String> getStoppwords() {
		return stoppwords;
	}

	public static void setStoppwords(Set<String> stoppwords) {
		TextPreprocessor.stoppwords = stoppwords;
	}
	
	/**
	 * processes every line of the doc_dump by doing the following:
	 * - removing non-permitted characters: permitted are only words, punctuation and numbers.
	 * - tokenizing the text: adding whitespaces before and after punctuation if punctuation is at the beginning or end of the word.
	 * - setting the text to lowercase
	 * - replacing multiple whitespaces with only one
	 * - removing the stoppwords
	 * @param in_path the path to the file to be processed
	 * @param out_path the output path
	 */
	public static void processText(String in_path, String out_path){
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(in_path), "UTF-8"))){
			while (br.ready()) {
				String line = br.readLine();
				String[] components = line.split("\t");
				String id = components[0];
				String text = components[1];
				StringBuilder new_text = new StringBuilder();
				
				
				Matcher m = TextPreprocessor.permittedCharactersForDocument.matcher(text);
				while(m.find()){
					new_text.append(m.group() + " ");
				}
				
				text = new_text.toString();
				text = TextPreprocessor.removePunctuationBeforeAndAfterWords(text);
				text = TextPreprocessor.removeStandAlonePunctuation(text);
				text = TextPreprocessor.replaceAllStandAloneNumbers(text);
				text = text.toLowerCase();
				text = TextPreprocessor.removeStoppwords(text);
				text = text.replaceAll("\\s+", " ");
				// has to be escaped, otherwise not replaced just by the regex
				text = text.replaceAll("\"", " ");
				text = text.replaceAll(",", " ");
				text = text.replaceAll("\\.", " ");
				text = text.replaceAll("\\?", " ");
				text = text.replaceAll("!", " ");
				text = text.replaceAll("\\(", " ");
				text = text.replaceAll("\\)", " ");
				// because testing with linux tools not possible otherwise
				text = text.replaceAll("\r", "");
				Writer.appendLineToFile(id + "\t" + text, out_path);
			}
			
			br.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Extracts stoppwords from a given stoppwords file.
	 */
	public static void extractStoppwords(){
		String stoppwords_file = ProcessingProperties.PATH_TO_STOPPWORDSLIST;
		try(BufferedReader br = new BufferedReader(new InputStreamReader(TextPreprocessor.class.getResourceAsStream(stoppwords_file), "UTF-8"))){
			while(br.ready()){
				TextPreprocessor.getStoppwords().add(br.readLine().trim());
			}
			
			br.close();
		}
		
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Removes stoppwords from a string
	 * @param input the string to be processed
	 * @return the string without stoppwords
	 */
	public static String removeStoppwords(String input){
		
		String line_without_stoppwords = "";
		String[] line = input.split(" ");
		for(String word: line){
			if(!TextPreprocessor.getStoppwords().contains(word)){
				
				line_without_stoppwords += word + " ";
			}
		}
		
		return line_without_stoppwords;
	}
	
	/**
	 * removes punctuation before and after words
	 * @param input string to be processed
	 * @return string without the punctuation before and after words
	 */
	public static String removePunctuationBeforeAndAfterWords(String input){
		input = input.replaceAll(TextPreprocessor.punctuationBeforeWord, "$1$3");
		input = input.replaceAll(TextPreprocessor.punctuationAfterWord, "$1$3");
		return input;
		
	}
	/**
	 * removes standalone punctuation
	 * @param input string to be processed
	 * @return string without standalone punctuation
	 */
	public static String removeStandAlonePunctuation(String input){
		String result = input;
		result = input.replaceAll(TextPreprocessor.whitespaceBeforePunctuation, "$1");
		
		result = input.replaceAll(TextPreprocessor.whitespaceAfterPunctuation, "$2");
		
		return result;
	}
	
	/**
	 * replaces all numbers with "NUM"
	 * @param input string to be processed
	 * @return the inputstring with "num" instead of numbers
	 */
	public static String replaceAllStandAloneNumbers(String input){
		return input.replaceAll(TextPreprocessor.numReplacement, " NUM ");
	}
	
	/**
	 * Preprocesses documents.
	 */
	public static void preProcessDocs(){
		Editor.deleteFile(ProcessingProperties.PREPROCESSED_DOCS_PATH);
		TextPreprocessor.extractStoppwords();
		TextPreprocessor.processText(ProcessingProperties.DOC_TEXTS_PATH, ProcessingProperties.PREPROCESSED_DOCS_PATH);
		Editor.transferFileName(ProcessingProperties.DOC_TEXTS_PATH, ProcessingProperties.PREPROCESSED_DOCS_PATH);
	}
	
	/**
	 * Preprocesses queries
	 * @param input_queries_path the input path to the queries
	 * @param output_queries_path the output path to the queries
	 */
	public static void preProcessQueries(String input_queries_path, String output_queries_path){
		Editor.deleteFile(output_queries_path);
		TextPreprocessor.extractStoppwords();
		TextPreprocessor.processText(input_queries_path, output_queries_path);
		Editor.transferFileName(input_queries_path, output_queries_path);
	}
	
}
