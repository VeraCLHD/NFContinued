package crawling_docs;

public class DocCrawlerFactory {
	
	public static DocCrawler createCrawler(String arg){
		
		DocCrawler doc_crawler = null;
		
		if(arg.equalsIgnoreCase("-fulltext")){
			doc_crawler = DocCrawlerFullTextMode.getInstance();
		} else if (arg.equalsIgnoreCase("-abst_only")) {
			doc_crawler = DocCrawlerAbstractsOnlyMode.getInstance();
		}
		
		return doc_crawler;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
