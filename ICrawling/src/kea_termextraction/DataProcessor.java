package kea_termextraction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import crawling_docs.DocProperties;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import io.Reader;
import io.Writer;

public class DataProcessor {
	
	private Set<String> terms = new HashSet<String>();
	private static final String PATH_TO_KEA_TERMS = "kea_terms.txt";
	
	public void readAndRewriterNFDump(){
		List<String> linesOfDump = Reader.readLinesList("nfdump.txt");
		for (int i=0;i< linesOfDump.size();i++) {
			String line = linesOfDump.get(i);
			String[] elements = line.split("\t");
			String id = elements[0].trim();
			
			String processedDoc = elements[3];
			
			// inserts a whitespace after a sentence if there is none.
			processedDoc = processedDoc.replaceAll("([\\p{Lower}\\d\\\\p{Punct}][,.!?;:])" +
					 "(\\p{Upper})", "$1 $2").replaceAll("\\s+", " ");
			
			
			Writer.writeEmptyFile("testdocs/en/test/" + id + "_.txt");
			Writer.overwriteFile(processedDoc, "testdocs/en/test/" + id + "_.txt");
			
		}
			
	}
	
	public void concatenateKeaTerms(String path){
		File[] files = new File(path).listFiles();
		for (File file: files){
			ArrayList<String> lines = Reader.readLinesList(file.getPath());
			for(String line: lines){
				if(!line.isEmpty()){
					terms.add(line.trim().toLowerCase());
				}
				
			}
		}
		
		Writer.writeEmptyFile(PATH_TO_KEA_TERMS);
		for(String term: terms){
			Writer.appendLineToFile(term, PATH_TO_KEA_TERMS);
		}
	}
	public static void main(String[] args) {
		DataProcessor dp = new DataProcessor();
		//dp.readAndRewriterNFDump();
		dp.concatenateKeaTerms("testdocs/en/test/no vocabulary_test_train");

	}

}
