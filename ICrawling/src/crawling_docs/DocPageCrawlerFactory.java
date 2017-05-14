/**
 * 
 */
package crawling_docs;



/**
 * Provides a method to build the correct Crawler object
 * based on a link, e.g. a Crawler for NCBI or NonNCBI pages
 * based on any link to a scientific document
 * 
 * @author Vera Boteva, Demian Gholipour
 */
public class DocPageCrawlerFactory {
		
		public static DocPageCrawler createCrawler(String link) throws ExpectedConnectionException {
			
			DocPageCrawler doc_crawler = null;
			
			if(DocPageCrawler.isPMCLink(link)){
				doc_crawler = new PMCArticleCrawler(link);
			} else if (DocPageCrawler.isNCBILink(link)) {
				doc_crawler = new NCBIPageCrawler(link);
			} else {
				doc_crawler = new NonNCBIPageCrawler(link);
			}
			
			return doc_crawler;
		}
}
