package relations_identification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private Set<String> catAndMesh = new HashSet<String>();
	
	private StanfordLemmatizer lemm = new StanfordLemmatizer();
	
	public Term(String term) {
		originalTerm = term;
		lemma = lemm.lemmatize(term);
	}
	
	
	public static void main(String[] args) {
		Set<Term> terms = new HashSet<Term>();
		Term term1 = new Term("cardiovascular disease");
		Term term2 = new Term("cardiovascular disease");
		terms.add(term1);
		terms.add(term2);
		Set<String> set = new HashSet<String>();
		set.add("var1");
		set.add("var2");
		
		Set<String> setToAdd = new HashSet<String>();
		set.add("la1");
		set.add("la2");
		term1.setCatAndMesh(set);
		for(Term t: terms){
			t.getCatAndMesh().addAll(setToAdd);
			System.out.println(t.getCatAndMesh().toString());
		}
		
	}
	
	@Override
	public String toString(){
		String term = this.getOriginalTerm();
		return term;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((catAndMesh == null) ? 0 : catAndMesh.hashCode());
		result = prime * result + ((lemma == null) ? 0 : lemma.hashCode());
		result = prime * result + ((originalTerm == null) ? 0 : originalTerm.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Term other = (Term) obj;
		if (catAndMesh == null) {
			if (other.catAndMesh != null)
				return false;
		} else if (!catAndMesh.equals(other.catAndMesh))
			return false;
		if (lemma == null) {
			if (other.lemma != null)
				return false;
		} else if (!lemma.equals(other.lemma))
			return false;
		if (originalTerm == null) {
			if (other.originalTerm != null)
				return false;
		} else if (!originalTerm.equals(other.originalTerm))
			return false;
		return true;
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

	public Set<String> getCatAndMesh() {
		return catAndMesh;
	}

	public void setCatAndMesh(Set<String> catvariations) {
		this.catAndMesh = catvariations;
	}

	public StanfordLemmatizer getLemm() {
		return lemm;
	}

	public void setLemm(StanfordLemmatizer lemm) {
		this.lemm = lemm;
	}

}
