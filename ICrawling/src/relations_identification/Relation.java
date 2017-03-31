/**
 * 
 */
package relations_identification;

/**
 * @author Vera
 *
 */
public class Relation {
	private String arg1;
	private String arg2;
	private String rel;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public String toString(){
		String relation = this.getArg1() + "\t" + this.getRel() + "\t" + this.getArg1();
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
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

}
