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
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
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
		String str = "The coronary such as heart is la.";
		SentenceSplitter splitter = new SentenceSplitter(str);
		
		for(Sentence sentence: splitter.getSentences()){
			System.out.println(sentence.parse());
			for(Tree t: sentence.parse()){
				// get constituents of leaves funktioniert nicht
				//System.out.println("CONSTITUENTS" + t.constituents());
				//System.out.println("PARENT " + t.parent());
			String s = "NP < \"such as\" <NP";
			TregexPattern p = TregexPattern.compile(s);
			TregexMatcher m = p.matcher(sentence.parse());
			while (m.find()) {
				   m.getMatch().pennPrint();
				   //System.out.println(m.getMatch().getLeaves());
			}
		}
			
		}
		
	}

	@Override
	/*
	 * A function that extracts the initial connections here. If a word is connected to a term in 1 sentence (!),
	 * it is extracted as a connection. Problem: lots of the terms doesn't appear in the text. This problem is solved with a morphological analyzer (CatVar)
	 * @see relations_identification.QueryRelationsExplorer#extractRelations()
	 */
	public void extractRelations() {
		// The Stanford Parser should be used for the sentence splitting. It has a more elaborate method to identify a sentence.
		SentenceSplitter splitter = new SentenceSplitter(this.getTextLower());
		
		Set<Term> termSet = InitialRelationsManager.getTerms();
		
		for(Sentence sentence: splitter.getSentences()){
			String sentenceString = sentence.toString();
			List<String> lemmas = sentence.lemmas();
			
			for(Term term1: termSet){
				for(Term term2: termSet){
					
					if(!term1.equals(term2)){
						// first and second term are not multi-words
						
				    		// terms themselves
							String stdCase = lookForATermWordMatch(sentenceString, term1.getOriginalTerm(), term2.getOriginalTerm());
							extractRelation(term1, term1.getOriginalTerm(), term2, term2.getOriginalTerm(), stdCase);
							
							handleMorphoVariations(sentenceString, term1, term2);
							if(!term1.getOriginalTerm().contains(" ") && !term2.getOriginalTerm().contains(" ")){
								String caseLemma = lookForATermWordMatch(sentenceString, term1.getLemma(), term2.getLemma());
								extractRelation(term1, term1.getLemma(), term2, term2.getLemma(), caseLemma);
							} else if(term1.getOriginalTerm().contains(" ") && !term2.getOriginalTerm().contains(" ")){
								String caseLemma = lookForATermWordMatch(sentenceString, term1.getOriginalTerm(), term2.getLemma());
								extractRelation(term1, term1.getOriginalTerm(), term2, term2.getLemma(), caseLemma);
							} else if(!term1.getOriginalTerm().contains(" ") && term2.getOriginalTerm().contains(" ")){
								String caseLemma = lookForATermWordMatch(sentenceString, term1.getLemma(), term2.getOriginalTerm());
								extractRelation(term1, term1.getLemma(), term2, term2.getOriginalTerm(), caseLemma);
							} 
								
							}
							
	 
					}
					
					

						// Here: http://stackoverflow.com/questions/11255353/java-best-way-to-grab-all-strings-between-two-strings-regex
						//This will deliver just one match and would possibly contain other terms or the term as well. Is this a problem?
						// Multiword-terms are covered here.
				    	// Here, it is checked if the lemma of term matches a word in the sentence
				    		
				    	// With this approach, we are loosing things like if "processed meat" is in the text instead of "meats" -> multiword that is searched for entirely without lemmatization.
						// But also: American heart association isn't recognized as American which would gain too many artificial connections that are not there
					
					
				}
			}
		
		
	}


	private void handleMorphoVariations(String sentenceString, Term term1, Term term2) {
		//morph. variations of both terms
		List<String> vars1 = term1.getCatvariations();
		vars1.addAll(term1.getMesh());
		List<String> vars2 = term2.getCatvariations();
		vars2.addAll(term2.getMesh());
		
		if(vars1.isEmpty() && !vars2.isEmpty()){
			for(String morpho2: vars2){
				String morphoCase = lookForATermWordMatch(sentenceString, term1.getOriginalTerm(), morpho2);
				if(!morphoCase.isEmpty()){
					extractRelation(term1, term1.getOriginalTerm(), term2, morpho2, morphoCase);
				}
			}
		} else if(vars2.isEmpty() && !vars1.isEmpty()){
			for(String morpho1: vars1){
				String morphoCase = lookForATermWordMatch(sentenceString, morpho1, term2.getOriginalTerm());
				if(!morphoCase.isEmpty()){
					extractRelation(term1, morpho1, term2, term2.getOriginalTerm(), morphoCase);
				}
			}
		} else if(!vars2.isEmpty() && !vars1.isEmpty()){
			for(String morpho1: vars1){
				for(String morpho2: vars2){
					String morphoCase = lookForATermWordMatch(sentenceString, morpho1, morpho2);
					if(!morphoCase.isEmpty()){
						extractRelation(term1, morpho1, term2, morpho2, morphoCase);
					}
				}
				
			}
		}
	}


	private void extractRelation(Term term1, String var1, Term term2, String var2, String candidate) {
		Relation relation = new Relation();
		//found like this
		relation.setArg1(var1);
		//original term
		relation.setArg1Origin(term1.getOriginalTerm());
		relation.setArg2(var2);
		relation.setArg1Origin(term2.getOriginalTerm());
		// punctuation commented out .replaceAll("(\\p{Punct}+)","")
		relation.setRel(candidate);
		this.getRelationsForOneText().add(relation);
		
		InitialRelationsManager.getUsedTerms().put(term1.getOriginalTerm(), term1.getLemma());
		InitialRelationsManager.getUsedTerms().put(term2.getOriginalTerm(), term2.getLemma());
		// The actual relation is not yet extracted
	}
	
	private String lookForATermWordMatch(String sentenceString, String term1, String term2) {
		String candidate = "";
		Matcher matcher = Pattern.compile(
		        Pattern.quote(term1)
		        + "(.*?)"
		        + Pattern.quote(term2)).matcher(sentenceString);
		
		while(matcher.find()){
			String match = matcher.group(1);
			candidate += match;
			/*if(!match.matches("(\\p{Punct}+)")){
				
			}*/
			
		}
		return candidate;
	}
	
	/*@Override
	 * A function that extracts the initial connections here. If a word is connected to a term in 1 sentence (!),
	 * it is extracted as a connection. Problem: lots of the terms doesn't appear in the text. This problem is solved with a morphological analyzer (CatVar)
	 * @see relations_identification.QueryRelationsExplorer#extractRelations()
	 */
	
	/*
	 * public void extractRelations() {
		// The Stanford Parser should be used for the sentence splitting. It has a more elaborate method to identify a sentence.
		SentenceSplitter splitter = new SentenceSplitter(this.getTextLower());
		//String [] sentences = this.getTextLower().split("[?!.]($|\\s)");
		
		for(Sentence sentence: splitter.getSentences()){
			String sentenceString = sentence.toString();
			
			Set<Term> termMap = InitialRelationsManager.getTerms();
			for(Term term1: termMap){
				for(Term term2: termMap){
					String candidate = "";
					String candidateLemmas = "";

					if(!term1.equals(term2)){
						// Here: http://stackoverflow.com/questions/11255353/java-best-way-to-grab-all-strings-between-two-strings-regex
						//This will deliver just one match and would possibly contain other terms or the term as well. Is this a problem?
						// Multiword-terms are covered here.
				    	// Here, it is checked if the lemma of term matches a word in the sentence
				    		
				    	// With this approach, we are loosing things like if "processed meat" is in the text instead of "meats" -> multiword that is searched for entirely without lemmatization.
						// But also: American heart association isn't recognized as American which would gain too many artificial connections that are not there.
						if(!term1.getOriginalTerm().contains(" ") && !term2.getOriginalTerm().contains(" ")){
				    		candidateLemmas = lookForATermWordMatch(sentenceString, term1.getOriginalTerm(), term2.getOriginalTerm(),
									candidateLemmas);
				    	} else if(term1.getOriginalTerm().contains(" ") && !term2.getOriginalTerm().contains(" ")){
				    		candidateLemmas = lookForATermWordMatch(sentenceString, term1.getOriginalTerm(), term2.getLemma(),
									candidateLemmas);
				    	} else if(!term1.getOriginalTerm().contains(" ") && term2.getOriginalTerm().contains(" ")){
				    		candidateLemmas = lookForATermWordMatch(sentenceString, term1.getLemma(), term2.getOriginalTerm(),
									candidateLemmas);
				    	} else if(term1.getOriginalTerm().contains(" ") && term2.getOriginalTerm().contains(" ")){
				    		candidate = lookForATermWordMatch(sentenceString, term1.getOriginalTerm(), term2.getOriginalTerm(),
				    				candidate);
				    	} 
				    	
					}

					if(!candidateLemmas.isEmpty()){
						Relation relation = new Relation();
						relation.setArg1(term1.getOriginalTerm());
						relation.setArg2(term2.getOriginalTerm());
						relation.setRel(candidateLemmas.replaceAll("(\\p{Punct}+)",""));
						this.getRelationsForOneText().add(relation);
						
						InitialRelationsManager.getUsedTerms().put(term1.getOriginalTerm(), term1.getLemma());
						InitialRelationsManager.getUsedTerms().put(term1.getOriginalTerm(), term2.getLemma());
						// The actual relation is not yet extracted
					}
					
					//Only if it is a multiword term, we need another approach. The one above has already covered lemmas.
					//Only something like veggies wouldn't be recognized because it is lemmatized to "veggy" (veggies in text).
					if(!candidate.isEmpty()){
						Relation relation = new Relation();
						relation.setArg1(term1.getOriginalTerm());
						relation.setArg2(term2.getOriginalTerm());
						relation.setRel(candidate.replaceAll("(\\p{Punct}+)",""));
						this.getRelationsForOneText().add(relation);
						
						InitialRelationsManager.getUsedTerms().put(term1.getOriginalTerm(), term1.getLemma());
						InitialRelationsManager.getUsedTerms().put(term2.getOriginalTerm(), term2.getLemma());
						// The actual relation is not yet extracted
					}
					
					
				}
			}
		}
		
	}*/





}
