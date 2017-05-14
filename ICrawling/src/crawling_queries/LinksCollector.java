package crawling_queries;

import java.io.File;
import java.util.ArrayList;

/**
 * A class that collects query links from different start pages.
 * @author  Vera Boteva, Demian Gholipour
 *
 */

public abstract class LinksCollector {
	protected final int DELAY_SECONDS = 4;
	protected final int TIMEOUT = 10000; // milliseconds
	private ArrayList<String> links;
	private ArrayList<String> new_links_for_update;
	private String output_path;
	
	/**
	 * Collects all query links and writes them in the query link files.
	 */
	public void collectQueryLinks() {
		File f = new File(this.getOutput_path());
		if(f.exists()) { 
			readQueryLinks();
		} else {
			crawlQueryLinks();
		}
	}
	
	/**
	 * reads query links from the links file.
	 */
	public void readQueryLinks() {
		ArrayList<String> links_list = io.Reader.readLinesList(this.getOutput_path()); 
		this.setLinks(links_list);
	}
	
	public abstract void crawlQueryLinks();
	
	public abstract void updateLinks();

	public ArrayList<String> getLinks() {
		return links;
	}
	
	public void setLinks(ArrayList<String> links) {
		this.links = links;
	}
	
	public String getOutput_path() {
		return output_path;
	}
	
	public void setOutput_path(String output_path) {
		this.output_path = output_path;
	}

	public ArrayList<String> getNew_links_for_update() {
		return new_links_for_update;
	}

	public void setNew_links_for_update(ArrayList<String> new_links_for_update) {
		this.new_links_for_update = new_links_for_update;
	}

}
