package linguistic_processing;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import io.Reader;
import io.Writer;

public class CatVariator {
	public static Map<String, Set<String>> contentOfCatVarFile = new HashMap<String, Set<String>>();
	
	public CatVariator() {
		readCatVar();
	}
	
	public void readCatVar(){
		ArrayList<String> variations = readFileLinewise("catvar21");
		for(String variation: variations){
			// only if # the term has variations
			if(variation.contains("#")){
				String[] oneVar = variation.split("#");
				if(oneVar.length > 1){
					String lemma = oneVar[1].trim();
					lemma = lemma.substring(0, lemma.indexOf("_"));
					// all morphological variations in this list
					Set<String> vars = new HashSet<String>(Arrays.asList(oneVar).subList(1, oneVar.length));
					Set<String> finalVars = new HashSet<String>();
					for(String var: vars){
						if(var.contains("_")){
							finalVars.add(var.trim().substring(0, var.indexOf("_")).toLowerCase());
						}
					}
					CatVariator.getContentOfCatVarFile().put(lemma, finalVars);
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
	 * A method for writing the catVars of the CatVar file.
	 */
	public static void writeTerminologyVariations(String inputfile, String outputfile){
		Writer.overwriteFile("", "terminology_variations_catvar.txt");
		ArrayList<String> terms = readFileLinewise(inputfile);
		
		 
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
				

				if(contentOfCatVarFile.containsKey(lemma)){
					for(String var: contentOfCatVarFile.get(lemma)){
						line += var.trim() + ",";
					}
				} else{
					for(Entry<String,Set<String>> variation: contentOfCatVarFile.entrySet()){
						if(variation.getValue().contains(term) || variation.getValue().contains(lemma)){
							line += variation.getKey() + ",";
							for(String varList: variation.getValue()){
								line += varList + ",";
							}
							break;
						}
					}
				}
				
				
				if(contentOfCatVarFile.get(lemma) !=null){
					
					
					
				}
				
				Writer.appendLineToFile(line, outputfile);
				
				

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
	
	public static Map<String, Set<String>> getContentOfCatVarFile() {
		return contentOfCatVarFile;
	}

	public static void setContentOfCatVarFile(Map<String, Set<String>> contentOfCatVarFile) {
		CatVariator.contentOfCatVarFile = contentOfCatVarFile;
	}

	
	
	public static void main(String[] args) {
		crawlAndWriteTerminologyVariations();

	}

}
