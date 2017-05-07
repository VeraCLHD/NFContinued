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
	private Set<Relation> relationsForOneText = new HashSet<Relation>();
	private List<Term> terms = new ArrayList<Term>();

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
				Term term = new Term(topic);
				List<String> list = InitialRelationsManager.getCatVar().get(term);
				if(list == null || list.isEmpty()){
					term.setCatvariations(new ArrayList<String>());
				} else{
					term.setCatvariations(list);
				}
				
				List<String> meshList = InitialRelationsManager.getMeshTerms().get(term);
				if(meshList == null || meshList.isEmpty()){
					term.setMesh(new ArrayList<String>());
				} else{
					term.setMesh(meshList);
				} 
				
				// special case: a typo in the tag
				if(topic.equals("cance")){
					term.setOriginalTerm("cancer");
				}
				
				InitialRelationsManager.getTerms().add(term);
				InitialRelationsManager.getTermsOverall().put(topic, term.getLemma());
				// Here a link between a query and a term is provided (contains duplicates)
				Writer.appendLineToFile(this.getQueryID() + "\t" + topic + "\t" + term.getLemma(), "termsOverall.txt");
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
	public Set<Relation> getRelationsForOneText() {
		return relationsForOneText;
	}
	public void setRelationsForOneText(Set<Relation> relationsForOneText) {
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


	public List<Term> getTerms() {
		return terms;
	}


	public void setTerms(List<Term> terms) {
		this.terms = terms;
	}

}
