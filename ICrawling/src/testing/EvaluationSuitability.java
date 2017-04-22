package testing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import crawling_docs.DocProperties;
import edu.stanford.nlp.simple.Sentence;
import io.Reader;
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
	
	/**
	 * This class checks if all used (ggf. unused) terms can be found in all doc texts. 
	 * Crawling: in abstract-only mode.
	 */
	public EvaluationSuitability() {
		readLemmatizedTerms();
		
		
	}
	
	public void readLemmatizedTerms(){
		
		List<String> terms = Reader.readLinesList("used.txt");
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
	
	public static void checkDocDumpForMatches(String allDocsStr, String term, List<Sentence> list){
		int count = 0;
		if(term.contains(" ")){
			Pattern r = Pattern.compile(term);
		    Matcher m = r.matcher(allDocsStr);
		    
		    
	        while (m.find()){
	        	count+=1;
	        }
		} else{

			for(Sentence sentence: list){
				String t = EvaluationSuitability.getUsedTerms().get(term);
				count += Collections.frequency(sentence.lemmas(), t);
			}
			
			/*Pattern r = Pattern.compile(EvaluationSuitability.getUsedTerms().get(term));
		    Matcher m = r.matcher(allDocsStr);
		    
	        while (m.find()){
	        	count+=1;	
	        }*/
	        
	        System.out.println(term + " " + count);
		}
		
		if(count >=1){
			usedInEvaluation +=1;
			
		}
            
	    
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
		EvaluationSuitability eval = new EvaluationSuitability();
		try {
			String texts = eval.readDocDump(DocProperties.DOC_DUMP_PATH);
			List<Sentence> sent = splitDocsInSentences(texts);
			for(String termx: usedTerms.keySet()){
				checkDocDumpForMatches( texts, termx, sent);
			}
			if(usedInEvaluation >0){
				System.out.println(usedInEvaluation/used);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

}
