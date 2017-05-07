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
		/*String str = "have videos on the effects on artery function of walnuts, dark chocolate, tea,";
		SentenceSplitter splitter = new SentenceSplitter(str);
		
		for(Sentence sentence: splitter.getSentences()){

			System.out.println(sentence.parse());
			for(Tree t: sentence.parse()){
				// get constituents of leaves funktioniert nicht
				//System.out.println("CONSTITUENTS" + t.constituents());
				//System.out.println("PARENT " + t.parent());
			String s = "VP";
			TregexPattern p = TregexPattern.compile(s);
			TregexMatcher m = p.matcher(sentence.parse());
			while (m.find()) {
				   m.getMatch().pennPrint();
				   //System.out.println(m.getMatch().getLeaves());
			}
		}
			
		}*/
		String candidate = "";
		Matcher matcher = Pattern.compile(
		        Pattern.quote("pattern1")
		        + "(.*?)"
		        + Pattern.quote("pattern2")).matcher("its a string with pattern1 aleatory pattern2 things between pattern1 and pattern2 and sometimes pattern1 pattern2 nothing");
		
		while(matcher.find()){
			String match = matcher.group(1);
			candidate += match;
			
		}
		
		System.out.println(candidate);
		
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
			
			
			for(Term term1: termSet){
				for(Term term2: termSet){
					
					if(!term1.equals(term2)){
						// first and second term are not multi-words
						
				    		// terms themselves
							List<String> stdCase = lookForATermWordMatch(sentenceString, term1.getOriginalTerm(), term2.getOriginalTerm());
							for(String match: stdCase){
								extractRelation(term1, term1.getOriginalTerm(), term2, term2.getOriginalTerm(), match);
							}
							
							
							// special case: m&m's -> is lemmatized to m which leads to many unwanted connections
							if(!term1.getOriginalTerm().contains("&") && !term2.getOriginalTerm().contains("&")){
								// case morphological variations
								handleMorphoVariations(sentenceString, term1, term2);
								
								/*if(!term1.getOriginalTerm().contains(" ") && !term2.getOriginalTerm().contains(" ")){
									// case lemmas: only for single terms
									handleLemmas(term1, term2, sentence);
								} 
								
								 These cases don't need to be handled - if a term is multiword, then only search for term itself.
								 * else if(term1.getOriginalTerm().contains(" ") && !term2.getOriginalTerm().contains(" ")){
									String caseLemma = lookForATermWordMatch(sentenceString, term1.getOriginalTerm(), term2.getLemma());
									extractRelation(term1, term1.getOriginalTerm(), term2, term2.getLemma(), caseLemma);
								} else if(!term1.getOriginalTerm().contains(" ") && term2.getOriginalTerm().contains(" ")){
									String caseLemma = lookForATermWordMatch(sentenceString, term1.getLemma(), term2.getOriginalTerm());
									extractRelation(term1, term1.getLemma(), term2, term2.getOriginalTerm(), caseLemma);
								} */
									
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
	
	private void handleLemmas(Term term1, Term term2, Sentence sentence){
		List<String> lemmas = sentence.lemmas();
		
		if(!term1.getOriginalTerm().contains(" ") && !term2.getOriginalTerm().contains(" ")){
			String lemma1 = term1.getLemma();
			String lemma2 = term2.getLemma();
			
			// only one connection is possible in a sentence - the first occurence of term 1 and the first of term 2
			if(lemmas.contains(term1.getLemma()) && lemmas.contains(term2.getLemma())){
				int index1 = lemmas.indexOf(lemma1);
				int index2 = lemmas.indexOf(lemma2);
				if(index1+1>index2){
					String connection = String.join(" ", sentence.words().subList(index2+1, index1));
					extractRelation(term1, term1.getLemma(), term2, term2.getLemma(), connection);
				} else{
					String connection = String.join(" ", sentence.words().subList(index1+1, index2));
					extractRelation(term1, term1.getLemma(), term2, term2.getLemma(), connection);
				}
				
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
				List<String> morphoCases = lookForATermWordMatch(sentenceString, term1.getOriginalTerm(), morpho2);
				for(String match1: morphoCases){
					if(!match1.isEmpty()){
						extractRelation(term1, term1.getOriginalTerm(), term2, morpho2, match1);
					}
				}
				
				
			}
		} else if(vars2.isEmpty() && !vars1.isEmpty()){
			for(String morpho1: vars1){
				List<String> morphoCases = lookForATermWordMatch(sentenceString, morpho1, term2.getOriginalTerm());
				for(String match2: morphoCases){
					if(!match2.isEmpty()){
						extractRelation(term1, morpho1, term2, term2.getOriginalTerm(), match2);
					}
				}
				
			}
		} else if(!vars2.isEmpty() && !vars1.isEmpty()){
			for(String morpho1: vars1){
				for(String morpho2: vars2){
					
					List<String> morphoCases = lookForATermWordMatch(sentenceString, morpho1, morpho2);
					for(String match3: morphoCases){
						if(!match3.isEmpty()){
							extractRelation(term1, morpho1, term2, morpho2, match3);
						}
					}
					
				}
				
			}
		}
	}
	

	private void extractRelation(Term term1, String var1, Term term2, String var2, String candidate) {
		if(!candidate.isEmpty()){
			candidate = processCandidate(candidate);
			int len = candidate.split(" ").length;
			Relation relation = new Relation();
			//found like this
			relation.setArg1(var1);
			//original term
			relation.setArg1Origin(term1.getOriginalTerm());
			relation.setArg2(var2);
			relation.setArg2Origin(term2.getOriginalTerm());
			// punctuation commented out .replaceAll("(\\p{Punct}+)","")
			relation.setRel(candidate);
			
			
			boolean fixed_result = RelationsFilter.matchesFixedConnections(candidate);
			boolean vb_result = RelationsFilter.startsWithVPAndNotOtherSentence(candidate);
			// if longer than 10 words -> automatically filtered
			if(len > 8){
				String relations = "";
				relations = relations + relation.getArg1() + "\t";
				relations = relations + relation.getArg1Origin() + "\t";
				relations = relations + relation.getArg2() + "\t";
				relations = relations + relation.getArg2Origin() + "\t";
				relations = relations + relation.getRel() + "\t";
				Writer.appendLineToFile(relations, "relations_backup/trash_relations.txt");
			}
			// if candidate matches fixed patterns
			else if((fixed_result == true || vb_result == true) || len<=8){
				if(!RelationsFilter.isOrStartsWithPunct(candidate) && !RelationsFilter.startsWithSingleChar(candidate)){
					// at the level of 1 text - no duplicates, at the level of all texts - duplicates
					this.getRelationsForOneText().add(relation);
					
					InitialRelationsManager.getUsedTerms().put(term1.getOriginalTerm(), term1.getLemma());
					InitialRelationsManager.getUsedTerms().put(term2.getOriginalTerm(), term2.getLemma());
				}
				
			} else {
				String relations = "";
				relations = relations + relation.getArg1() + "\t";
				relations = relations + relation.getArg1Origin() + "\t";
				relations = relations + relation.getArg2() + "\t";
				relations = relations + relation.getArg2Origin() + "\t";
				relations = relations + relation.getRel() + "\t";
				Writer.appendLineToFile(relations, "relations_backup/trash_relations.txt");
			}
			
		}
		
	}


	private String processCandidate(String candidate) {
		candidate = candidate.trim();
		candidate = candidate.replaceAll("-LRB-","(");
		candidate = candidate.replaceAll("-RRB-",")");
		return candidate;
	}
	
	//http://stackoverflow.com/questions/11255353/java-best-way-to-grab-all-strings-between-two-strings-regex
	private List<String> lookForATermWordMatch(String sentenceString, String term1, String term2) {
		List<String> candidates = new ArrayList<String>();
		String candidate = "";
		Matcher matcher = Pattern.compile(
		        Pattern.quote("\\b"+term1+"\\b")
		        + "(.*?)"
		        + Pattern.quote("\\b"+term2+"\\b")).matcher(sentenceString);
		
		while(matcher.find()){
			String match = matcher.group(1);
			candidates.add(match);
			/*if(!match.matches("(\\p{Punct}+)")){
				
			}*/
			
		}
		return candidates;
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
