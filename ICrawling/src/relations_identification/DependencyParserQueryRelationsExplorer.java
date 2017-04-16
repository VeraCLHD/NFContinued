package relations_identification;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.parser.nndep.demo.DependencyParserDemo;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.util.logging.Redwood;

import java.io.StringReader;
import java.util.List;

/**
 * This class implements the initial extraction of connections with the help of the Stanford
 * dependency parser (in order to extract a proper phrase between two words).
 * https://nlp.stanford.edu/software/nndep.shtml
 * https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/parser/nndep/demo/DependencyParserDemo.java
 * https://nlp.stanford.edu/nlp/javadoc/javanlp-3.5.0/edu/stanford/nlp/parser/nndep/DependencyParser.html
 * The tested one here should be with the universal dependencies:
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

	    String text = "Obesity is up, exercise is down, and the number of people eating just five servings of fruits and veggies a day dropped like a rock.";

	    MaxentTagger tagger = new MaxentTagger(taggerPath);
	    DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

	    DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
	    for (List<HasWord> sentence : tokenizer) {
	      List<TaggedWord> tagged = tagger.tagSentence(sentence);
	      GrammaticalStructure gs = parser.predict(tagged);
	      // The grammatical structure has a toString()-method.
	      gs.toString();
	      System.out.println(gs);
	      // Print typed dependencies
	      //log.info(gs);
	    }
	}

}
