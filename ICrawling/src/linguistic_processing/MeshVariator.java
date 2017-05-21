package linguistic_processing;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Node;

import crawling_docs.DelayManager;
import io.Reader;
import io.Writer;
import relations_identification.InitialRelationsManager;
import relations_identification.Term;
import run.createQCLIRCorpus;

public class MeshVariator {
	// the object that manages the delay depending on the domain
	private static final DelayManager delay_manager = new DelayManager();
	private static final String domain = extractDomain("https://www.ncbi.nlm.nih.gov/mesh/?term=");
	
	private static Map<String, String> is_a_map = new HashMap<String, String>();
	
	public static Map<String, Set<String>> contentOfMeshFile = new HashMap<String, Set<String>>();
	
	

	public MeshVariator() {
		readMesh();
	}
	
	public void readMesh(){
		List<String> variations = readFileLinewise("meshVariants.txt");
		for(String variation: variations){
			if( variation != null && !variation.equals("") && !variation.isEmpty()){
				// only if # the term has variations
				String[] oneEntry = variation.split("\t");
				if(oneEntry.length > 2){
					String term = oneEntry[0].trim();
					Set<String> meshVariations = new HashSet<String>(Arrays.asList(oneEntry[2].split(",")));
					// all morphological variations in this list
					
						MeshVariator.getContentOfMeshFile().put(term, meshVariations);
					
					
				}
			}
		
	}
	}
	

	
	/**
	 * Extracts the domain from the initial link. Used for tracking the delay within a domain when two or more pages of the same domain
	 * are crawled after one another.
	 * @see DelayManager
	 */
	public static String extractDomain(String link) {
		String domain = "http://" + link.split("/")[2];
		return domain;
	}
	
	
	
	public static String crawlAndWriteVariations(String lemma, String termItself){
		lemma = lemma.trim();
		String termWithVariations = termItself + "\t";
		Document doc = null;
		getDelayManager().delay(domain);
		
			try {
				Response connection = Jsoup.connect("https://www.ncbi.nlm.nih.gov/mesh/?term=" + lemma)
			    .userAgent("Student Project Uni Heidelberg (boteva@cl.uni-heidelberg.de, gholipour@stud.uni-heidelberg.de")
				.execute();
						
				doc = connection.parse();
				Elements variationsTag = doc.select("p:contains(Entry Terms:)");
				if(!variationsTag.isEmpty()){
					Elements relElements = variationsTag.get(0).nextElementSibling().children();
					
					for (Element element: relElements){
						String text = element.text().toLowerCase();
						if(text.contains(",") && text.contains(" ")){
							String[] arr = text.split(",");
							String first_word = arr[1].trim();
							String second_word = arr[0].trim();
							text = first_word.toLowerCase() + " " + second_word.toLowerCase();
						}

						if( text != null && !text.equals("") && !text.isEmpty()){
							termWithVariations += text + ",";
						}
						}
					//Crawl tree of terms to write
					List<String> tree = crawlMeshTrees(doc);
					for(int i = tree.size()-1 ; i>0  ;i--){
						String child = tree.get(i).toLowerCase();
						String parent = tree.get(i-1).toLowerCase();
						MeshVariator.getIs_a_map().put(child, parent);
					}
				}


						} catch (IOException e) {
							System.out.println("The " + lemma + "could not be crawled.");
							
						}

		return termWithVariations;
	}
	
	public static List<String> crawlMeshTrees(Document doc){
		List<String> treeList = new ArrayList<String>();
		Elements root = doc.select("ul").select("a:contains(All MeSH Categories)");
		
		if(!root.isEmpty()){
			Element tree_r = root.get(0).nextElementSibling();
			for(Element treeel: tree_r.select("a")){
				treeList.add(treeel.text());
			}
			
			treeList.add(tree_r.select("b").text());
		}
		
		return treeList;
	}
	
	public static List<String> readFileLinewise(String file){
		ArrayList<String> termsOverall = Reader.readLinesList(file);
		return termsOverall;
	}
	
	public static Map<String, Set<String>> readMeshVariations(String file){
		Map<String,Set<String>> varForLemma = new HashMap<String, Set<String>>();
		HashSet<String> lines = new HashSet<String>(Reader.readLinesList(file));
		
		for(String line: lines){
			if(!line.isEmpty()){
				String[] lineSplitted = line.split("\t");
				String term = lineSplitted[0];
				if(lineSplitted.length> 1){
					varForLemma.put(term, new HashSet<String>(Arrays.asList(lineSplitted[1].split(","))));
				} else{
					varForLemma.put(term, new HashSet<String>());
				}
				
			}

		}
		
		return varForLemma;
	}
	
	
	public static void writeTerminologyVariations(){
		Writer.overwriteFile("", "mesh_variations.txt");
		Writer.overwriteFile("", "mesh_tree.txt");
		Set<Term> terms = InitialRelationsManager.getTerms();
		for (Term t: terms){
			// the original term
			String term = t.getOriginalTerm();
			if(!term.isEmpty() && !term.equals("\\s")){
				
				// we use the lemma to check in Mesh; otherwise very often nothing is found.
				String lemma = t.getLemma();
				
				if(term.matches(".*\\p{Punct}") || term.contains(" ") ){
					continue;
				}
				String line = crawlAndWriteVariations(lemma, term);
				Writer.appendLineToFile(line, "mesh_variations.txt");
			}

		}
		
		for(String child: MeshVariator.getIs_a_map().keySet()){
			String is_a = child + "\t" + MeshVariator.getIs_a_map().get(child);
			Writer.appendLineToFile(is_a, "mesh_tree.txt");
		}
	}
	
	
	
	public static void main(String[] args) {
		writeTerminologyVariations();
		//crawlAndWriteVariations("blueberries", "blueberry");

	}

	public static DelayManager getDelayManager() {
		return delay_manager;
	}

	public static Map<String, String> getIs_a_map() {
		return is_a_map;
	}

	public static void setIs_a_map(Map<String, String> is_a_map) {
		MeshVariator.is_a_map = is_a_map;
	}
	public static Map<String, Set<String>> getContentOfMeshFile() {
		return contentOfMeshFile;
	}

	public static void setContentOfMeshFile(Map<String, Set<String>> contentOfMeshFile) {
		MeshVariator.contentOfMeshFile = contentOfMeshFile;
	}

}
