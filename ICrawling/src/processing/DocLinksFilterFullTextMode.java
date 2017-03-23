package processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import crawling_docs.DocProperties;
import crawling_queries.Properties;

/**
 * A class that filters all non relevant links from 
 * Filter criteria:
 * 1) each direct pdf link is taken into the corpus. Single exception: "http://www.greatwar.nl/books/meinkampf/meinkampf.pdf"
 * 2) each direct link from the domains http://www.ncbi.nlm.nih.gov are taken into the corpus
 * 3) for all other links, only a handful of journals was chosen based on counting the most frequent journals.
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class DocLinksFilterFullTextMode extends DocLinksFilter{
	
	// a list for manually excluded scientific documents
	public static final String[] MANUALLY_EXCLUDED_DIRECT_PDFS = {
		"http://www.greatwar.nl/books/meinkampf/meinkampf.pdf"
	};
	
	/*
	 * this list was created based on the current crawling data from June 2015.
	 * When the corpus is used in the future, adjustments should be made to this list/ 
	 */
	public static final String[] 
			ALLOWED_JOURNAL_DOMAINS = {
				"ajcn.org",
				"sciencedirect.com",
				"onlinelibrary.wiley.com",
				"cdc.gov",
				"jn.nutrition.org",
				"ars.usda.gov",
				"nature.com",
				"fda.gov"	
				};
	
	
	/**
	 * Writes a single line of the initial dump after filtering the non-relevant documents from it.
	 * @param dumpline the line from the initial nf_dump as String.
	 */
	@Override
	public void writeFilteredDumpLine(String dumpline) {
		String[] components = dumpline.split("\t");
		String[] doclinks = components[components.length - 1].split(",");
		String filtered_links_string = "";
		for (String link: doclinks) {
			if (DocLinksFilter.matchesPattern(link, DocProperties.POSSIBLE_PDF_LINK_PATTERN) && !Arrays.asList(MANUALLY_EXCLUDED_DIRECT_PDFS).contains(link)) {
				filtered_links_string += link + ",";
			} else if (DocLinksFilter.matchesPattern(link, DocProperties.NCBI_PAGE_PATTERN)){
				filtered_links_string += link + ",";
			} else {
				for (String domain: DocLinksFilterFullTextMode.ALLOWED_JOURNAL_DOMAINS) {
					if (link.contains(domain)) {
						filtered_links_string += link + ",";
						break;
					}
				}
			}
		}
		
		if(filtered_links_string == null || filtered_links_string.isEmpty()){
			filtered_links_string = "-";
		}
		
		components[components.length - 1] = filtered_links_string;
		String new_dumpline = String.join("\t", components);
		io.Writer.appendLineToFile(new_dumpline, DocProperties.FILTERED_NFDUMP_PATH);
	}
	


}
