package crawling_docs;

import java.util.ArrayList;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Crawls relevant data of a page of the www.ncbi.nlm.nih.gov domain (National Center for Biotechnology Information)
 * which are typically PubMed pages.
 * Usually these pages do not contain the wanted full text but links to those or an abstract.
 * This crawler can collect these data:
 * <p>
 * - doi <br>
 * - abstract <br>
 * - links to full texts <br>
 * - link to PMC full text <br>
 * <p>
 * The PMC full text links/pages have a special treatment because they are very frequent and always
 * of the same structure.
 * 
 * @author Vera Boteva, Demian Gholipour
 */

public class NCBIPageCrawler extends DocPageCrawler {

	
	
	public NCBIPageCrawler(String link)  throws ExpectedConnectionException {
		super(link);
	}
	
	/**
	 * Crawls the content of an NCBI page: doi, title, abstract, full text links, free pmc article.
	 */
	@Override
	public void crawlContent() {
		
		if (this.hasHTML()) {
			this.crawlDOI();
			this.crawlTitleFromJournalPage();
			this.crawlAbstract();
			this.crawlFullTextLinks();
			this.crawlFreePMCLink();
			
			String error_log = this.getLinkToSite() + "\t";
			boolean errors_occurred = false;
			Boolean[] booleans = {this.hasDOI(), this.hasTitle(), this.hasAbstract()};
			String[] components = {"DOI", "title", "abstract"};
			for (int i = 0; i < components.length; i++) {
				if (booleans[i] == false) {
					errors_occurred = true;
					error_log += "no " + components[i] + "\t"; 
				}
			}
			if (errors_occurred == true) {
				this.setErrorsOccuredDuringCrawling(error_log);	
			}	
		} else {
			this.setErrorsOccuredDuringCrawling(getErrorsOccuredDuringCrawling() + "\t" + "link:" + "\t" + this.getLinkToSite() + "no HTML"); 
		}
	}
	
	@Override
	public void crawlAbstractOnly() {
		
		if (this.hasHTML()) {
			this.crawlDOI();
			this.crawlTitleFromJournalPage();
			this.crawlAbstract();
			this.crawlFreePMCLink();
			
			String error_log = this.getLinkToSite() + "\t";
			boolean errors_occurred = false;
			Boolean[] booleans = {this.hasDOI(), this.hasTitle(), this.hasAbstract()};
			String[] components = {"DOI", "title", "abstract"};
			for (int i = 0; i < components.length; i++) {
				if (booleans[i] == false) {
					errors_occurred = true;
					error_log += "no " + components[i] + "\t"; 
				}
			}
			if (errors_occurred == true) {
				this.setErrorsOccuredDuringCrawling(error_log);	
			}	
		} else {
			this.setErrorsOccuredDuringCrawling(getErrorsOccuredDuringCrawling() + "\t" + "link:" + "\t" + this.getLinkToSite() + "no HTML"); 
		}
	}
	
	/**
	 * Crawls the link of a PMC (PubMed Central) article page of NCBI if available.
	 */
	
	public void crawlFreePMCLink(){
		Document ncbi_page = this.getHTMLContent();
		Elements elements = ncbi_page.select("a");
		for (Element element: elements) {
			if(element.text().equals("Free PMC Article")){
				String link = element.attr("href");
				if (!link.contains(DocProperties.NBCI_MAIN_PAGE)) {
					// often uncomplete link
					link = DocProperties.NBCI_MAIN_PAGE + element.attr("href");
				}
				this.setFree_pmc_link(link);
				return;
			}
		}
	}
	
	/**
	 * Crawls all full text links on the ncbi page.
	 */
	
	public void crawlFullTextLinks() {
		Document page =  this.getHTMLContent();
		Elements elements = page.getElementsByAttributeValue("class", "icons portlet");
		if (!elements.isEmpty()) {
			Elements link_elements = elements.get(0).getElementsByTag("a");
			ArrayList<String> links = new ArrayList<String>();
			for (Element e: link_elements) {
				links.add(e.attr("href"));
			}
			this.setFull_text_links(links);
		}
	}
	
	/**
	 * Crawls the doi (Digital Object Identifier) of the article if available.
	 */
	
	public void crawlDOI() {
		try  {
			Document page = this.getHTMLContent();
			Element e = page.select("meta[name = description]").first();
			if (!e.attr("content").contains("doi")) {
				return;
			}
			String string1 = e.attr("content").split("doi:")[1].trim();
			// period at the end not part of DOI
			String string2 = string1.split(" ")[0];
			String doi = string2.substring(0, string2.length() - 1);
			this.setDoi(doi);
		} catch (NullPointerException e) {
			System.err.println("NullPointerException: No DOI in crawlDOI (NCBIPageCrawler)");
		}
	}
	
	/**
	 * Crawls abstract if it is on the ncbi page.
	 */
	
	@Override
	public void crawlAbstract() {
		try  {
			Document page = this.getHTMLContent();
			Element e = page.select("div[class=abstr]").first();
			this.setAbstract_article(e.text());
		} catch (NullPointerException e) {
			System.err.println("NullPointerException: No Abstract in crawlAbstract (NCBIPageCrawler)");
		}
	}
	

	

}
