package relations_identification;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.parser.nndep.demo.DependencyParserDemo;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.logging.Redwood;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;

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
	
	public DependencyParserQueryRelationsExplorer() {
		
	}
	
	public void extractRelations() {
		
	}
	
	  private static Redwood.RedwoodChannels log = Redwood.channels(DependencyParserDemo.class);

	  public static void main(String[] args) {
	    /*//Dependency parse
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
	      
	      For some reason NULL
	       SemanticGraph graph = new SemanticGraph(gs.typedDependenciesEnhanced());
			CoreLabel label = new CoreLabel();
			label.setWord("exercise");
			
			String term2 = "fruits";
			CoreLabel label2 = new CoreLabel();
			label2.setWord(term2);
			IndexedWord i = new IndexedWord(label);
			IndexedWord i2 = new IndexedWord(label);
			System.out.println(graph.getShortestUndirectedPathEdges(i, i2));
	      
	      List<TypedDependency> tempPath = new ArrayList<TypedDependency>();
	      for(String term1: test_terms.keySet()){
	    	  getPathOfTerm(test_terms, gs, term1, tempPath);
	      }
	      
	      gs.toString();
	      System.out.println(gs);

	    }*/
		  
		  DependencyParserQueryRelationsExplorer de = new DependencyParserQueryRelationsExplorer();
		 de.annotateSyntax("Obesity starts in childhood heart.");
	}
	  
	public void annotateSyntax(String text){
		StanfordCoreNLP pipeline = new StanfordCoreNLP(
				PropertiesUtils.asProperties(
					"annotators", "tokenize,ssplit,pos,lemma,parse,natlog",
					"ssplit.isOneSentence", "true",
					"parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz",
					"tokenize.language", "en"));

			// read some text in the text variable
			
			Annotation document = new Annotation(text);

			// run all Annotators on this text
			pipeline.annotate(document);
		
		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for(CoreMap sentence: sentences) {
		  // traversing the words in the current sentence
		  // a CoreLabel is a CoreMap with additional token-specific methods
		 
		  // this is the parse tree of the current sentence
		  Tree tree = sentence.get(TreeAnnotation.class);
		  //(ROOT (S (NP (NN Obesity)) (VP (VBZ is) (NP (NNS fruits))) (. .)))
		  System.out.println("parse tree:\n" + tree);
		  //[(0,0), (2,2), (1,2), (0,3)]
		  System.out.println(tree.constituents());
	
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
