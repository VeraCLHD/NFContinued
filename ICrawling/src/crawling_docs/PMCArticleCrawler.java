package crawling_docs;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Crawls the content of a PMC Article Page. 
 * PMC Article Pages are often linked as full text links on PubMed pages and usually contain a whole article including the abstract.
 * 
 * @author Vera Boteva, Demian Gholipour
 */

public class PMCArticleCrawler extends DocPageCrawler {
	
	public PMCArticleCrawler(String link) throws ExpectedConnectionException {
		super(link);
	}
	
	/**
	 * Extracts the full text of the PMC page, including the abstract. 
	 * Sections that don't belong to the texts (e.g. references) are excluded reliably,
	 * since their html tags are always the same on PMC article  pages.
	 */
	
	public void crawlText() {
		Document page = this.getHTMLContent();
		Elements els = page.select("div[id]").select("div[class~=tsec sec.*]");
		String text = "";
		for (Element e: els) {
			Elements h2s = e.select("h2");
			
			if (!h2s.text().matches("(Supplementary Material|Acknowledgments|Footnotes|References|References and Notes)")) {
				text += e.text();
			}		
		}
		this.setText(text);
	}
	
	/**
	 * Crawls the complete content of a PMC article page: doi, title, abstract, text without references.
	 */
	@ Override
	public void crawlContent() {
		if (this.hasHTML()) {
			this.crawlDOI();
			this.crawlTitleFromJournalPage();
			this.crawlAbstract();
			this.crawlText();
			this.collectErrors();	
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
			this.collectErrors();	
		} else {
			this.setErrorsOccuredDuringCrawling(getErrorsOccuredDuringCrawling() + "\t" + "link:" + "\t" + this.getLinkToSite() + "no HTML"); 
		}
	}
	
	
	
}
