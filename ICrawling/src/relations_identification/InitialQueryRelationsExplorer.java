package relations_identification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.Writer;
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
		// punctuation regex is to be improved
		String [] sentences = this.getTextLower().split("[.?!]");
		/*for (String sentence: sentences){
			System.out.println(sentence.trim());
		}*/

		for(String sentence: sentences){
			sentence = sentence.trim();
			String[] sentence_splitted = sentence.split(" ");
			List<String> list = Arrays.asList(sentence_splitted);
			
			String candidate1 = "";
			for (String word: sentence_splitted){
				String processedWord = this.processString(word);
				if(this.wordIsTerm(processedWord)){
					candidate1 = processedWord;
					String whatLiesInBetween = " ";
					List<String> sentence_splitted_rest = list.subList(list.indexOf(word), list.size());
					for (String otherword: sentence_splitted_rest){
						otherword = this.processString(otherword);
						// this would be the candidate for the relation
						if(!wordIsTerm(otherword)){
							whatLiesInBetween = whatLiesInBetween + otherword.trim() + " ";
						}
						
						
						if(wordIsTerm(otherword)){
							if(!candidate1.equals(otherword) && !candidate1.equalsIgnoreCase("")){
								//It is a second term that appears in the sentence
								Relation relation = new Relation();
								relation.setArg1(candidate1);
								relation.setArg2(otherword);
								relation.setRel(whatLiesInBetween);
								this.getRelationsForOneText().add(relation);
								// The actual relation is not yet extracted
							}
						}

					}
				}

			}
		}
		
	}
	
	// A word of a sentence is contained in the topics of a text or the word of a sentence contains a topic.
	public boolean wordIsTerm(String word){
		for(String term: this.getTermsForOneText()){
			
			if (word.equals(term) || word.contains(term)){
				return true;
			}
		}
		return false;
	}
	
	public String processString(String word){
		String punctuation ="(\\p{Punct}+)";
		word = word.replaceAll(punctuation, "");
		word = word.trim();
		return word;
	}

}
