package testing;

import java.util.List;

import io.Reader;

public class EvaluationSuitability {
	public int termsOverall = 0;
	public int used = 0;
	public int unused = 0;
	public int termsInEvaluationSource = 0;
	public int usedInEvaluation = 0;
	public int unusedInEvaluation = 0;
	
	public EvaluationSuitability() {
		// TODO Auto-generated constructor stub
	}
	
	public void readLemmatizedTerms(){
		List<String> terms = Reader.readLinesList("termsOverall.txt");
		
		
	}
	
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
