package testing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.simple.Sentence;
import linguistic_processing.SentenceSplitter;

public class TestingRegex {

	public TestingRegex() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		/*/ The basis but doesn't extract everything
		Pattern p = Pattern.compile(("(\\bone\\b)(.*?)(\\bla\\b)"));
		Matcher m = p.matcher("one la la two la one bla two.");
		
		while (m.find()) {
	         matches.add(m.group(1));
	        
	         System.out.println(matches);
	      }*/
		
		/*String regexString = Pattern.quote("one") + "(.*?)" + Pattern.quote("two");
		
		Pattern pattern = Pattern.compile(regexString);
		// Contains the patterns as well -> don't want that
		Matcher matcher = pattern.matcher("one la la  la one bla two.");
		List<String> matches = new ArrayList<String>();
		while (matcher.find()) {
		  String textInBetween = matcher.group(1); // Since (.*?) is capturing group 1
		  // You can insert match into a List/Collection here
		  matches.add(textInBetween);
		}
		
		System.out.println(matches);*/
		
		/*String str = "one la la la one bla two.";
		Matcher m = Pattern.compile(
		                            Pattern.quote("one")
		                            + "(.*?)"
		                            + Pattern.quote("two")
		                   ).matcher(str);
		while(m.find()){
		    String match = m.group(1);
		    System.out.println(">"+match+"<");
		    //here you insert 'match' into the list
		}*/
		
		SentenceSplitter splitter = new SentenceSplitter("Only 3% of Americans at the turn of the 21st century had the following four healthy lifestyle characteristics: not smoking, not overweight, five daily servings of fruits and vegetables, and exercising a half hour a day at least five days a week.");
		for(Sentence s: splitter.getSentences()){
			System.out.println(s.lemmas().size());
			System.out.println(s.words().size());
		
		}

	}

}
