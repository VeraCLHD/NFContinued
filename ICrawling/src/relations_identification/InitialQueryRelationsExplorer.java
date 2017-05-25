package relations_identification;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.classic.ParseException;
import org.codehaus.plexus.util.StringUtils;
import org.unix4j.Unix4j;
import org.unix4j.unix.Grep;

import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import io.Writer;
import linguistic_processing.LuceneSearcher;
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
		List<String> candidates = new ArrayList<String>();
		Matcher matcher = Pattern.compile(
				"\\b" +
		        Pattern.quote("pattern1") + "\\b"
		        + "(.*?)" +
		        "\\b"
		        + Pattern.quote("pattern2") + "\\b").matcher("its a string with pattern1 aleatory pattern2 things between pattern1 and pattern2 and sometimes pattern1 pattern2 nothing");
		
		while(matcher.find()){
			String match = matcher.group(1);
			candidates.add(match);
			
		}

		
	}

	@Override
	/*
	 * A function that extracts the initial connections here. If a word is connected to a term in 1 sentence (!),
	 * it is extracted as a connection. Problem: lots of the terms doesn't appear in the text. This problem is solved with a morphological analyzer (CatVar)
	 * @see relations_identification.QueryRelationsExplorer#extractRelations()
	 */
	public void extractRelations() throws IOException, ParseException {
		// The Stanford Parser should be used for the sentence splitting. It has a more elaborate method to identify a sentence.
		SentenceSplitter splitter = new SentenceSplitter(this.getTextLower());
		  File f = new File("denied/tuples_of_terms.txt");
			
		for(Sentence sentence: splitter.getSentences()){
			String sentenceString = sentence.toString();
			
			List<String> termCandidates1 = extractCombinationsOfWords(sentence);
			List<String> termCandidates2 = extractCombinationsOfWords(sentence);
			
			for(String candidate1: termCandidates1){
				for(String candidate2: termCandidates2){

					
					if(!candidate1.equals(candidate2)){
						String candidate = candidate1 + "l_o_v_e" + candidate2;
						boolean isTermCombi = LuceneSearcher.doSearch(candidate);
						
						
						if (isTermCombi){
							List<Pair<String>> stdCase = lookForATermWordMatch(sentenceString, candidate1, candidate2);
							for(Pair<String> match: stdCase){
								extractRelation(candidate1, candidate2, match);
							}
						}
					}

						
				}
			}
			
			}

	}


	private List<String> extractCombinationsOfWords(Sentence sentence) {
		List<String> tokens = sentence.words();
		List<String> tokenCombinations = new ArrayList<String>();
		
		// termSize =1 is the size of terms for multiwords/single if = 1
		//i = number of terms of certain size
		//// termSize -> max. size of a possible term is 8. This is currently the longest term
		for(int termSize=1;termSize <= Math.min(tokens.size(),8); termSize++){
			for(int i=1; i<=tokens.size()-termSize+1;i++)
			{
				List<String> initialtokenCombination = tokens.subList(i - 1, i - 1 + termSize);
				List<String> tokenCombination = new ArrayList<String>();
				for (String token: initialtokenCombination){
					if(!token.matches("(\\p{Punct}+)") && !token.isEmpty() && !token.matches("") && !token.matches("\\s+")){
						tokenCombination.add(token.toLowerCase().trim());
					
					}
				}
				
				
				// punctuation is included in the candidates, shouldn't be -> pattern
				String tokenString = String.join(" ", tokenCombination);
				if(!tokenString.isEmpty() && !tokenString.matches("") && !tokenString.matches(" ") && !tokenString.matches("\\s+")){
					
					tokenCombinations.add(tokenString);
				}
				
				
				
			}
		
		
		}
		
		return tokenCombinations;
	}
	
	/*private void handleLemmas(Term term1, Term term2, Sentence sentence){
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
		
		
	}*/


	/*private void handleMorphoVariations(String sentenceString, Term term1, Term term2) {
		//morph. variations of both terms
		Set<String> vars1 = term1.getCatvariations();
		vars1.addAll(term1.getMesh());
		Set<String> vars2 = term2.getCatvariations();
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
	}*/
	
	// term1: der Term selbst
	private void extractRelation(String var1, String var2, Pair<String> pair) {
		//without terms
		String candidate = pair.first;
		candidate = processCandidate(candidate);
		
		if(!candidate.isEmpty() ){
			int len = candidate.split(" ").length;
			Relation relation = new Relation();
			//term 1 found like this
			relation.setArg1(var1);
			//original term
			
			relation.setArg2(var2);

			relation.setRel(candidate);
			
			List<String> posTags = annotatePOS(pair, var1, var2);
			relation.setPosTags(posTags);
			// Filters begin here
			boolean fixed_result = RelationsFilter.matchesFixedConnections(candidate, relation);
			boolean vb_result = RelationsFilter.startsWithVPAndNotOtherSentence(posTags, candidate, relation);
			// do not extract incomplete NPs, do not extract candidates that contain other terms
			if(RelationsFilter.isIncompleteNP(posTags, candidate) || RelationsFilter.candidateContainsOtherTerms(candidate)){
				String relations = relation.toString();
				
				Writer.appendLineToFile(relations, "relations_backup/trash_relations.txt");
			}
			// if candidate matches fixed patterns; || len<=8
			else if((
					RelationsFilter.isOrStartsWithRelevantPunct(candidate, relation)
					|| RelationsFilter.isARelation(candidate, relation)
					|| RelationsFilter.isEmpty(candidate, relation)
					|| RelationsFilter.isInfluence(candidate, relation)
					|| fixed_result == true 
					|| vb_result == true 
					|| RelationsFilter.startsWithPrepAndNotOtherSentence(posTags, candidate, relation)
					|| RelationsFilter.startsWithAdjAndNotOtherSentence(posTags, candidate, relation)
					|| RelationsFilter.isPreposition(candidate, relation, posTags)
					|| RelationsFilter.isCoordinatingConjunction(candidate, relation, posTags)) && len<=10){
					
					// at the level of 1 text - no duplicates, at the level of all texts - duplicates
					this.getRelationsForOneText().add(relation);
					
					// put relation in maps
					InitialRelationsManager.getUsedTerms().add(var1);
					InitialRelationsManager.getUsedTerms().add(var2);
					//add relation to overall relations and count frequency
					Integer relationFrequency = InitialRelationsManager.getOverallRelations().get(relation);
					if( relationFrequency != null){
						InitialRelationsManager.getOverallRelations().put(relation, relationFrequency+1);
					} else{
						InitialRelationsManager.getOverallRelations().put(relation, 1);
					}
				
			} else {
				String relations = relation.toString();
				Writer.appendLineToFile(relations, "relations_backup/unknown_trash_relations.txt");
			}
			
		}
		
	}
	
	public List<String> annotatePOS(Pair<String> pair, String var1, String var2){
		int len1 = var1.split(" ").length;
		int len2 = var2.split(" ").length;
		String candidate = pair.first;
		String candidateWithTerms = pair.second;
		List<String> pos =  new ArrayList<String>();
		// annotate POS
		if(!candidate.isEmpty() && !candidate.matches("\\s+") && !candidate.equals(" ") && candidate !=null){
			Sentence sent = new Sentence(candidateWithTerms);
				// pos tags penn tree bank
				//https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
				// for checking of the POS tags -> candidate starts with NNS for example, we don't need the terms themselves.
			// This is done only to make sure we have the correct tags.
			List<String> posTest =  sent.posTags();
			if(!candidate.matches("(\\p{Punct}+)") && posTest.size() != 0){
				pos =  sent.posTags().subList(0+len1, posTest.size()-len2);
			}
			
				
			}
		return pos;
	}

	public String processCandidate(String candidate) {
		candidate = candidate.trim();
		candidate = candidate.replaceAll("-LRB-","(");
		candidate = candidate.replaceAll("-RRB-",")");
		candidate = candidate.replaceAll("\"","");
		return candidate;
	}
	
	//http://stackoverflow.com/questions/11255353/java-best-way-to-grab-all-strings-between-two-strings-regex
	//http://stackoverflow.com/questions/4769652/how-do-you-use-the-java-word-boundary-with-apostrophes
	private List<Pair<String>> lookForATermWordMatch(String sentenceString, String term1, String term2) {
		List<Pair<String>> candidates = new ArrayList<Pair<String>>();
		Matcher matcher = Pattern.compile(
				"\\b" +
				 Pattern.quote(term1) + "\\b"
				 + "(.*?)" +
				 "\\b"
				 + Pattern.quote(term2) + "\\b").matcher(sentenceString);
		
		while(matcher.find()){
			// the old candidate without the terms themselves
			String matchWithoutTerms = matcher.group(1);
			// match contains the strings of the terms themselves now
			String match = term1 + matchWithoutTerms + term2;
			Pair<String> pair = new Pair<String>(matchWithoutTerms, match);
			candidates.add(pair);
			
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
