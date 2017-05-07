package relations_identification;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import crawling_docs.DocProperties;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import linguistic_processing.SentenceSplitter;

public class RelationsFilter {
	private static List<String> possiblePatterns = new ArrayList<String>();
	
	public RelationsFilter() {
		//only conjunction between both terms
		possiblePatterns.add("and|or|of|in|consist.*\\sof|replace.*|link.*\\sto|appear.*\\sto|cause.*");
		possiblePatterns.add("(,\\sand|or\\s+)*");
		possiblePatterns.add("may\\s+be.*");
		// is-a patterns
		// example: is an active compound called ...
		possiblePatterns.add("is\\s+.+|be\\s+.+|was\\s+.+called.+");
		//bow lute, such as Bambara ndang 
		possiblePatterns.add("such\\sas\\s\\w*\\b*(or|and)?\\s\\w*\\b*");
		possiblePatterns.add(",\\ssuch\\sas\\s\\w*\\b*(or|and)?\\s\\w*\\b*");
		possiblePatterns.add("such\\s.*as\\s\\w*\\b*(or|and)?\\s\\w*\\b*");
		//... temples, treasuries, and other important civic buildings
		possiblePatterns.add("(\\w*\\b*),\\s*(\\w*\\b*)*(or|and)?.*");
		//most European countries, especially
		possiblePatterns.add("(\\w*\\b*),\\s*(including|especially)(\\w*\\b*)*(or|and)?.*");
	}
	
	public static boolean matchesFixedConnections(String candidate){
		boolean result = false;
		for(String pattr: possiblePatterns){
			Pattern pattern = Pattern.compile(pattr);
			Matcher m = pattern.matcher(candidate);
			boolean current = m.matches();
			if (current == true){
				result = true;
				break;
			}
			
		}
		
		return result;
		
	}
	
	public static boolean startsWithVP(String candidate){
		boolean result = false;
		if(!candidate.isEmpty() && !candidate.matches("\\s")){
			Sentence sent = new Sentence(candidate);
			// pos tags penn tree bank
		    //https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
		   List<String> pos =  sent.posTags();
		    if(!pos.isEmpty()){
		    	// if the candidate starts with a verb, then it is a verbal phrase
		    	result = pos.get(0).matches("VB|VBD|VBN|VBG|VBZ|VBP|MD");
			}
		}
	    
	    
		return result;
	}
	
	public static boolean startsWithVPAndShort(String candidate){
		boolean result = false;
		if(!candidate.isEmpty() && !candidate.matches("\\s")){
			Sentence sent = new Sentence(candidate);
			// pos tags penn tree bank
		    //https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
		   List<String> pos =  sent.posTags();
		    if(!pos.isEmpty()){
		    	// if the candidate starts with a verb, then it is a verbal phrase
		    	if(pos.get(0).matches("VB|VBD|VBN|VBG|VBZ|VBP|MD") == true && pos.size() > 10){
		    		result = false;
		    	} else{
		    		result = true;
		    	}
		    	
			}
		}
	    
	    
		return result;
	}
	
	public static boolean isOrStartsWithPunct(String candidate){
		boolean result = false;
		if(candidate.startsWith(",") || candidate.matches("(\\p{Punct}+)")){
			result = true;
		}
		return result;
	}
	
	// sometimes when searching for lemmas, only a part of the word is matched. This is already covered by term itself, so it is a duplicate.
	//
	public static boolean isLemmaDuplicate(String candidate){
		boolean result = false;
		if(candidate.matches("s\\s.*")){
			result = true;
		}
		
		return result;
	}

	public static void main(String[] args) {
		String candidate = "s ksiss";
		System.out.println(isLemmaDuplicate(candidate));

	}

	public static List<String> getPossiblePatterns() {
		return possiblePatterns;
	}

	public static void setPossiblePatterns(List<String> possiblePatterns) {
		RelationsFilter.possiblePatterns = possiblePatterns;
	}

}
