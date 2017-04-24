package testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import crawling_docs.DocProperties;
import crawling_docs.PDFCrawler;
import edu.stanford.nlp.simple.Sentence;
import io.Reader;
import io.Writer;
import linguistic_processing.SentenceSplitter;
import linguistic_processing.StanfordLemmatizer;
import processing.DocDumpParser;

public class EvaluationSuitability {
	// From initial document
	public static int termsOverall = 0;
	public static int used = 0;
	public static int unused = 0;
	public static int termsInEvaluationSource = 0;
	public static int usedInEvaluation = 0;
	public static int unusedInEvaluation = 0;
	
	private static Map<String,String> unusedTerms = new HashMap<String, String>();
	private static Map<String,String> usedTerms = new HashMap<String, String>();
	private static StanfordLemmatizer lemm = new StanfordLemmatizer();
	
	private static Map<String, Integer> termCount = new HashMap<String, Integer>();
	
	private static final String PATH_TO_NOT_DIE = "evaluation/How Not to Die.pdf";
	
	
	/**
	 * This class checks if all used (ggf. unused) terms can be found in all doc texts. 
	 * Crawling: in abstract-only mode.
	 */
	public EvaluationSuitability() {
		readLemmatizedTerms();
		
		
	}
	
	public void readLemmatizedTerms(){
		
		List<String> terms = Reader.readLinesList("used_terms.txt");
		for(String termLine: terms){
			if(!termLine.isEmpty()){
				String[] line = termLine.split("\t");
				String term =line[0].trim();
				String termLemma =line[1].trim();
				usedTerms.put(term, termLemma);
				used +=1;
			}

		}
		
	}
	
	public String readDocDump(String file) throws IOException{
		// Lowercase doctects with punctuation
		String allRawDocTexts = "";
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
			while(br.ready()){
				String nextLine = br.readLine();
				String[] lineAsArray = nextLine.split("\t");
				if(!nextLine.isEmpty()){
					String textLower = lineAsArray[2] + " " + lineAsArray[3].toLowerCase();
					allRawDocTexts += textLower;
					allRawDocTexts += "\r\n";
				}
			}
			br.close();
		}
		
