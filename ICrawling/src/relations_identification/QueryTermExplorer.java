/**
 * 
 */
package relations_identification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;

import io.Writer;
import linguistic_processing.StanfordLemmatizer;

/**
 * @author XMobile
 *
 */
public class QueryTermExplorer {
	
	private String queryID;
	private String linkToText;
	
	private List<Term> terms = new ArrayList<Term>();

	private String textLower;
	private String title;
	
	/*
	 * 1 line = 1 text in the initial dump.
	 */
	public QueryTermExplorer(String line){
		readAndInitializeQuery( line);
	}
	
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
				
				
				// special case: a typo in the tag
				if(topic.equals("cance")){
					term.setOriginalTerm("cancer");
				}
				
				
				InitialRelationsManager.getTermsOverall().put(topic, term.getLemma());
				InitialRelationsManager.getTerms().add(term);
				// Here a link between a query and a term is provided (contains duplicates)
				Writer.appendLineToFile(this.getQueryID() + "\t" + topic + "\t" + term.getLemma(), "termsOverall.txt");
			}
			
		}
	}
	
	
	public void extractRelations( ) throws IOException, ParseException{
		
	}
	
	
	public String getQueryID() {
		return queryID;
	}
	public void setQueryID(String queryID) {
		this.queryID = queryID;
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
