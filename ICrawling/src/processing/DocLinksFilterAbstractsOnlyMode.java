/**
 * 
 */
package processing;

import java.util.Arrays;

import crawling_docs.DocProperties;

/**
 * @author Vera
 *
 */
public class DocLinksFilterAbstractsOnlyMode extends DocLinksFilter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

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
			if (DocLinksFilter.matchesPattern(link, DocProperties.POSSIBLE_PDF_LINK_PATTERN)) {
				continue;
			} else if (DocLinksFilter.matchesPattern(link, DocProperties.NCBI_PAGE_PATTERN)){
				filtered_links_string += link + ",";
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