		catch (IOException e) {
			e.printStackTrace();
		}
		return allRawDocTexts;
	}
	
	public String readPDFText(String file) throws IOException{
		// Lowercase doctects with punctuation
		String allRawDocTexts = Reader.readContentOfFile(file);
		
		return allRawDocTexts;
	}
	
	public static void checkDocDumpForMatches(String allDocsStr, String term, List<Sentence> list){
		
		if(term.contains(" ")){
			Pattern r = Pattern.compile(term);
		    Matcher m = r.matcher(allDocsStr);
		    
		    
	        while (m.find()){
	        	Integer count = EvaluationSuitability.getTermCount().get(term);
	        	if(count != null){
	        		EvaluationSuitability.getTermCount().put(term, count+1);
	        	}
	        	EvaluationSuitability.getTermCount().put(term, 1);
	        }
		} else{

			for(Sentence sentence: list){
				String t = EvaluationSuitability.getUsedTerms().get(term);
				Integer count = EvaluationSuitability.getTermCount().get(term);
				int counts = Collections.frequency(sentence.lemmas(), t);
				if(count != null){
					EvaluationSuitability.getTermCount().put(term, count + counts);
				}
				
				EvaluationSuitability.getTermCount().put(term, 0 + counts);
			}
			
			/*Pattern r = Pattern.compile(EvaluationSuitability.getUsedTerms().get(term));
		    Matcher m = r.matcher(allDocsStr);
		    
	        while (m.find()){
	        	count+=1;	
	        }*/
	        
		}
		
		
		 
		/*if(finalC >=1){
			usedInEvaluation +=1;
			
		}*/
            
	    
		/*Solution fot relations
		 * Matcher matcherLemmas = Pattern.compile(
		        Pattern.quote(term)).matcher(allDocsStr);
		
		while(matcherLemmas.find()){
			String match = matcherLemmas.group(1);
			if(!match.matches("(\\p{Punct}+)")){
				candidate += match;
			}
			
		}*/
	}
	
	public static List<Sentence> splitDocsInSentences(String allDocsStr){
		SentenceSplitter splitter = new SentenceSplitter(allDocsStr);
		return splitter.getSentences();
	}
	
	

	public static void main(String[] args) {
		//EvaluationSuitability.readPDFIntoFile(PATH_TO_NOT_DIE);
		Writer.overwriteFile("", "evaluation_suitability.txt");
		
			// Here, the docdump is evaluated.
			/*String texts = eval.readDocDump(DocProperties.DOC_DUMP_PATH);
			List<String> lines = Reader.readLinesList(DocProperties.DOC_DUMP_PATH);
			String texts = eval.readDocDump(DocProperties.DOC_DUMP_PATH);*/
			//Here, the pdf
			String texts = Reader.readContentOfFile("pdf_test.txt");
			System.out.println("Read PDF text");
			List<String> lines = Reader.readLinesList("pdf_test.txt");
			System.out.println("read lines");
			evaluateSource(texts, lines);
			

	}

	private static void evaluateSource(String texts, List<String> lines) {
		int numterms = 0;
		for(String line: lines){
			numterms +=1;
			System.out.println(numterms);
		
				for(String termx: usedTerms.keySet()){
					List<Sentence> sent = splitDocsInSentences(line);
					checkDocDumpForMatches( texts, termx, sent);
			}
		}
		
		/*if(usedInEvaluation >0){
			System.out.println(usedInEvaluation/used);
		}*/
		// final count for a all documents
		for(String termC: EvaluationSuitability.getTermCount().keySet()){
			Integer finalC = EvaluationSuitability.getTermCount().get(termC);
			Writer.appendLineToFile(termC + "\t" + finalC, "evaluation_suitability.txt");
		}
		
		Writer.appendLineToFile("USED_TERMS" + "\t" + used, "evaluation_suitability.txt");
		Writer.appendLineToFile("USED_TERMS_EVAL" + "\t" + usedInEvaluation, "evaluation_suitability.txt");
	}
	
	public static String readPDFIntoFile(String path){
			String text = "";
			PDDocument pdf;
			
			try {
				pdf = PDDocument.load(path);
				if (pdf.isEncrypted()) {
			        try {
			        	pdf.decrypt("");
			        	pdf.setAllSecurityToBeRemoved(true);
			        }
			        catch (Exception e) {
			            System.err.println("The document is encrypted, and we can't decrypt it.");
			        }
			    }
				PDFTextStripper pdf_stripper = new PDFTextStripper();		
				text += pdf_stripper.getText(pdf);
				pdf.close();
			} catch (IOException e) {
				 System.err.println("IOException");
			}
			Writer.writeEmptyFile("pdf_test.txt");
			Writer.overwriteFile(text, "pdf_test.txt");
			return text;
	}

	public static Map<String,String> getUnusedTerms() {
		return unusedTerms;
	}

	public static void setUnusedTerms(Map<String,String> unusedTerms) {
		EvaluationSuitability.unusedTerms = unusedTerms;
	}

	public static Map<String,String> getUsedTerms() {
		return usedTerms;
	}

	public static void setUsedTerms(Map<String,String> usedTerms) {
		EvaluationSuitability.usedTerms = usedTerms;
	}

	public static StanfordLemmatizer getLemm() {
		return lemm;
	}

	public static void setLemm(StanfordLemmatizer lemm) {
		EvaluationSuitability.lemm = lemm;
	}

	public static Map<String, Integer> getTermCount() {
		return termCount;
	}

	public static void setTermCount(Map<String, Integer> termCount) {
		EvaluationSuitability.termCount = termCount;
	}

}
