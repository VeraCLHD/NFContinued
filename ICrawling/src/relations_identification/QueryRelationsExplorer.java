/**
 * 
 */
package relations_identification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author XMobile
 *
 */
public abstract class QueryRelationsExplorer {
	
	private String queryID;
	private String linkToText;
	private List<Relation> relationsForOneText = new ArrayList<Relation>();
	private Set<String> termsForOneText = new HashSet<String>();
	

	private String textLower;
	private String title;
	
	
	public void readAndInitializeQuery(String line){
		String[] elements = line.split("\t");
		this.setQueryID(elements[0].trim());
		System.out.println(this.queryID);
		this.setLinkToText(elements[1]);
		//System.out.println(this.linkToText);
		this.setTitle(elements[2]);
		//System.out.println(this.title);
		this.setTextLower(elements[3].toLowerCase());
		//System.out.println(this.textLower);
		for(String topic: elements[5].split(",")){
			termsForOneText.add(topic.trim().toLowerCase());
		}
	}
	
	
	public abstract void extractRelations();
	
	
	public String getQueryID() {
		return queryID;
	}
	public void setQueryID(String queryID) {
		this.queryID = queryID;
	}
	public List<Relation> getRelationsForOneText() {
		return relationsForOneText;
	}
	public void setRelationsForOneText(List<Relation> relationsForOneText) {
		this.relationsForOneText = relationsForOneText;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}

	public String getLinkToText() {
		return linkToText;
	}

	public void setLinkToText(String linkToText) {
		this.linkToText = linkToText;
	}


	public String getTextLower() {
		return textLower;
	}


	public void setTextLower(String textLower) {
		this.textLower = textLower;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Set<String> getTermsForOneText() {
		return termsForOneText;
	}


	public void setTermsForOneText(Set<String> termsForOneText) {
		this.termsForOneText = termsForOneText;
	}

}
