/**
 * 
 */
package relations_identification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.Writer;
import linguistic_processing.StanfordLemmatizer;

/**
 * @author XMobile
 *
 */
public abstract class QueryRelationsExplorer {
	
	private String queryID;
	private String linkToText;
	private List<Relation> relationsForOneText = new ArrayList<Relation>();
	

	private String textLower;
	private String title;

	
	public void readAndInitializeQuery(String line){
		
		String[] elements = line.split("\t");
		this.setQueryID(elements[0].trim());
		System.out.println(this.queryID);
		this.setLinkToText(elements[1]);
		this.setTitle(elements[2]);
		this.setTextLower(elements[3].toLowerCase());
		for(String topic: elements[5].split(",")){
			topic = topic.trim().toLowerCase();
			if(!topic.equals("-")){
				
				InitialRelationsManager.getTermsOverall().put(topic, lemma);
				// Here a link between a query and a term is provided (contains duplicates)
				Writer.appendLineToFile(this.getQueryID() + "\t" + topic + "\t" + lemma, "termsOverall.txt");
			}
			
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

}
