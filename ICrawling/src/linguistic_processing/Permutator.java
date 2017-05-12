package linguistic_processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.simple.Sentence;

public class Permutator {

    // print n! permutation of the characters of the string s (in order)
    public  static void perm1(String s) { perm1("", s); }
    private static void perm1(String prefix, String s) {
        int n = s.length();
        if (n == 0) System.out.println(prefix);
        else {
            for (int i = 0; i < n; i++)
               perm1(prefix + s.charAt(i), s.substring(0, i) + s.substring(i+1, n));
        }

    }

    // print n! permutation of the elements of array a (not in order)
    public static void perm2(String s) {
        int n = s.length();
        char[] a = new char[n];
        for (int i = 0; i < n; i++)
            a[i] = s.charAt(i);
        perm2(a, n);
    }

    private static void perm2(char[] a, int n) {
        if (n == 1) {
        	System.out.println(a);
            return;
        }
        for (int i = 0; i < n; i++) {
            swap(a, i, n-1);
            perm2(a, n-1);
            swap(a, i, n-1);
        }
    }  

    // swap the characters at indices i and j
    private static void swap(char[] a, int i, int j) {
        char c = a[i];
        a[i] = a[j];
        a[j] = c;
    }



    public static void main(String[] args) {
    	String sentence = "Obesity is up, exercise is down, and the number of people eating just five servings of fruits and veggies a day dropped like a rock";
    	SentenceSplitter splitter = new SentenceSplitter(sentence);
    	
    
    	List<String> tokens = splitter.getSentences().get(0).words();
		List<String> tokenCombinations = new ArrayList<String>();
		
		// termSize =1 is the size of terms for multiwords/single if = 1
		// termSize -> max. size of a possible term is 8. This is currently the longest term
		//i = number of terms of certain size
		for(int termSize=1;termSize <= Math.min(tokens.size(),8); termSize++){
			for(int i=1; i<=tokens.size()-termSize+1;i++)
			{
				List<String> tokenCombination = tokens.subList(i - 1, i - 1 + termSize);
				// punctuation is included in the candidates, shouldn't be -> pattern
				String tokenString = String.join(" ", tokenCombination);
				tokenCombinations.add(tokenString);
				
			}
		
		
		}
		
		System.out.println(tokenCombinations);
    }
}
