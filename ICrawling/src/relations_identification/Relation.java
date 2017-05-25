/**
 * 
 */
package relations_identification;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;

/**
 * @author Vera
 *
 */
public class Relation {
	// the term1 like in text (a variation of the original term)
	private String arg1;
	// the term2 like in text (a variation of the original term)
	private String arg2;

	private String relationText;
	private String typeOfRelation;
	private List<String> posTags = new ArrayList<String>();
	private String queryId;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arg1 == null) ? 0 : arg1.hashCode());
		result = prime * result + ((arg2 == null) ? 0 : arg2.hashCode());
		result = prime * result + ((relationText == null) ? 0 : relationText.hashCode());
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
		Relation other = (Relation) obj;
		if (arg1 == null) {
			if (other.arg1 != null)
				return false;
		} else if (!arg1.equals(other.arg1))
			return false;
		if (arg2 == null) {
			if (other.arg2 != null)
				return false;
		} else if (!arg2.equals(other.arg2))
			return false;
		if (relationText == null) {
			if (other.relationText != null)
				return false;
		} else if (!relationText.equals(other.relationText))
			return false;
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("123-".matches("\\d+"));

	}
	
	@Override
	public String toString(){
		
		String relation =this.getQueryId() + "\t" + this.getArg1() + "\t" + this.getArg2() + "\t" + this.getRel() + "\t";
		if(this.getTypeOfRelation() !=null){
			relation = relation + this.getTypeOfRelation() +"\t";
		} if(this.getPosTags() !=null){
			relation = relation + this.getPosTags().toString();
		}
		return relation;
	}
	

	
	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	public String getArg2() {
		return arg2;
	}

	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}

	public String getRel() {
		return relationText;
	}

	public void setRel(String rel) {
		this.relationText = rel;
	}


	public String getTypeOfRelation() {
		return typeOfRelation;
	}

	public void setTypeOfRelation(String typeOfRelation) {
		this.typeOfRelation = typeOfRelation;
	}

	public List<String> getPosTags() {
		return posTags;
	}

	public void setPosTags(List<String> posTags) {
		this.posTags = posTags;
	}

	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
}
