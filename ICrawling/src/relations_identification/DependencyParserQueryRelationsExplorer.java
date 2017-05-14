package relations_identification;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.parser.nndep.demo.DependencyParserDemo;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.logging.Redwood;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements the initial extraction of connections with the help of the Stanford
 * dependency parser (in order to extract a proper phrase between two words).
 * https://nlp.stanford.edu/software/nndep.shtml
 * https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/parser/nndep/demo/DependencyParserDemo.java
 * https://nlp.stanford.edu/nlp/javadoc/javanlp-3.5.0/edu/stanford/nlp/parser/nndep/DependencyParser.html
 * The tested one here is the one with the universal dependencies:
 * edu/stanford/nlp/models/parser/nndep/english_UD.gz (default, English, Universal Dependencies)
 * http://universaldependencies.org/
 * @author Vera
 *
 */
public class DependencyParserQueryRelationsExplorer extends QueryRelationsExplorer {

	public DependencyParserQueryRelationsExplorer(String line) {
		readAndInitializeQuery(line);
	}
	
	public void extractRelations() {
		
	}
	
	  private static Redwood.RedwoodChannels log = Redwood.channels(DependencyParserDemo.class);

	  public static void main(String[] args) {
	    String modelPath = DependencyParser.DEFAULT_MODEL;
	    String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

	    for (int argIndex = 0; argIndex < args.length; ) {
	      switch (args[argIndex]) {
	        case "-tagger":
	          taggerPath = args[argIndex + 1];
	          argIndex += 2;
	          break;
	        case "-model":
	          modelPath = args[argIndex + 1];
	          argIndex += 2;
	          break;
	        default:
	          throw new RuntimeException("Unknown argument " + args[argIndex]);
	      }
	    }

	    String text = "Obesity is fruits.";
	    Map<String, String> test_terms = new HashMap<String, String>();
	    test_terms.put("exercise", "exercise");
	    test_terms.put("fruits", "fruit");
	    test_terms.put("vegetables", "vegetable");
	    	
	    
	    MaxentTagger tagger = new MaxentTagger(taggerPath);
	    DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

	    DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
	    for (List<HasWord> sentence : tokenizer) {
	      List<TaggedWord> tagged = tagger.tagSentence(sentence);
	      GrammaticalStructure gs = parser.predict(tagged);
	      // The grammatical structure has a toString()-method.
	    

	      for(TypedDependency typed: gs.allTypedDependencies()){
	    	  if(typed.toString().contains("fruits") || typed.toString().contains("obesity")){
	    		  System.out.println(typed.reln().toString());
		    	  System.out.println("DEP: " + typed.dep());
		    	  System.out.println("GOV " + typed.gov());
	    	  }
	    	  
	      }
	      
	      /*For some reason NULL
	       * SemanticGraph graph = new SemanticGraph(gs.typedDependenciesEnhanced());
			CoreLabel label = new CoreLabel();
			label.setWord("exercise");
			
			String term2 = "fruits";
			CoreLabel label2 = new CoreLabel();
			label2.setWord(term2);
			IndexedWord i = new IndexedWord(label);
			IndexedWord i2 = new IndexedWord(label);
			System.out.println(graph.getShortestUndirectedPathEdges(i, i2));*/
	      
	      /*List<TypedDependency> tempPath = new ArrayList<TypedDependency>();
	      for(String term1: test_terms.keySet()){
	    	  getPathOfTerm(test_terms, gs, term1, tempPath);
	      }*/
	      
	      gs.toString();
	      System.out.println(gs);
	      // Print typed dependencies
	      //log.info(gs);
	    }
	}

	private static void getPathOfTerm(Map<String, String> test_terms, GrammaticalStructure gs, String term1, List<TypedDependency> tempPath) {
		
		
		for(TypedDependency typed: gs.allTypedDependencies()){
			
			  //which word modifies the term in question?
			  if(typed.dep().toString().equals(term1)){
				 
				  tempPath.add(typed);
				  getPathOfTerm(test_terms, gs, typed.dep().toString(), tempPath);
				  
			  } else if(typed.dep().lemma().equals(term1)){
				  //which word modifies the word from if
				  tempPath.add(typed);
				  getPathOfTerm(test_terms, gs, typed.gov().toString(), tempPath);
				  
			  }
		  }
	}

}
