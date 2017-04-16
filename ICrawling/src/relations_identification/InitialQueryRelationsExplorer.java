package relations_identification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.simple.Sentence;
import io.Writer;
import linguistic_processing.SentenceSplitter;
import linguistic_processing.StanfordLemmatizer;
import processing.TextPreprocessor;

public class InitialQueryRelationsExplorer extends QueryRelationsExplorer {
	
	
	/*
	 * 1 line = 1 text in the initial dump.
	 */
	public InitialQueryRelationsExplorer(String line){
		readAndInitializeQuery(line);
	}
	
	
	public static void main(String[] args) {

	}

	@Override
	/*
	 * A function that extracts the initial connections here. If a word is connected to a term in 1 sentence (!),
	 * it is extracted as a connection. Problem: lots of the terms doesn't appear in the text. This problem is to be solved with a morphological analyzer. (non-Javadoc)
	 * @see relations_identification.QueryRelationsExplorer#extractRelations()
	 */
	public void extractRelations() {
		// The Stanford Parser should be used for the sentence splitting. It has a more elaborate method to identify a sentence.
		SentenceSplitter splitter = new SentenceSplitter(this.getTextLower());
		//String [] sentences = this.getTextLower().split("[?!.]($|\\s)");
		/*for (Sentence sentence: splitter.getSentences()){
			System.out.println(sentence);
		}*/
		
		for(Sentence sentence: splitter.getSentences()){
			String sentenceString = sentence.toString();
			
			Map<String, String> termMap= this.getTermsForOneText();
			for(String term1: termMap.keySet()){
				for(String term2: termMap.keySet()){
					String candidate = "";
					String candidateLemmas = "";

					if(!term1.equals(term2)){
						// Here: http://stackoverflow.com/questions/11255353/java-best-way-to-grab-all-strings-between-two-strings-regex
						//This will deliver just one match and would possibly contain other terms or the term as well. Is this a problem?
						// Multiword-terms are covered here.
				    	// Here, it is checked if the lemma of term matches a word in the sentence
				    		
				    	// With this approach, we are loosing things like if processed meat is in the text instead of meats -> multiword that is searched for entirely without lemmatization.
						// But also: American heart association isn't recognized as American which would gain too many artificial connections that are not there.
						if(!term1.contains(" ") && !term2.contains(" ")){
				    		candidateLemmas = lookForATermWordMatch(sentenceString, termMap, termMap.get(term1), termMap.get(term2),
									candidateLemmas);
				    	} else if(term1.contains(" ") && !term2.contains(" ")){
				    		candidateLemmas = lookForATermWordMatch(sentenceString, termMap, term1, termMap.get(term2),
									candidateLemmas);
				    	} else if(!term1.contains(" ") && term2.contains(" ")){
				    		candidateLemmas = lookForATermWordMatch(sentenceString, termMap, termMap.get(term1), term2,
									candidateLemmas);
				    	} else if(term1.contains(" ") && term2.contains(" ")){
				    		candidate = lookForATermWordMatch(sentenceString, termMap, term1, term2,
				    				candidate);
				    	} 
				    	
					}

					if(!candidateLemmas.isEmpty()){
						Relation relation = new Relation();
						relation.setArg1(term1);
						relation.setArg2(term2);
						relation.setRel(candidateLemmas.replaceAll("(\\p{Punct}+)",""));
						this.getRelationsForOneText().add(relation);
						
						this.getUsedTerms().put(term1, this.getTermsForOneText().get(term1));
						this.getUsedTerms().put(term2, this.getTermsForOneText().get(term2));
						// The actual relation is not yet extracted
					}
					
					/* Only if it is a multiword term, we need another approach. The one above has already covered lemmas.
					Only something like veggies wouldn't be recognized because it is lemmatized to "veggy" (veggies in text).*/
					if(!candidate.isEmpty()){
						Relation relation = new Relation();
						relation.setArg1(term1);
						relation.setArg2(term2);
						relation.setRel(candidate.replaceAll("(\\p{Punct}+)",""));
						this.getRelationsForOneText().add(relation);
						
						this.getUsedTerms().put(term1, this.getTermsForOneText().get(term1));
						this.getUsedTerms().put(term2, this.getTermsForOneText().get(term2));
						// The actual relation is not yet extracted
					}
					
					
				}
			}
		}
		
	}


	private String lookForATermWordMatch(String sentenceString, Map<String, String> termMap, String term1, String term2,
			String candidate) {
		Matcher matcherLemmas = Pattern.compile(
		        Pattern.quote(term1)
		        + "(.*?)"
		        + Pattern.quote(term2)).matcher(sentenceString);
		while(matcherLemmas.find()){
			String match = matcherLemmas.group(1);
			if(!match.matches("(\\p{Punct}+)")){
				candidate += match;
			}
			
		}
		return candidate;
	}

}
