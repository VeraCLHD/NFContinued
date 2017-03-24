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

/**
 * @author Vera
 * This class manages all relations and terms from all texts.
 */
public class InitialRelationsManager {
	private static Set<String> overallTerms = new HashSet<String>();
	private static Set<String> terms = new HashSet<String>();
	private String pathToNFDump;
	
	public InitialRelationsManager(String pathToNFDump){
		this.setPathToNFDump(pathToNFDump);
		
	}
	
	public void readInputAndAddTerms(){
		List<String> linesOfDump = Reader.readLinesList(pathToNFDump);
		
		
		for (String line: linesOfDump) {
			if(!line.isEmpty()){
				QueryRelationsExplorer initialExplorer = new InitialQueryRelationsExplorer(line);
			}
			
		}
	}
	
	public static Set<String> getOverallTerms() {
		return overallTerms;
	}
	public static void setOverallTerms(Set<String> overallTerms) {
		InitialRelationsManager.overallTerms = overallTerms;
	}
	
	public static Set<String> getTerms() {
		return terms;
	}
	public static void setTerms(Set<String> terms) {
		InitialRelationsManager.terms = terms;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
	}

	public String getPathToNFDump() {
		return pathToNFDump;
	}

	public void setPathToNFDump(String pathToNFDump) {
		this.pathToNFDump = pathToNFDump;
	}
	

}
