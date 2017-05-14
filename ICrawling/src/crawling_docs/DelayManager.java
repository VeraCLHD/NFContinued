package crawling_docs;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Is responsible for the time delays between visiting different URLs during the crawling process.
 * The delay is only necessary if pages of the same domain are visited in a certain period.
 * So the last time of a connection is saved and updated constantly for all domains.
 * <p>
 * During the crawling process, the DocCrawler class has a DelayManager object as an instance variable.
 * It's delay method is only called in the DocPageCrawler (setConnectionAndGetHtml method)
 * and in the PDFHandler class (setConnectionToPDFLink method).
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class DelayManager {
	
	private HashMap<String, Long> domain_last_time_map = new HashMap<String, Long>();
	
	/**
	 * Each time a connection to an url is made with jsoup or an URL Connection, the last domain
	 * where a document was crawled, is checked. If it is the same as the domain of the last url,
	 * the connection is delayed. The goal is to always keep a certain delay between different connections.
	 * 
	 * @param domain String that is extracted from the link with witch a connection is built
	 */
	public void delay(String domain) {
		Long time = System.nanoTime();
		if (this.getDomain_last_time_map().containsKey(domain)) {
			Long last_time = this.getDomain_last_time_map().get(domain);
			Long passed_time = time - last_time;
			Long delay = 0L;
			if (domain.contains(DocProperties.NCBI_DOMAIN)) {
				delay = DocProperties.PUBMED_DELAY_NANOSECONDS - passed_time;
			} else {
				delay = DocProperties.DEFAULT_DELAY_NANOSECONDS - passed_time;
			}
			try {
				TimeUnit.NANOSECONDS.sleep(delay);
			} catch (InterruptedException e) {
				System.err.println("InterruptedException");
			}
		}
		time = System.nanoTime();
		this.getDomain_last_time_map().put(domain, time);
	}

	public HashMap<String, Long> getDomain_last_time_map() {
		return domain_last_time_map;
	}

	public void setDomain_last_time_map(HashMap<String, Long> domain_last_time_map) {
		this.domain_last_time_map = domain_last_time_map;
	}

}
