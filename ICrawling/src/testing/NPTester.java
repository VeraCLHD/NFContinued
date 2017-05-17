package testing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;

import edu.stanford.nlp.simple.Sentence;

public class NPTester {
	
	public static boolean testNPAtBeginning(String candidate){
		boolean result = false;
		String pos0 = null;
		if(!candidate.isEmpty() && !candidate.matches("\\s+") && !candidate.equals(" ") && candidate !=null){
			Sentence sent = new Sentence(candidate);
			// pos tags penn tree bank
		    //https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
		   List<String> pos =  sent.posTags();
		    if(!pos.isEmpty()){
		    	// if the candidate starts with a verb, then it is a verbal phrase
		    	result = pos.get(0).matches("NN|NNS|NNP|NNPS"); //|| pos.get(pos.size()-1).matches("NN|NNS|NNP|NNPS");
		    	pos0 = pos.get(0);
			}
		}
		if(result == true){
			System.out.println(candidate + "POS " + pos0);
		}
		return result;
	}
	
	
	public static void main(String[] args) {
		Set<String> set = new HashSet<String>();
		set.add("c. diff");
		set.add("la la la");
		String candidate = "heart disease parkinsonism if sh c. diff.";
		int index = StringUtils.indexOfAny(candidate, set.toArray(new String[set.size()]));
	    if(index !=-1){
	    	System.out.println("doesn't contain others");
		
	}
	}

}
