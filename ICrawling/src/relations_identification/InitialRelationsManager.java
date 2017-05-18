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
import io.RunnableThread;
import linguistic_processing.CatVariator;
import linguistic_processing.MeshVariator;
import linguistic_processing.StanfordLemmatizer;

/**
 * @author Vera
 * This class manages all relations and terms from all texts.
 */
public class InitialRelationsManager {

	private static Map<Relation, Integer> overallRelations = new HashMap<Relation, Integer>();
	private String pathToNFDump;
	private static final String PATH_CAT_VAR = "terminology_variations_catvar.txt";
	private static final String PATH_MESH = "mesh_variations.txt";
	/**
	 * Used and unused terms refer to the words from each text that are qualified as terms, e.g through lemmatization.
	 * But at the end, the original terms are written in the file
	 *
	 */
	private static Map<String,String> unusedTerms = new HashMap<String, String>();
	private static Map<String,String> usedTerms = new HashMap<String, String>();
	// variable only for writing all terms with lemmas into a file
	private static Map<String,String> termsOverall = new HashMap<String,String>();
	
	private static Set<Term> terms = new HashSet<Term>();
	// example dogl_o_v_ecat => (Term(dog), Term(cat))
	public static Map<String, Pair<Term>> tuplesOfTerms = null;
	
	// for checking if a string contains any term
	public static Set<String> allTermsAndVariations = new HashSet<String>();
	
	private static Map<String,Set<String>> catVar = new HashMap<String,Set<String>>();
	private static Map<String,Set<String>> meshTerms = new HashMap<String,Set<String>>();
	private static RelationsFilter filter = new RelationsFilter();
	
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
	
