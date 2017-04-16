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

/**
 * @author Vera
 * This class manages all relations and terms from all texts.
 */
public class InitialRelationsManager {
	// All terms from all texts that are there
	private Map<String,String> usedTerms = new HashMap<String, String>();
	private List<Relation> overallRelations = new ArrayList<Relation>();
	private String pathToNFDump;
	
	public InitialRelationsManager(String pathToNFDump){
		this.setPathToNFDump(pathToNFDump);
		
	}
	// Die Klasse, die alle anderen aufruft und schreibt.
	public void doInitialExtraction(){
		List<String> linesOfDump = Reader.readLinesList(pathToNFDump);
		Writer.overwriteFile("", "initial_relations.txt");
		Writer.overwriteFile("", "used_terms.txt");
		Writer.overwriteFile("", "unused_terms.txt");
		
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
				
				Map<String, String> used = initialExplorer.getUsedTerms();
				initialExplorer.determindeUnusedTerms();
				Map<String, String> unused = initialExplorer.getUnusedTerms();
				
				
				for (String term: unused.keySet()){
					Writer.appendLineToFile(initialExplorer.getQueryID() + "\t" + term + "\t" + unused.get(term), "unused_terms.txt");
				}
				for (String termUsed: used.keySet()){
					Writer.appendLineToFile(initialExplorer.getQueryID() + "\t" + termUsed + "\t" + used.get(termUsed), "used_terms.txt");
				}
			}
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

	public Map<String, String> getUsedTerms() {
		return usedTerms;
	}


	public void setUsedTerms(Map<String, String> usedTerms) {
		this.usedTerms = usedTerms;
	}
	

}
