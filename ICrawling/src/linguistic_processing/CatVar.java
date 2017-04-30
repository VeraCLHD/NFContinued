package linguistic_processing;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import io.Reader;
import io.Writer;

public class CatVar {

	public CatVar() {
		// TODO Auto-generated constructor stub
	}
	
	public static String crawlAndWriteVariations(String term, String termItself){
		term = term.trim();
		String termWithVariations = termItself + "\t";
		Document doc = null;
		//past-week-april-14-21-2017/
			try {
				Response connection = Jsoup.connect("https://clipdemos.umiacs.umd.edu/cgi-bin/catvar/webCVsearch.pl?query=" + term+ "&submit=CatVariate%21")
			    .userAgent("Student Project Uni Heidelberg (boteva@cl.uni-heidelberg.de, gholipour@stud.uni-heidelberg.de")
				.execute();
						
				doc = connection.parse();
				Elements releventElements = doc.select("td").select("tr");
				for (int i=0; i < releventElements.size(); i++){
					String text = releventElements.get(i).text();
					//CATVAR 2.0: not needed; verb supplements, main row
					if( text != null && !text.equals("") && !text.isEmpty() && !text.startsWith("CATVAR 2.0") && !text.equals("_")){
						if(!text.startsWith("Word")){
							String[] derivation = releventElements.get(i).select("b").text().split("\\s");
							if(derivation.length > 1){
								termWithVariations += derivation[0] + " ";
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
	
	public static ArrayList<String> readTermsFile(){
		ArrayList<String> termsOverall = Reader.readLinesList("all_terms.txt");
		return termsOverall;
	}
	
	public static void writeTerminologyVariations(){
		Writer.overwriteFile("", "terminology_variations.txt");
		ArrayList<String> terms = readTermsFile();
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
				Writer.appendLineToFile(line, "terminology_variations.txt");
			}
			
			
		}
	}
	
	public static void main(String[] args) {
		writeTerminologyVariations();

	}

}