	public void extractTerms(){
		//this file still contains duplicates
		Writer.overwriteFile("", "termsOverall.txt");
		//this one doesn't contain duplicates
		Writer.overwriteFile("", "all_terms.txt");
		List<String> linesOfDump = Reader.readLinesList(pathToNFDump);
		for (String line: linesOfDump) {
			if(!line.isEmpty()){
				QueryRelationsExplorer initialExplorer = new InitialQueryRelationsExplorer(line);
				//InitialRelationsManager.getExplorer().add(initialExplorer);
				Writer.overwriteFile("", "relations_backup/initial_relations" +"_" + initialExplorer.getQueryID() + ".txt" );
			}
		}
		
		for(String term_a : InitialRelationsManager.getTermsOverall().keySet()){
			Writer.appendLineToFile(term_a + "\t" + InitialRelationsManager.getTermsOverall().get(term_a), "all_terms.txt");
		}
	
	}
		
	
	public void doInitialExtraction(){
		
		Writer.overwriteFile("", "initial_relations.txt");
		Writer.overwriteFile("", "map_tuples.txt");
		Writer.overwriteFile("", "used_terms.txt");
		Writer.overwriteFile("", "unused_terms.txt");
		Writer.overwriteFile("", "all_relations.txt");
		Writer.overwriteFile("", "relations_backup/trash_relations.txt");
		Writer.overwriteFile("", "relations_backup/unknown_trash_relations.txt");
		
		List<String> linesOfDump = Reader.readLinesList(pathToNFDump);
		for (int i=0;i< linesOfDump.size();i++) {
			String line = linesOfDump.get(i);
			if(!line.isEmpty()){
				QueryRelationsExplorer initialExplorer = new InitialQueryRelationsExplorer(line);
				//InitialRelationsManager.getExplorer().add(initialExplorer);
				System.out.println("Query " + initialExplorer.getQueryID());
				
				initialExplorer.extractRelations();
				
				for(Relation relation: initialExplorer.getRelationsForOneText()){
					writeRelation(initialExplorer, relation);
					
				}
			}
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
		
		for(Relation rel: InitialRelationsManager.getOverallRelations().keySet()){
			Writer.appendLineToFile(rel.toString() + "\t" + InitialRelationsManager.getOverallRelations().get(rel), "all_relations.txt");
		}
	}


	private void writeRelation(QueryRelationsExplorer initialExplorer, Relation relation) {
		String relations = initialExplorer.getQueryID() + "\t";
		relations = relations + relation.toString();
		Writer.appendLineToFile(relations, "relations_backup/initial_relations" + "_" + initialExplorer.getQueryID() + ".txt");
	}
	
	/**
	 * Creates a map of tuples from all of the terms. 
	 * Example [cat, dog, fox] => cat.dog: (cat, dog), cat.fox: (cat, fox), dog.fox: (dog, fox), 
	 * @param terms
	 * @return
	 */
	public static Map<String, Pair<Term>> buildaTupleHashmapOfTerms(Set<Term> terms)
	{
		Map<String, Pair<Term>> tuples = new HashMap<String, Pair<Term>>();
		
		// Loop through all of the terms to create tuples from them all
		for(Term term1: terms)
		{
			
			Set<String> term1Variations = new HashSet<String>(term1.getCatvariations());
			// We don't know if the original term is a variation
			term1Variations.add(term1.getOriginalTerm());
			term1Variations.addAll(term1.getMesh());
			
			for(Term term2: terms)
			{
				if(!term1.equals(term2)){
					Set<String> term2Variations = new HashSet<String>(term2.getCatvariations());
					// We don't know if the original term is a variation
					term2Variations.add(term2.getOriginalTerm());
					term2Variations.addAll(term2.getMesh());
					for (String term1Variation: term1Variations)
						for (String term2Variation: term2Variations)
						{
							
							tuples.put(term1Variation + "l_o_v_e" + term2Variation, new Pair<Term>(term1, term2));
						}
				}
				
			}
		}
		
		
		/*for(String pair: tuples.keySet()){
			Writer.appendLineToFile(pair + "\t" + tuples.get(pair).first.toString() +  "\t" + tuples.get(pair).second.toString(), "map_tuples.txt");
		}*/
		
		return tuples;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InitialRelationsManager manager = new InitialRelationsManager(crawling_queries.Properties.NFDUMP_PATH);
		// needs to be modified in order to include the new terms
		manager.extractTerms();
		
		CatVariator.readCatVar();
		CatVariator.writeTerminologyVariations();
		InitialRelationsManager.setCatVar(CatVariator.readCatVariations(PATH_CAT_VAR));
		InitialRelationsManager.setMeshTerms(MeshVariator.readMeshVariations(PATH_MESH));
		
		
		
		// Builds tuples from all of the terms 
		InitialRelationsManager.tuplesOfTerms = InitialRelationsManager.buildaTupleHashmapOfTerms(InitialRelationsManager.getTerms());
		
		manager.doInitialExtraction();
		
		
	}

	public String getPathToNFDump() {
		return pathToNFDump;
	}

	public void setPathToNFDump(String pathToNFDump) {
		this.pathToNFDump = pathToNFDump;
	}

	public static Map<Relation, Integer> getOverallRelations() {
		return overallRelations;
	}

	public static void setOverallRelations(Map<Relation, Integer> overallRelations) {
		InitialRelationsManager.overallRelations = overallRelations;
	}


	public static Map<String, String> getTermsOverall() {
		return termsOverall;
	}


	public static void setTermsOverall(Map<String, String> termsOverall) {
		InitialRelationsManager.termsOverall = termsOverall;
	}


	public static Map<String,Set<String>> getCatVar() {
		return catVar;
	}


	public static void setCatVar(Map<String,Set<String>> catVar) {
		InitialRelationsManager.catVar = catVar;
	}


	public static Map<String,Set<String>> getMeshTerms() {
		return meshTerms;
	}


	public static void setMeshTerms(Map<String,Set<String>> meshTerms) {
		InitialRelationsManager.meshTerms = meshTerms;
	}


	public static Set<Term> getTerms() {
		return terms;
	}


	public static void setTerms(Set<Term> terms) {
		InitialRelationsManager.terms = terms;
	}

	public static RelationsFilter getFilter() {
		return filter;
	}


	public static void setFilter(RelationsFilter filter) {
		InitialRelationsManager.filter = filter;
	}
	

}
