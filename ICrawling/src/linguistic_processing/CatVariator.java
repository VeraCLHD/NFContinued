package linguistic_processing;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import io.Reader;
import io.Writer;

public class CatVariator {
	public static Map<String, List<String>> contentOfCatVarFile = new HashMap<String, List<String>>();
	
	public CatVariator() {
		
	}
	
	public static void readCatVar(){
		ArrayList<String> variations = readFileLinewise("catvar21");
		for(String variation: variations){
			// only if # the term has variations
			if(variation.contains("#")){
				String[] oneVar = variation.split("_(\\w)*#");
				if(oneVar.length > 1){
					String lemma = oneVar[1].trim();
					List<String> vars = Arrays.asList(oneVar).subList(1, oneVar.length);
					CatVariator.getContentOfCatVarFile().put(lemma, vars);
				}
			}
			
		}
	}
	
	public static String crawlAndWriteVariations(String term, String termItself){
		term = term.trim();
		String termWithVariations = termItself + "\t";
		Document doc = null;
		//past-week-april-14-21-2017/
			try {
				Response connection = Jsoup.connect("https://clipdemos.umiacs.umd.edu/cgi-bin/catvar/webCVsearch.pl?query=" + term + "&length=&submit=CatVariate%21")
			    .userAgent("Student Project Uni Heidelberg (boteva@cl.uni-heidelberg.de, gholipour@stud.uni-heidelberg.de")
				.execute();
						
				doc = connection.parse();
				Elements releventElements = doc.select("td").select("tr");
				
				for (int i=0; i < releventElements.size(); i++){
					String text = releventElements.get(i).text();
					if(text.startsWith("CATVAR 2.0 Preposition-Verb Supplement")){
						System.out.println(text);
						// assumption: the preposition-verb supplements are at the end
						break;
					}
					
					//CATVAR 2.0: not needed; verb supplements, main row
					if( text != null && !text.equals("") && !text.isEmpty() && !text.startsWith("CATVAR 2.0") && !text.equals("_")){
						if(!text.startsWith("Word")){
							
							String[] derivation = releventElements.get(i).select("b").text().split("\\s");
							if(derivation.length > 1){
								termWithVariations += derivation[0] + ",";
							}
							
						}
					}
				}

						} catch (IOException e) {
							System.out.println(term);
							//crawlAndWriteVariations(term, termItself);
							
						}
		
		
		return termWithVariations;
	}
	
	public static ArrayList<String> readFileLinewise(String file){
		ArrayList<String> termsOverall = Reader.readLinesList(file);
		return termsOverall;
	}
	
	public static void crawlAndWriteTerminologyVariations(){
		Writer.overwriteFile("", "terminology_variations_catvar.txt");
		ArrayList<String> terms = readFileLinewise("all_terms.txt");
		for (String term: terms){
		
		
			if(!term.isEmpty() && !term.equals("\\s")){
				String[] oneTerm = term.split("\t");
				// we use the lemma to check in CATVAR; otherwise very often nothing is found.
				String lemma = oneTerm[1].trim();
				String termItself = oneTerm[0].trim();
				if(termItself.matches(".*\\p{Punct}") || termItself.contains(" ") ){
					continue;
				}
				String line = crawlAndWriteVariations(lemma, termItself);
				Writer.appendLineToFile(line, "terminology_variations_catvar.txt");
			}
			
			
		}
	}
	
	/**
	 * A method for writing  reading the catVars of the CatVar file.
	 */
	public static void writeTerminologyVariations(){
		Writer.overwriteFile("", "terminology_variations_catvar.txt");
		ArrayList<String> terms = readFileLinewise("all_terms.txt");
		
		 
		for (String term: terms){
		
		
			if(!term.isEmpty() && !term.equals("\\s")){
				String[] oneTerm = term.split("\t");
				// we use the lemma to check in CATVAR; otherwise very often nothing is found.
				String lemma = oneTerm[1].trim();
				String termItself = oneTerm[0].trim();
				if(termItself.matches(".*\\p{Punct}") || termItself.contains(" ") ){
					continue;
				}
				String line = termItself + "\t";
				if(contentOfCatVarFile.get(lemma) !=null){
					
					for(String var: contentOfCatVarFile.get(lemma)){
						line += var.trim() + ",";
					}
					
				}
				
				Writer.appendLineToFile(line, "terminology_variations_catvar.txt");
				
				

			}
			
			
		}
	}
	
	
	/**
	 * A method for reading the output of the catVariator - all variations for a certain term (lemma)
	 * @param file
	 * @return
	 */
	public static Map<String, Set<String>> readCatVariations(String file){
		Map<String,Set<String>> varForLemma = new HashMap<String, Set<String>>();
		ArrayList<String> lines = Reader.readLinesList(file);
		
		for(String line: lines){
			if(!line.isEmpty()){
				String[] lineSplitted = line.split("\t");
				String term = lineSplitted[0];
				if(lineSplitted.length> 1){
					varForLemma.put(term, new HashSet<String>(Arrays.asList(lineSplitted[1].split(" "))));
				} else{
					varForLemma.put(term, new HashSet<String>());
				}
				
			}

		}
		
		return varForLemma;
	}
	
	public static Map<String, List<String>> getContentOfCatVarFile() {
		return contentOfCatVarFile;
	}

	public static void setContentOfCatVarFile(Map<String, List<String>> contentOfCatVarFile) {
		CatVariator.contentOfCatVarFile = contentOfCatVarFile;
	}

	
	
	public static void main(String[] args) {
		crawlAndWriteTerminologyVariations();

	}

}
