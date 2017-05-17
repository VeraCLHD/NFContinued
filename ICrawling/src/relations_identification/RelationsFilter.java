package relations_identification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;

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
		possiblePatterns.add("(,\\sand|or\\s\\w?\\b?)*");
		possiblePatterns.add("may\\s+be.*");
		// is-a patterns
		// example: is an active compound called ...
		possiblePatterns.add("(is\\s+.+|be\\s+.+|was\\s+.+)called.+");
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
		if(!candidate.isEmpty() && !candidate.matches("\\s+") && !candidate.equals(" ") && candidate !=null){
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
	
	/**
	 * A method that checks for incomplete nout phrases - if the first or the last elements of a candidate are nouns, the noun phrase is incomplete
	 * example: disease study about heart disease. Candidate: study about heart -> makes no sense.
	 * @param candidate
	 * @return
	 */
	public static boolean startsWithN(List<String> pos, String candidate){
		String pos0 = null;
		boolean result = false;
		
		    if(!pos.isEmpty()){
		    	// avoids extracting incomplete noun phrases: if the first word of candidate is noun or the last is noun or adjective
		    	result = pos.get(0).matches("NN|NNS|NNP|NNPS") || pos.get(pos.size()-1).matches("NN|NNS|NNP|NNPS|JJ|JJR|JJS");
		    	pos0 = pos.get(0);//;
		    }
		
		return result;
	}
	
	public static boolean startsWithVPAndNotOtherSentence(List<String> pos, String candidate){
		boolean result = false;
		
		    if(!pos.isEmpty()){
		    	// if the candidate starts with a verb, then it is a verbal phrase
		    	if(pos.get(0).matches("VB|VBD|VBN|VBG|VBZ|VBP|MD") == true && !candidate.contains(",") && !candidate.contains(";")){
		    		result = true;
		    	}
			
		}
	    
	    
		return result;
	}
	
	public static boolean candidateContainsOtherTerms(String candidate){
		boolean result = false;
		Set<String> set = InitialRelationsManager.allTermsAndVariations;
		
		int index = StringUtils.indexOfAny(candidate, set.toArray(new String[set.size()]));
		    if(index !=-1){
		    	result = true;
			
		}
	    
	    
		return result;
	}
	
	public static boolean isOrStartsWithPunct(String candidate){
		boolean result = false;
		;
		if(candidate.startsWith(",") || candidate.startsWith(";") || candidate.matches("(\\p{Punct}+)")){
			result = true;
		}
		return result;
	}
	
	// When the word is health and the match is healthy, the connection starts with y"
	//
	public static boolean startsWithSingleChar(String candidate){
		boolean result = false;
		if(candidate.matches("\\w(,)?\\s.*")){
			result = true;
		}
		
		return result;
	}

	public static void main(String[] args) {
		String candidate = "and seeds, we eat too much salt, too much processed";
		System.out.println(candidate.matches("(,\\sand|or\\s\\w?\\b?)*"));
		// and seeds, we eat too much salt, too much processed -> and , has to apply only for single words?
		int count = StringUtils.countMatches(candidate, "(\\p{Punct}+)");
		System.out.println(count);
	}

	public static List<String> getPossiblePatterns() {
		return possiblePatterns;
	}

	public static void setPossiblePatterns(List<String> possiblePatterns) {
		RelationsFilter.possiblePatterns = possiblePatterns;
	}

}
