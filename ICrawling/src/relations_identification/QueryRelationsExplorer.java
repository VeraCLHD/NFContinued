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

import linguistic_processing.StanfordLemmatizer;

/**
 * @author XMobile
 *
 */
public abstract class QueryRelationsExplorer {
	
	private String queryID;
	private String linkToText;
	private List<Relation> relationsForOneText = new ArrayList<Relation>();
	private Map<String, String> termsForOneText = new HashMap<String, String>();
	

	private String textLower;
	private String title;
	/**
	 * Used and unused terms refer to the words from each text that are qualified as terms, e.g through lemmatization.
	 * As opposed to the terms themselves that could be a lot less.
	 */
	Map<String,String> unusedTerms = new HashMap<String, String>();
	Map<String,String> usedTerms = new HashMap<String, String>();
	
	public Map<String, String> getUnusedTerms() {
		return unusedTerms;
	}


	public void setUnusedTerms(Map<String, String> unusedTerms) {
		this.unusedTerms = unusedTerms;
	}


	public Map<String, String> getUsedTerms() {
		return usedTerms;
	}


	public void setUsedTerms(Map<String, String> usedTerms) {
		this.usedTerms = usedTerms;
	}

	
	
	
	public void readAndInitializeQuery(String line){
		StanfordLemmatizer lemm = new StanfordLemmatizer();
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
			topic = topic.trim().toLowerCase();
			String lemma = lemm.lemmatize(topic);
			termsForOneText.put(topic, lemma);
		}
	}
	
	
	public abstract void extractRelations();
	
	public Map<String, String> determindeUnusedTerms(){
		StanfordLemmatizer lemm = new StanfordLemmatizer();
		for(String term: this.getTermsForOneText().keySet()){
			if(!this.getUsedTerms().containsKey(term)){
				
				String termLemma = lemm.lemmatize(term);
				this.getUnusedTerms().put(term, termLemma);

			}
		}
		
		return unusedTerms;
		
	}
	
	
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
	
	public Map<String, String> getTermsForOneText() {
		return termsForOneText;
	}


	public void setTermsForOneText(HashMap<String, String> termsForOneText) {
		this.termsForOneText = termsForOneText;
	}

}
