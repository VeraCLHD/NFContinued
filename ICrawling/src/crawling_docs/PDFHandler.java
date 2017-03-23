package crawling_docs;

import io.Writer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Deals with (potential) direct PDF links to read their content.
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */

public class PDFHandler {
	
	/**
	 * Checks if a link is a PDF link, based on the content type.
	 * This does not imply that the PDF link is readable.
	 * The link is is received through its corresponding DocPageCrawler object
	 * which has saved the MIME type of its connection already.
	 * @param doc_page_crawler
	 */
	
	public static boolean isPDFPage(DocPageCrawler doc_page_crawler){
		return (doc_page_crawler.getMimeType().matches("[aA]pplication\\/pdf")); 
    }
	
	/**
	 * Checks if a link could be a PDF link, based on whether it contains the string "pdf".
	 * The purpose is to exclude links from possible PDF links without setting a URL connection.
	 */
	
	public static boolean isPotentialPDFLink(String link) {
		Matcher m = DocProperties.POSSIBLE_PDF_LINK_PATTERN.matcher(link);
		boolean b = m.matches();
		return b;
	}
	
	/**
	 * Checks if a (assumed) PDF link is the PDF link that contains the target document.
	 * Criterion: the previous link usually contains an ID at the end which should also
	 * appear in the PDF link. 
	 */
	
	public static boolean isPDFOfDocument(DocPageCrawler doc_page_crawler, String pdf_link){
		String initial_link = doc_page_crawler.getLinkToSite();
		Matcher m = DocProperties.DOC_ID_PATTERN.matcher(initial_link);
		String id = "";
		while(m.find()){
			id = m.group();
		}
		return pdf_link.contains(id);		
	}
	
	/**
	 * Finds the PDF link that contains the target document.
	 * <p>
	 * It starts out from a (Journal) page that usually contains PDF links.
	 * Often there are links that seem like PDF links but aren't ones.
	 * The page of such a link often contains an actual PDF link.
	 * This method considers these links too, but doesn't go further.
	 * @param doc_page_crawler provides the Journal page link
	 */
	
	public static String crawlPDFLink(DocPageCrawler doc_page_crawler) throws ExpectedConnectionException {
		ArrayList<String> correct_pdf_links = new ArrayList<String>();
		// find all supposed PDF links on the page that correspond to the target document
		Set<String> pdf_links = PDFHandler.findPDFLinks(doc_page_crawler);
		// further links are the potential pdf links fetched from a page that doesn't contain the end document (indirect path: full text link -> expected-to-be-end-pdf-link -> end pdf)
		ArrayList<String> further_pdf_links = new ArrayList<String>();
		// going through PDF links linked on the Journal Page
		for (String link: pdf_links) {
			DocPageCrawler doc_page_crawler2 = DocPageCrawlerFactory.createCrawler(link);
			// link is visited/checked 
			if (PDFHandler.isPDFPage(doc_page_crawler2)) {
				correct_pdf_links.add(link);
			} else {
				try {
					further_pdf_links.addAll(PDFHandler.findPDFLinks(doc_page_crawler2));
				} catch(Exception e) {
					doc_page_crawler.setErrorsOccuredDuringCrawling(doc_page_crawler.getErrorsOccuredDuringCrawling() + "\t" + doc_page_crawler.getLinkToSite() + "\t" + "crawlPDFLink" + "\t" + e.getMessage() + "\r\n");
				}
			}
		}
		// going through PDF links found on a page of a supposed PDF link 
		for (String link: further_pdf_links) {
			DocPageCrawler doc_page_crawler2 = DocPageCrawlerFactory.createCrawler(link);	
			if (PDFHandler.isPDFPage(doc_page_crawler2)) {
				correct_pdf_links.add(link);
			} 
		}
		if(correct_pdf_links.size() > 0) {
			doc_page_crawler.setErrorsOccuredDuringCrawling(doc_page_crawler.getErrorsOccuredDuringCrawling() + doc_page_crawler.getDoi() + "\t" + doc_page_crawler.getLinkToSite() + "\t" + "crawlPDFLinks" + "\t" + "more than one pdf link" + "\r\n");
			return correct_pdf_links.get(0);
		} else {
			return "";	
		}
	}
	
	/**
	 * Finds the PDF links in a DocPageCrawler's page. 
	 * The links are selected by whether they contain the String "pdf" and the ID in the DocPageCrawlers link.
	 * This should ensure that they are PDF links at all and do also correspond the target document.
	 * The links are not visited, so there can still be content type problems etc.
	 * @param doc_page_crawler a low level object that is created for every link.
	 */
	public static Set<String> findPDFLinks(DocPageCrawler doc_page_crawler) {
		String link_cssQuery = ("meta[content], a[href], link[href], iframe[id]");
		String[] relevant_attributes = {"content", "href", "src", "id"};
		Document html = doc_page_crawler.getHTMLContent();
		Elements els = html.select(link_cssQuery);
		HashSet<String> links_set = new HashSet<String>();
		for (Element e: els) {
			for(String attribute: relevant_attributes){
				if (!e.attr(attribute).isEmpty()) {
					String link = e.attr(attribute);
					if (link.startsWith("/")) {
						link = doc_page_crawler.getDomain() + link;
					}
					if(PDFHandler.isPotentialPDFLink(link) && PDFHandler.isPDFOfDocument(doc_page_crawler, link)){
						links_set.add(link);
					}		
				}
			}
		}
		if (links_set.isEmpty()){
			doc_page_crawler.setErrorsOccuredDuringCrawling(doc_page_crawler.getErrorsOccuredDuringCrawling() + doc_page_crawler.getLinkToSite() + "\t" + "crawlPotentialPDFLinks" + "\r\n");
		}
		return links_set;
	}
	
}
