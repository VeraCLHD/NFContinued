/**
 * 
 */
package relations_identification;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vera
 *
 */
public class Relation {
	// the term1 like in text (a variation of the original term)
	private String arg1;
	// the term2 like in text (a variation of the original term)
	private String arg2;

	// the original term - original term like in list of terms
	private String arg1Origin;
	// the term2 like in text - original term like in list of terms
	private String arg2Origin;
	private String relationText;
	private String typeOfRelation;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public String toString(){
		
		String relation = this.getArg1() + "\t" + this.getArg1Origin()  + "\t" + this.getArg2() + "\t" + this.getArg2Origin() + "\t" + this.getRel() + "\t";
		if(this.getTypeOfRelation() !=null){
			relation = relation + this.getTypeOfRelation();
		}
		return relation;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arg1Origin == null) ? 0 : arg1Origin.hashCode());
		result = prime * result + ((arg2Origin == null) ? 0 : arg2Origin.hashCode());
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
		if (arg1Origin == null) {
			if (other.arg1Origin != null)
				return false;
		} else if (!arg1Origin.equals(other.arg1Origin))
			return false;
		if (arg2Origin == null) {
			if (other.arg2Origin != null)
				return false;
		} else if (!arg2Origin.equals(other.arg2Origin))
			return false;
		if (relationText == null) {
			if (other.relationText != null)
				return false;
		} else if (!relationText.equals(other.relationText))
			return false;
		return true;
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
	public String getArg1Origin() {
		return arg1Origin;
	}

	public void setArg1Origin(String arg1Origin) {
		this.arg1Origin = arg1Origin;
	}

	public String getArg2Origin() {
		return arg2Origin;
	}

	public void setArg2Origin(String arg2Origin) {
		this.arg2Origin = arg2Origin;
	}

	public String getTypeOfRelation() {
		return typeOfRelation;
	}

	public void setTypeOfRelation(String typeOfRelation) {
		this.typeOfRelation = typeOfRelation;
	}
}
