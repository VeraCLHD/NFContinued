/**
 * 
 */
package relations_identification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.Reader;
import io.Writer;

/**
 * @author Vera
 * This class manages all relations and terms from all texts.
 */
public class InitialRelationsManager {
	// All terms from all texts that are there
	private static Set<String> overallTerms = new HashSet<String>();
	private List<Relation> overallRelations = new ArrayList<Relation>();
	private String pathToNFDump;
	
	public InitialRelationsManager(String pathToNFDump){
		this.setPathToNFDump(pathToNFDump);
		
	}
	
	public void doInitialExtraction(){
		List<String> linesOfDump = Reader.readLinesList(pathToNFDump);
		Writer.overwriteFile("", "initial_relations.txt");
		
		for (String line: linesOfDump) {
			if(!line.isEmpty()){
				QueryRelationsExplorer initialExplorer = new InitialQueryRelationsExplorer(line);
				InitialRelationsManager.getOverallTerms().addAll(initialExplorer.getTermsForOneText());
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
	}
	
	public static Set<String> getOverallTerms() {
		return overallTerms;
	}
	public static void setOverallTerms(Set<String> overallTerms) {
		InitialRelationsManager.overallTerms = overallTerms;
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
	

}
