package relations_identification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		// TODO Auto-generated method stub

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
		StanfordLemmatizer lemm = new StanfordLemmatizer();
		
		for(Sentence sentence: splitter.getSentences()){
			// At this point a single word can also be punctuation.
			List<String> sentence_splitted = sentence.words();
			String candidate1 = "";
			String candidateLemma = "";
			for (String word1: sentence_splitted){
				//String processedWord = this.processString(word);
				String wordLemma = lemm.lemmatize(word1);
				if(!word1.matches("(\\p{Punct}+)") && this.wordIsTerm(word1, wordLemma)){
					
					candidate1 = word1;
					candidateLemma = wordLemma;
					String whatLiesInBetween = " ";
					List<String> sentence_splitted_rest = sentence_splitted.subList(sentence_splitted.indexOf(word1), sentence_splitted.size());
					for (String otherword: sentence_splitted_rest){
						// this would be the candidate for the relation
						String otherLemma = lemm.lemmatize(otherword);
						if(!wordIsTerm(otherword, otherLemma) && !otherword.matches("(\\p{Punct}+)")){
							whatLiesInBetween = whatLiesInBetween + otherword.trim() + " ";
						}
						
						
						if(wordIsTerm(otherword, otherLemma)){
							if(!candidate1.equals(otherword) && !candidate1.equalsIgnoreCase("")){
								//It is a second term that appears in the sentence
								Relation relation = new Relation();
								relation.setArg1(candidate1);
								relation.setArg2(otherword);
								relation.setRel(whatLiesInBetween);
								this.getRelationsForOneText().add(relation);
								
								this.getUsedTerms().put(candidate1, candidateLemma);
								this.getUsedTerms().put(otherword, otherLemma);
								// The actual relation is not yet extracted
							}
						}

					}
				}

			}
		}
		
	}
	
	// A word of a sentence contains a topic of a text or the word is the term.
	public boolean wordIsTerm(String word, String lemma){
		
		if(!word.isEmpty() && !word.matches("(\\p{Punct}+)")){
			Map<String, String> termMap = this.getTermsForOneText();
			if(termMap.keySet().contains(word) || termMap.values().contains(lemma)){
				return true;
			}
			//|| word.contains(term) fehlt
		}
		
		return false;
	}

}
