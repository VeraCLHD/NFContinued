package relations_identification;

import java.util.ArrayList;
import java.util.List;

import linguistic_processing.StanfordLemmatizer;

/**
 * A class that governs a single term.
 * It contains all of its morphological variations, synonym Mesh Terms if available and its lemma.
 * @author Vera
 *
 */
public class Term {
	
	private String originalTerm;
	private String lemma;
	private List<String> catvariations = new ArrayList<String>();
	
	private StanfordLemmatizer lemm = new StanfordLemmatizer();
	
	public Term(String term) {
		originalTerm = term;
		lemma = lemm.lemmatize(term);
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String getOriginalTerm() {
		return originalTerm;
	}

	public void setOriginalTerm(String originalTerm) {
		this.originalTerm = originalTerm;
	}

	public String getLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public List<String> getCatvariations() {
		return catvariations;
	}

	public void setCatvariations(List<String> catvariations) {
		this.catvariations = catvariations;
	}

	public StanfordLemmatizer getLemm() {
		return lemm;
	}

	public void setLemm(StanfordLemmatizer lemm) {
		this.lemm = lemm;
	}

}
