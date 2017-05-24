/**
 * 
 */
package relations_identification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
	private static Set<String> unusedTerms = new HashSet< String>();
	private static Set<String> usedTerms = new HashSet<String>();
	// variable only for writing all terms with lemmas into a file
	private static Map<String,String> termsOverall = new HashMap<String,String>();
	
	private static Set<Term> terms = new HashSet<Term>();
	// example dogl_o_v_ecat => (Term(dog), Term(cat))
	public static Set<String> tuplesOfTerms = null;
	
	// for checking if a string contains any term
	public static Set<String> allTermsAndVariations = new HashSet<String>();

	private static RelationsFilter filter = new RelationsFilter();
	
	
	
	public static Set<String> getUnusedTerms() {
		return unusedTerms;
	}


	public static void setUnusedTerms(Set<String> unusedTerms) {
		InitialRelationsManager.unusedTerms = unusedTerms;
	}


	public static Set<String> getUsedTerms() {
		return usedTerms;
	}


	public static void setUsedTerms(Set<String> usedTerms) {
		InitialRelationsManager.usedTerms = usedTerms;
	}
	
	public void determindeUnusedTerms(){
		StanfordLemmatizer lemm = new StanfordLemmatizer();
		for(String term: getTermsOverall().keySet()){
			if(!getUsedTerms().contains(term)){
				
				String termLemma = lemm.lemmatize(term);
				getUnusedTerms().add(term);

			}
		}
		
		
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
	
	//the main structure allTermsAndVariations gets mesh and cat here; each term gehts mesh
	public void addMeshVariationsToTerms(Map<String, Set<String>> contentOfMeshFile){
		// mesh variations
		for(Term term : InitialRelationsManager.getTerms()){
			// we use the lemma to check in mesh
			String lemma = term.getLemma();
			
			if(contentOfMeshFile.containsKey(term.getOriginalTerm())){
				Set<String> list = contentOfMeshFile.get(term.getOriginalTerm());
				if(list !=null && !list.isEmpty()){
					term.getCatAndMesh().addAll(contentOfMeshFile.get(term.getOriginalTerm()));
					
				}
				
			}
			else if(!term.getOriginalTerm().contains(" ") && contentOfMeshFile.containsKey(lemma)){
				Set<String> list = contentOfMeshFile.get(lemma);
				if(list !=null && !list.isEmpty()){
					term.getCatAndMesh().addAll(contentOfMeshFile.get(term.getLemma()));
					InitialRelationsManager.allTermsAndVariations.add(term.getLemma());
				}
				
			} 
			
			else{
				for(Entry<String,Set<String>> variation: contentOfMeshFile.entrySet()){
					if(variation.getValue().contains(term.getOriginalTerm()) || (!term.getOriginalTerm().contains(" ") && variation.getValue().contains(lemma))){
						Set<String> vars = new HashSet<String>();
						vars.add(variation.getKey());
						vars.addAll(variation.getValue());
						term.getCatAndMesh().addAll(vars);
						break;
					}
				}
			}
			
			
			// this structure only for checking if a string contains them later
			InitialRelationsManager.allTermsAndVariations.addAll(term.getCatAndMesh());
			InitialRelationsManager.allTermsAndVariations.add(term.getOriginalTerm());
			
	
	}
	
	}
	/**
	 * prerequisite for this method is: to run the addCatVariations first, then mesh!
	 * Here, each term gehts cat. allTermsAndVariations gehts lemmas and term itself
	 * @param contentOfCatVarFile
	 */
	public void addCatVariationsToTerms(Map<String, Set<String>> contentOfCatVarFile){
		
		
		for(Term term : InitialRelationsManager.getTerms()){
				// we use the lemma to check in CATVAR; otherwise very often nothing is found.
				String lemma = term.getLemma();
				if(term.getOriginalTerm().matches(".*\\p{Punct}") || term.getOriginalTerm().contains(" ")){
					continue;
				}
				

				if(contentOfCatVarFile.containsKey(lemma)){
					Set<String> list = contentOfCatVarFile.get(lemma);
					if(list == null || list.isEmpty()){
						term.setCatAndMesh(new HashSet<String>());
					} else{
						term.setCatAndMesh(new HashSet<String>(contentOfCatVarFile.get(lemma)));
					}
					
					InitialRelationsManager.allTermsAndVariations.add(term.getLemma());
					
				} else{
					for(Entry<String,Set<String>> variation: contentOfCatVarFile.entrySet()){
						if(variation.getValue().contains(term.getOriginalTerm()) || variation.getValue().contains(lemma)){
							Set<String> vars = new HashSet<String>();
							vars.add(variation.getKey());
							vars.addAll(variation.getValue());
							term.setCatAndMesh(vars);
							break;
						}
					}
					
					InitialRelationsManager.allTermsAndVariations.add(term.getLemma());
				}
				
				
				
				// this structure only for checking if a string contains them later
				InitialRelationsManager.allTermsAndVariations.add(term.getOriginalTerm());
				
		
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
		Set<String> used = getUsedTerms();
		determindeUnusedTerms();
		Set<String> unused = getUnusedTerms();
		
		
		for (String term: unused){
			Writer.appendLineToFile(term , "unused_terms.txt");
		}
		for (String termUsed: used){
			Writer.appendLineToFile(termUsed, "used_terms.txt");
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
	public static void buildaTupleHashmapOfTerms(Set<Term> terms)
	{
		String filepath = "term_outputs/tuples_of_terms";	
		System.out.println("building tuples of terms");
		//int[] file = {2,3,5,7};
		/*for(int f: file){
			Writer.overwriteFile("", filepath + "_" + String.valueOf(f) + ".txt");
		}*/
		Writer.overwriteFile("", filepath + "_" + 2 + ".txt");
		Writer.overwriteFile("", "which_term.txt");
		
		
		
		List<String> allTermsAndVariations = new ArrayList<String>(InitialRelationsManager.allTermsAndVariations);
		for(int i = 0; i<		allTermsAndVariations.size(); i++){
			for(int j = i; j<		allTermsAndVariations.size(); j++){
				String term1 = allTermsAndVariations.get(i);
				String term2 = allTermsAndVariations.get(j);
				if(!term1.equals(term2)){

					if(i%2 == 0){
						Writer.appendLineToFile(term1 + "l_o_v_e" + term2, filepath + "_" + 2 + ".txt");
						Writer.appendLineToFile(term2 + "l_o_v_e" + term1, filepath + "_" + 2 + ".txt");
					} else{
						Writer.appendLineToFile(term1 + "l_o_v_e" + term2, filepath + "_" + 1 + ".txt");
						Writer.appendLineToFile(term2 + "l_o_v_e" + term1, filepath + "_" + 1 + ".txt");
					}
					

				}
				
			}
			
			Writer.appendLineToFile(String.valueOf(i), "term_outputs/which_term.txt");
		}
		
		
		/*for(String pair: tuples.keySet()){
			Writer.appendLineToFile(pair + "\t" + tuples.get(pair).first.toString() +  "\t" + tuples.get(pair).second.toString(), "map_tuples.txt");
		}*/
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
		
		
		manager.addCatVariationsToTerms(CatVariator.getContentOfCatVarFile());
		// very important: addMesh should be runned after addCat!!!
		manager.addMeshVariationsToTerms(MeshVariator.getContentOfMeshFile());
		
		System.out.print(InitialRelationsManager.allTermsAndVariations.size());
		

		// Builds tuples from all of the terms 
		InitialRelationsManager.buildaTupleHashmapOfTerms(InitialRelationsManager.getTerms());
		/*InitialRelationsManager.tuplesOfTerms = InitialRelationsManager.buildaTupleHashmapOfTerms(InitialRelationsManager.getTerms());
		
		manager.doInitialExtraction();
		
		Map<Relation, Integer> filtered = InitialRelationsManager.filterOverallRelations();
		for(Relation rel: filtered.keySet()){
			Writer.appendLineToFile(rel.toString() + "\t" + InitialRelationsManager.getOverallRelations().get(rel), "all_relations.txt");
		}*/
		
		
	}
	
	public static Map<Relation, Integer> filterOverallRelations(){
		Writer.overwriteFile("","all_relations_duplicates.txt");
		Map<Relation, Integer> duplicatedMap = InitialRelationsManager.getOverallRelations();
		Map<Relation, Integer> finalMap = InitialRelationsManager.getOverallRelations();
		Set<Relation> duplicateCandidates = new HashSet<Relation>();
		
		for(Relation relation1: duplicatedMap.keySet()){
			Integer frequency = duplicatedMap.get(relation1);
			
			
			Set<Relation> set = duplicatedMap.keySet();
			
			// second loop only for comparison
			for(Relation relation2: set){
				if(!relation1.equals(relation2)){
					// if they have the same between them
					if(relation1.getRel().equals(relation2.getRel())){
						if(relation1.getArg1().contains(relation2.getArg1()) && relation1.getArg2().equals(relation2.getArg2())){
							frequency +=duplicatedMap.get(relation2);
							duplicateCandidates.add(relation2);

						} else if(relation1.getArg2().contains(relation2.getArg2()) && relation1.getArg1().equals(relation2.getArg1())){
							frequency +=duplicatedMap.get(relation2);
							duplicateCandidates.add(relation2);
							
						} else if(relation1.getArg1().contains(relation2.getArg1()) && relation1.getArg2().contains(relation2.getArg2())){
							frequency +=duplicatedMap.get(relation2);
							duplicateCandidates.add(relation2);
							
						}
					}
				}
			}

			finalMap.put(relation1, frequency);
			
		}
		
		for(Relation duplicate: duplicateCandidates){
			finalMap.remove(duplicate);
			Writer.appendLineToFile(duplicate.toString(), "all_relations_duplicates.txt");
		}
		return finalMap;
		
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
