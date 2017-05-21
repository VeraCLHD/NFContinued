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
import java.util.Map.Entry;

import io.Reader;
import io.Writer;
import io.RunnableThread;
import linguistic_processing.CatVariator;
import linguistic_processing.MeshVariator;
import linguistic_processing.StanfordLemmatizer;
import linguistic_processing.XMLParserMesh;

/**
 * @author Vera
 * This class manages all relations and terms from all texts.
 */
public class InitialRelationsManager {

	private static Map<Relation, Integer> overallRelations = new HashMap<Relation, Integer>();
	private String pathToNFDump;
	private static final String PATH_CAT_VAR = "terminology_variations_catvar.txt";
	private static final String PATH_MESH = "mesh_variations.txt";
	private static final String KEA_PATH = "kea_terms.txt";
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
				Writer.overwriteFile("", "relations_backup/initial_relations" +"_" + initialExplorer.getQueryID() + ".txt" );
			}
		}
	
	}
		
	public void manageAdditionalTerms(){
		
		List<String> additionalKeaTerms = Reader.readLinesList(KEA_PATH);
		for(String kea: additionalKeaTerms){
			if(!kea.isEmpty() && kea !=null){
				Term term = new Term(kea.trim());
				InitialRelationsManager.getTerms().add(term);
			}
			
		}
		
	}
	
	public void addMeshVariationsToTerms(Map<String, Set<String>> contentOfMeshFile){
		// mesh variations
		for(Term term : InitialRelationsManager.getTerms()){
			// we use the lemma to check in mesh
			String lemma = term.getLemma();
			
			if(contentOfMeshFile.containsKey(term.getOriginalTerm())){
				Set<String> list = contentOfMeshFile.get(term.getOriginalTerm());
				if(list == null || list.isEmpty()){
					term.setMesh(new HashSet<String>());
				} else{
					term.setMesh(new HashSet<String>(contentOfMeshFile.get(term.getOriginalTerm())));
				}
				
			}
			else if(!term.getOriginalTerm().contains(" ") && contentOfMeshFile.containsKey(lemma)){
				Set<String> list = contentOfMeshFile.get(lemma);
				if(list == null || list.isEmpty()){
					term.setMesh(new HashSet<String>());
				} else{
					term.setMesh(new HashSet<String>(contentOfMeshFile.get(lemma)));
				}
				
			} 
			
			else{
				for(Entry<String,Set<String>> variation: contentOfMeshFile.entrySet()){
					if(variation.getValue().contains(term.getOriginalTerm()) || (!term.getOriginalTerm().contains(" ") && variation.getValue().contains(lemma))){
						Set<String> vars = new HashSet<String>();
						vars.add(variation.getKey());
						vars.addAll(variation.getValue());
						term.setMesh(vars);
						break;
					}
				}
			}
			
			
			// this structure only for checking if a string contains them later
			InitialRelationsManager.allTermsAndVariations.addAll(term.getMesh());
			InitialRelationsManager.allTermsAndVariations.add(term.getOriginalTerm());
			InitialRelationsManager.allTermsAndVariations.add(term.getLemma());
	
	}
	
	}
	
	public void addCatVariationsToTerms(Map<String, Set<String>> contentOfCatVarFile){
		
		
		for(Term term : InitialRelationsManager.getTerms()){
				// we use the lemma to check in CATVAR; otherwise very often nothing is found.
				String lemma = term.getLemma();
				if(term.getOriginalTerm().matches(".*\\p{Punct}") || term.getOriginalTerm().contains(" ") ){
					continue;
				}
				

				if(contentOfCatVarFile.containsKey(lemma)){
					Set<String> list = contentOfCatVarFile.get(lemma);
					if(list == null || list.isEmpty()){
						term.setCatvariations(new HashSet<String>());
					} else{
						term.setCatvariations(new HashSet<String>(contentOfCatVarFile.get(lemma)));
					}
					
				} else{
					for(Entry<String,Set<String>> variation: contentOfCatVarFile.entrySet()){
						if(variation.getValue().contains(term.getOriginalTerm()) || variation.getValue().contains(lemma)){
							Set<String> vars = new HashSet<String>();
							vars.add(variation.getKey());
							vars.addAll(variation.getValue());
							term.setCatvariations(vars);
							break;
						}
					}
				}
				
				
				
				// this structure only for checking if a string contains them later
				InitialRelationsManager.allTermsAndVariations.addAll(term.getCatvariations());
				InitialRelationsManager.allTermsAndVariations.add(term.getOriginalTerm());
				InitialRelationsManager.allTermsAndVariations.add(term.getLemma());
		
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
		// extract the terms: without variations
		InitialRelationsManager manager = new InitialRelationsManager(crawling_queries.Properties.NFDUMP_PATH);
		manager.extractTerms();
		/* cover mesh variations
		Writer.overwriteFile("", "meshVariants.txt");
		XMLParserMesh.extractEntryTermsMeshHeadings("mesh/desc2017.xml", "DescriptorRecord", "DescriptorUI", "DescriptorName");
	    //extractEntryTermsMeshHeadings("mesh/supp2017.xml", "SupplementalRecord", "SupplementalRecordUI", "SupplementalRecordName");
		XMLParserMesh.extractEntryTermsMeshHeadings("mesh/qual2017.xml", "QualifierRecord", "QualifierUI", "QualifierName");*/
	    
		// second we get the variations of all words in EN -> fills the map with variations
		CatVariator variator = new CatVariator();
		MeshVariator meshVariator = new MeshVariator();
		//manager.manageAdditionalTerms();
		
		// set the variations of the terms we need (only Dr. Gregers Terms)
		//CatVariator.writeTerminologyVariations("kea_terms.txt", "catvar_kea_terms.txt");
		for(Term term_a : InitialRelationsManager.getTerms()){
			Writer.appendLineToFile(term_a.getOriginalTerm() + "\t" + term_a.getLemma(), "all_terms.txt");
		}
		
		manager.addMeshVariationsToTerms(MeshVariator.getContentOfMeshFile());
		manager.addCatVariationsToTerms(CatVariator.getContentOfCatVarFile());
		
		
		

		// Builds tuples from all of the terms 
		InitialRelationsManager.tuplesOfTerms = InitialRelationsManager.buildaTupleHashmapOfTerms(InitialRelationsManager.getTerms());
		
		manager.doInitialExtraction();
		
		
	}
	
	public static Map<Relation, Integer> filterOverallRelations(){
		Map<Relation, Integer> duplicatedMap = InitialRelationsManager.getOverallRelations();
		Map<Relation, Integer> finalMap = InitialRelationsManager.getOverallRelations();
		for(Relation relation1: duplicatedMap.keySet()){
			// second loop only for comparison
			for(Relation relation2: duplicatedMap.keySet()){
				if(!relation1.equals(relation2)){
					// if they have the same between them
					if(relation1.getRel().equals(relation2)){
						if(relation1.getArg1Origin().contains(relation2.getArg1Origin())){
						
						}
					}
				}
			}
		}
		return overallRelations;
		
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
