/**
 * 
 */
package relations_identification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.Reader;
import io.Writer;
import linguistic_processing.StanfordLemmatizer;

/**
 * @author Vera
 * This class manages all relations and terms from all texts.
 */
public class InitialRelationsManager {

	private List<Relation> overallRelations = new ArrayList<Relation>();
	private String pathToNFDump;
	/**
	 * Used and unused terms refer to the words from each text that are qualified as terms, e.g through lemmatization.
	 * But at the end, the original terms are written in the file
	 *
	 */
	private static Map<String,String> unusedTerms = new HashMap<String, String>();
	private static Map<String,String> usedTerms = new HashMap<String, String>();
	private static List<Term> termsOverall = new ArrayList<Term>();
	
	public static Map<String, String> getUnusedTerms() {
		return unusedTerms;
	}


	public static void setUnusedTerms(Map<String, String> unusedTerms) {
		InitialRelationsManager.unusedTerms = unusedTerms;
	}


	public static Map<String, String> getUsedTerms() {
		return usedTerms;
	}


	public static void setUsedTerms(Map<String, String> usedTerms) {
		InitialRelationsManager.usedTerms = usedTerms;
	}
	
	public Map<String, String> determindeUnusedTerms(){
		StanfordLemmatizer lemm = new StanfordLemmatizer();
		for(String term: getTermsOverall().keySet()){
			if(!getUsedTerms().containsKey(term)){
				
				String termLemma = lemm.lemmatize(term);
				getUnusedTerms().put(term, termLemma);

			}
		}
		
		return unusedTerms;
		
	}
	
	public InitialRelationsManager(String pathToNFDump){
		this.setPathToNFDump(pathToNFDump);
		
	}
	// Die Klasse, die alle anderen aufruft und schreibt.
	public void doInitialExtraction(){
		List<String> linesOfDump = Reader.readLinesList(pathToNFDump);
		Writer.overwriteFile("", "initial_relations.txt");
		Writer.overwriteFile("", "used_terms.txt");
		Writer.overwriteFile("", "unused_terms.txt");
		//this file still contains duplicates
		Writer.overwriteFile("", "termsOverall.txt");
		//this one doesn't contain duplicates
		Writer.overwriteFile("", "all_terms.txt");
		
		
		for (String line: linesOfDump) {
			if(!line.isEmpty()){
				QueryRelationsExplorer initialExplorer = new InitialQueryRelationsExplorer(line);
				//initialExplorer.identifyContainsMultiWordTerms();
				initialExplorer.extractRelations();
				this.getOverallRelations().addAll(initialExplorer.getRelationsForOneText());
				
				for(Relation relation: initialExplorer.getRelationsForOneText()){
					String relations = initialExplorer.getQueryID() + "\t";
					relations = relations + relation.getArg1() + "\t";
					relations = relations + relation.getArg2() + "\t";
					relations = relations + relation.getRel() + "\t";
					Writer.appendLineToFile(relations, "initial_relations.txt");
				}
				
			}
		}
		
		for(String term_a : InitialRelationsManager.getTermsOverall().keySet()){
			Writer.appendLineToFile(term_a + "\t" + InitialRelationsManager.getTermsOverall().get(term_a), "all_terms.txt");
		}
		
		// Here, we don't have a mapping between a query and the terms. Mapping provided in initial relations for used terms.
		Map<String, String> used = getUsedTerms();
		determindeUnusedTerms();
		Map<String, String> unused = getUnusedTerms();
		
		
		for (String term: unused.keySet()){
			Writer.appendLineToFile(term + "\t" + unused.get(term), "unused_terms.txt");
		}
		for (String termUsed: used.keySet()){
			Writer.appendLineToFile(termUsed + "\t" + used.get(termUsed), "used_terms.txt");
		}
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InitialRelationsManager manager = new InitialRelationsManager(crawling_queries.Properties.NFDUMP_PATH);
		manager.doInitialExtraction();
		
	}

	public String getPathToNFDump() {
		return pathToNFDump;
	}

	public void setPathToNFDump(String pathToNFDump) {
		this.pathToNFDump = pathToNFDump;
	}

	public List<Relation> getOverallRelations() {
		return overallRelations;
	}

	public void setOverallRelations(List<Relation> overallRelations) {
		this.overallRelations = overallRelations;
	}


	public static Map<String, String> getTermsOverall() {
		return termsOverall;
	}


	public static void setTermsOverall(Map<String, String> termsOverall) {
		InitialRelationsManager.termsOverall = termsOverall;
	}
	

}
