package testing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


public class Stats {
	
	public static void makeStats() {
		Integer i = 0;
		Integer number_all_doclinks = 0;
		ArrayList<String> domains_tokens = new ArrayList<String>();
		HashSet<String> all_doclinks_set = new HashSet<String>();
		try(BufferedReader br = new BufferedReader(new FileReader("filtered_nfdump.txt"))) {
			String line = new String();
			while (br.ready()) {
				line = br.readLine();
				String[] tabs = line.split("\t");
				String links_str = tabs[tabs.length-1];
				String[] doclinks = links_str.split(",");
				if (tabs[0].contains("T")) {
					//System.out.println("query " + i + ") doclinks: " + doclinks.length + ": " + String.join(",", doclinks));	
				}
				if (tabs[0].contains("V")){
					//continue;
				}
				io.Writer.appendLineToFile("query " + i + ") doclinks: " + doclinks.length + ": " + String.join(",", doclinks), "stats.txt");
				i ++;
				
		    	if (doclinks.length == 1 && doclinks[0].equals("-")) {
		    		continue;
		    	}
		    	
				number_all_doclinks += doclinks.length;
				
			    for (String link: doclinks) {
			    	if(!link.contains("pdf")){
				    	if (link.contains("www.fda.gov")) {
				    		//io.Writer.appendLineToFile(link, "pubmedlinks.txt");
				    		//System.out.println(link);
				    	}
				    	String[] linksplit = link.split("/");
				    	
				    	if (linksplit.length < 3) {
				    		System.out.println("FAIL " + link);
				    		continue;
				    	} else if (!linksplit[2].isEmpty() && !all_doclinks_set.contains(link)) {
				    		domains_tokens.add(linksplit[2]);
				    		all_doclinks_set.add(link);
				    		//if (link.contains("itunes")) {System.out.println(link + "  " + linksplit[2]);}
				    	}
			    		
			    	}
			    	

			    	
			    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		HashMap<String, Integer> domainsmap = new HashMap<String, Integer>();
		for (String s: domains_tokens) {
			//System.out.println(s);
			if (domainsmap.containsKey(s)) {
				domainsmap.put(s, domainsmap.get(s) + 1);
			} else {
				domainsmap.put(s, 1);
			}
		}
		HashMap<Integer, ArrayList<String>> freqs = new HashMap<Integer, ArrayList<String>>();
		for (Map.Entry<String, Integer> e: domainsmap.entrySet()) {
			if (freqs.containsKey(e.getValue())) {
				freqs.get(e.getValue()).add(e.getKey());
			} else {
				freqs.put(e.getValue(), new ArrayList<String>());
				freqs.get(e.getValue()).add(e.getKey());
			}
		}
		SortedMap<Integer, ArrayList<String>> freqs_treemap = new TreeMap<Integer, ArrayList<String>>(freqs);
		for (Map.Entry<Integer, ArrayList<String>> e: freqs_treemap.entrySet()) {
			System.out.println(e.getKey()+ ": (" + e.getValue().size() + ") "+ e.getValue().toString());
		}
		
		System.out.println("number of all doclinks (tokens): " + number_all_doclinks);
		System.out.println("number of doclinks (types): " + all_doclinks_set.size());
		
		// get nbci and pubmed links...
		
		Integer num_pubmed_links = 0;
		Integer num_nbci_links = 0;
		Integer num_pmc_links = 0;
		for (String link: all_doclinks_set) {
			if (link.split("/")[2].equals("www.ncbi.nlm.nih.gov")) {
				num_nbci_links += 1;
				//io.Writer.appendLineToFile(link, "nbci_links.txt");
			}
			if (link.contains("http://www.ncbi.nlm.nih.gov/pubmed/")) {
				num_pubmed_links += 1;
				//io.Writer.appendLineToFile(link, "pubmed_links.txt");
			} else if (link.contains("http://www.ncbi.nlm.nih.gov/pmc/articles/")) {
				num_pmc_links += 1;
			}
		}
		System.out.println("number of nbci links (types): " + num_nbci_links);
		System.out.println("number of pubmed links (types): " + num_pubmed_links);
		System.out.println("number of pmc links (types): " + num_pmc_links);
	}
	
	public static void main(String[] args) {
		Stats.makeStats();

	}

}
