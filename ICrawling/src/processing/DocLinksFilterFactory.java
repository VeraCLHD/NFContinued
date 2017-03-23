package processing;

import crawling_docs.DocCrawler;
import crawling_docs.DocCrawlerAbstractsOnlyMode;
import crawling_docs.DocCrawlerFullTextMode;
import crawling_docs.ExpectedConnectionException;

public class DocLinksFilterFactory {
	
	public static DocLinksFilter createFilter(String arg){
	
	DocLinksFilter doc_filter = null;
	
	if(arg.equalsIgnoreCase("-fulltext")){
		doc_filter = new DocLinksFilterFullTextMode();
	} else if (arg.equalsIgnoreCase("-abst_only")) {
		doc_filter = new DocLinksFilterAbstractsOnlyMode();
	}
	
	return doc_filter;
}
	
}


