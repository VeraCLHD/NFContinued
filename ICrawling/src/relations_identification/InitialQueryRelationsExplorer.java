package relations_identification;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import io.Reader;
import io.Writer;
import linguistic_processing.LuceneSearcher;
import linguistic_processing.SentenceSplitter;
import linguistic_processing.StanfordLemmatizer;
import processing.TextPreprocessor;

public class InitialQueryRelationsExplorer {
	
	private static Set<Relation> relations = new HashSet<Relation>();
	
	
	
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


	/*
	 * A function that extracts the initial connections here. If a word is connected to a term in 1 sentence (!),
	 * it is extracted as a connection. Problem: lots of the terms doesn't appear in the text. This problem is solved with a morphological analyzer (CatVar)
	 * @see relations_identification.QueryRelationsExplorer#extractRelations()
	 */
	public static void extractRelationsForPair(String term1, String term2, LuceneSearcher ls) throws IOException, ParseException {
		// The Stanford Parser should be used for the sentence splitting. It has a more elaborate method to identify a sentence.
		if(!term1.equals(term2)){
			Set<String> set = ls.doSearch("\"" + term1 +"\"" + "AND" + "\"" + term2 +"\"");
			if(!set.isEmpty()){
				for(String path: set){
					  String sentenceString = Reader.readContentOfFile(path);
					 
					  Set<Pair<String>> stdCase = lookForATermWordMatch(sentenceString, term1, term2);
					  Set<Pair<String>> stdCase2 = lookForATermWordMatch(sentenceString, term2, term1);
					 
					 if(!stdCase.isEmpty()){
						 for(Pair<String> match: stdCase){
								extractRelation(term1, term2, match, path);
							} 
					 } if(!stdCase2.isEmpty()){
						 for(Pair<String> match: stdCase2){
								extractRelation(term2, term1, match, path);
							} 
					 }
						
				}
			}
						

		}
		
						
				}
			
			



	
	// term1: der Term selbst
	public static void extractRelation(String var1, String var2, Pair<String> pair, String id) {
		//without terms
		String candidate = pair.first;
		candidate = processCandidate(candidate);
		
		if(!candidate.isEmpty() ){
			int len = candidate.split(" ").length;
			Relation relation = new Relation();
			relation.setQueryId(id);
			//term 1 found like this
			relation.setArg1(var1);
			
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
					InitialRelationsManager.writeRelation(relation);
					
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
	
	public static List<String> annotatePOS(Pair<String> pair, String var1, String var2){
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
			int left = posTest.size() - len1 - len2;
			if(!candidate.matches("(\\p{Punct}+)") && posTest.size() != 0 && left > 0){
				
				//int max = Math.max(len1, );
				//int min = Math.min(len1,posTest.size()-len2);
				pos =  sent.posTags().subList(len1,posTest.size()-len2);
			}
			
				
			}
		return pos;
	}

	public static String processCandidate(String candidate) {
		candidate = candidate.trim();
		candidate = candidate.replaceAll("-LRB-","(");
		candidate = candidate.replaceAll("-RRB-",")");
		candidate = candidate.replaceAll("\"","");
		return candidate;
	}
	
	//http://stackoverflow.com/questions/11255353/java-best-way-to-grab-all-strings-between-two-strings-regex
	//http://stackoverflow.com/questions/4769652/how-do-you-use-the-java-word-boundary-with-apostrophes
	public static Set<Pair<String>> lookForATermWordMatch(String sentenceString, String term1, String term2) {
		Set<Pair<String>> candidates = new HashSet<Pair<String>>();
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
	
	public static Set<Relation> getRelations() {
		return relations;
	}
	public static void setRelations(Set<Relation> relationsForOneText) {
		InitialQueryRelationsExplorer.relations = relationsForOneText;
	}





}
