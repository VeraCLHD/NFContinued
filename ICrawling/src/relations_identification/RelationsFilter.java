package relations_identification;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
		possiblePatterns.add("and|or|consist.*\\sof|replace.*|link.*\\sto|.?appear.*\\sto|cause.*");
		possiblePatterns.add("than\\s?");

	}
	
	public static boolean isInfluence(String candidate, Relation relation){
		boolean result = false;
		List<String> influence = new ArrayList<String>();
		// is-a patterns
		//bow lute, such as Bambara ndang 
		influence.add("(,\\s)?may\\s+be.*");
		influence.add("(,\\s)?(may|appear.*)\\s(\\w*\\b){0,6}");
		influence.add("(and|or)?\\s?(link|appear).*\\sto|cause.*");
		influence.add("(and|or)?\\s?(link|appear|cause).*");
		
		for(int i=0; i< influence.size();i++){
			Pattern pattern = Pattern.compile(influence.get(i));
			Matcher m = pattern.matcher(candidate);
			boolean current = m.matches();
			if (current == true){
				result = true;
				relation.setTypeOfRelation("INFLUENCE");
				break;
				
			}
			
		}

		return result;
	}
	
	public static boolean isARelation(String candidate, Relation relation){
		boolean result = false;
		List<String> isA = new ArrayList<String>();
		// is-a patterns
		//bow lute, such as Bambara ndang 
		isA.add("such\\sas\\s?");
		isA.add("(\\p{Punct})?\\ssuch\\sas\\s(\\w*\\b)(or|and)?\\s(\\w*\\b)");
		isA.add("such\\s.*as\\s(\\w*\\b)(or|and)?\\s(\\w*\\b)");
		
		// is-a patterns
		// example: is an active compound called ...
		isA.add("(is\\s+.+|be\\s+.+|was\\s+.+)called.+");
		isA.add("(\\p{Punct})?\\s?called.+");
		isA.add("(\\p{Punct})?\\s?is|was\\s?");
		//most European countries, especially
		isA.add("(\\w*\\b)?,\\s+(including|especially)(\\w*\\b)*(or|and)?.*");
		isA.add("like\\s?");
		
		for(int i=0; i< isA.size();i++){
			Pattern pattern = Pattern.compile(isA.get(i));
			Matcher m = pattern.matcher(candidate);
			boolean current = m.matches();
			if (current == true){
				result = true;

				relation.setTypeOfRelation("IS-A");
				break;
				
			}
			
		}

		return result;
	}
	
	public static boolean matchesFixedConnections(String candidate, Relation relation){
		boolean result = false;
		for(String pattr: possiblePatterns){
			Pattern pattern = Pattern.compile(pattr);
			Matcher m = pattern.matcher(candidate);
			boolean current = m.matches();
			if (current == true){
				result = true;
				relation.setTypeOfRelation("OTHER");
				break;
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
	public static boolean isIncompleteNP(List<String> pos, String candidate){
		
		boolean result = false;
		
		    if(!pos.isEmpty()){
		    	// avoids extracting incomplete noun phrases: if the first word of candidate is noun or the last is noun or adjective
		    	// VBG in the beginning: avoids poultry "producing" states as an incomplete NP
		    	result = pos.get(0).matches("NN|NNS|NNP|NNPS|VBG") || pos.get(pos.size()-1).matches("NN|NNS|NNP|NNPS|POS|JJ|JJR|JJS");

		    }
		
		return result;
	}
	
	public static boolean startsWithVPAndNotOtherSentence(List<String> pos, String candidate, Relation relation){
		boolean result = false;
		
		    if(!pos.isEmpty()){
		    	// if the candidate starts with a verb, then it is a verbal phrase
		    	if(pos.get(0).matches("VB|VBD|VBN|VBG|VBZ|VBP|MD") == true && !candidate.contains(",") && !candidate.contains(";")){
		    		result = true;
		    		relation.setTypeOfRelation("VP");
		    	}
			
		}
	    
	    
		return result;
	}
	
	public static boolean startsWithPrepAndNotOtherSentence(List<String> pos, String candidate, Relation relation){
		boolean result = false;
		
		    if(!pos.isEmpty()){
		    	// if the candidate starts with a verb, then it is a verbal phrase
		    	if(pos.get(0).matches("IN|TO") == true && !candidate.contains(",") && !candidate.contains(";")){
		    		result = true;
		    		relation.setTypeOfRelation("PREP");
		    	}
			
		}
	    
	    
		return result;
	}
	
	public static boolean startsWithAdjAndNotOtherSentence(List<String> pos, String candidate, Relation relation){
		boolean result = false;
		
		    if(!pos.isEmpty()){
		    	// if the candidate starts with a verb, then it is a verbal phrase
		    	if(pos.get(0).matches("JJ|JJR|JJS") == true && !candidate.contains(",") && !candidate.contains(";")){
		    		result = true;
		    		relation.setTypeOfRelation("ADJ");
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

	
	
	public static boolean isOrStartsWithRelevantPunct(String candidate, Relation relation){
		boolean result = false;
		
		if(candidate.contains(":") || candidate.contains(";") || candidate.contains("!") || candidate.contains("?")){
			result = false;
		}
		// include conjunctions via "&"
		//",\\s(\\w*\\b)?\\s?(&|and|or)?\\s?(\\w*\\b)*"
		//... temples, treasuries, and other important civic buildings

		if(candidate.matches("&") 
				|| candidate.matches(",\\s?(\\w*\\b)?,?(&|and|or)?\\s?(\\w*\\b)*") 
				|| candidate.matches(",?\\s?as\\swell\\sas\\s?") 
				|| candidate.matches("(\\w*\\b),\\s+(\\w*\\b)*(or|and).*")
				|| candidate.matches(",?\\s?&|and|or\\s?,?")){
			relation.setTypeOfRelation("AND-CONJ");
			result = true;
		} 
		// series of items should be included as well -> relevant
		if (candidate.matches(",\\s?(\\w*\\b)*\\s?,?\\s?") || candidate.matches("(\\w*\\b),\\s+(\\w*\\b)*,.*")){
			relation.setTypeOfRelation("LIST");
			result = true;
		}
		
		
		if (candidate.equals("-")){
			relation.setTypeOfRelation("-COMPOUND");
			result = true;
				}
		
		return result;
	}
	
	// When the word is health and the match is healthy, the connection starts with y"
	//
	public static boolean isSingleChar(String candidate){
		boolean result = false;
		if(candidate.matches("\\w(,)?\\s.*")){
			result = true;
		}
		
		return result;
	}
	

	public static boolean isEmpty(String candidate, Relation relation){
		boolean result = false;
		
		if(candidate.isEmpty() || candidate.matches("\\s+") || candidate.equals(" ")){
			result = true;
			// STRONG stands for nothing between the terms
			relation.setTypeOfRelation("STRONG");
		}
		
		return result;
		
			
	}
	
	public static boolean isPreposition(String candidate, Relation relation, List<String> pos){
		boolean result = false;
		// example: of, in
		if(!pos.isEmpty()){
	    	// if the candidate starts with a verb, then it is a verbal phrase
	    	if(pos.get(0).matches("IN|TO") == true && pos.size() == 1){
	    		
	    		result = true;
	    		relation.setTypeOfRelation("PREP");
	    	} 
	    	// example: of the, in the, in our
	    	if(pos.get(0).matches("IN|TO") == true && pos.size()> 1 && pos.get(1).matches("DT|WP$|PRP$|PRP")){
	    		
	    		result = true;
	    		relation.setTypeOfRelation("PREP");
	    	}
		}
		
		return result;
	
	}
	
	public static boolean isCoordinatingConjunction(String candidate, Relation relation, List<String> pos){
		boolean result = false;
		
		if(!pos.isEmpty()){
	    	// if the candidate starts with a verb, then it is a verbal phrase
	    	if(pos.get(0).matches("CC") == true && pos.size() == 1){
	    		
	    		result = true;
	    		relation.setTypeOfRelation("CC");
	    	}
		}
		
		return result;
	
	}
	
	public static void main(String[] args) {
		String candidate = "One person can eat ten chicken nuggets a day and have an LDL cholesterol of 90; another person eating ten a day could start out with an LDL of 120. ";
		List<String> pos =  new ArrayList<String>();
		// annotate POS
		if(!candidate.isEmpty() && !candidate.matches("\\s+") && !candidate.equals(" ") && candidate !=null){
			Sentence sent = new Sentence(candidate);
				// pos tags penn tree bank
				//https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
				// for checking of the POS tags -> candidate starts with NNS for example, we don't need the terms themselves.
			// This is done only to make sure we have the correct tags.
			List<String> posTest =  sent.posTags();
			if(!candidate.matches("(\\p{Punct}+)") && posTest.size() != 0){
				pos =  sent.posTags();
			}
			
				
			}
		System.out.println(RelationsFilter.isIncompleteNP(pos, candidate));
		// and seeds, we eat too much salt, too much processed -> and , has to apply only for single words?
		//int count = StringUtils.countMatches(candidate, "(\\p{Punct}+)");
		//System.out.println(count);
		System.out.println(pos.toString());
		
		/*int i = 5;
		int ii = 1;
		if((ii>5|| ii>0) && i>4 ){
			System.out.println(true);
		}*/

	}
	


	public static List<String> getPossiblePatterns() {
		return possiblePatterns;
	}

	public static void setPossiblePatterns(List<String> possiblePatterns) {
		RelationsFilter.possiblePatterns = possiblePatterns;
	}

}
