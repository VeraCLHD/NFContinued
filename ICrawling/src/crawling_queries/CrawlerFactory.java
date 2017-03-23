package crawling_queries;

/**
 * Provides a method to build the correct Crawler object
 * based on a link, e.g. a Crawler for Article pages
 * based on an article link from nutritionfacts.org.
 * 
 * @author Vera Boteva, Demian Gholipour
 */

public class CrawlerFactory {
		public QueryPageCrawler createCrawler(String link){
			
		QueryPageCrawler qpc = null;
		if(QueryPageCrawler.isArticleLink(link)){
			qpc = new ArticlePageCrawler(link);
		} else if (QueryPageCrawler.isQuestionLink(link)) {
			qpc = new QuestionPageCrawler(link);
		} else if(QueryPageCrawler.isTopicLink(link)){
			qpc = new TopicPageCrawler(link);
		} else if (QueryPageCrawler.isVideoLink(link)){
			qpc = new VideoPageCrawler(link);
		}
		return qpc;
	}

}
