package linguistic_processing;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.simple.Sentence;

public class StanfordLemmatizer {
	
	protected StanfordCoreNLP pipeline;
	   

	    public StanfordLemmatizer() {
	        /* Create StanfordCoreNLP object properties, with POS tagging
	        / (required for lemmatization), and lemmatization
	        Properties props;
	        props = new Properties();
	        props.put("annotators", "tokenize, ssplit, pos, lemma");*/

	        /*
	         * This is a pipeline that takes in a string and returns various analyzed linguistic forms. 
	         * The String is tokenized via a tokenizer (such as PTBTokenizerAnnotator), 
	         * and then other sequence model style annotation can be used to add things like lemmas, 
	         * POS tags, and named entities. These are returned as a list of CoreLabels. 
	         * Other analysis components build and store parse trees, dependency graphs, etc. 
	         * 
	         * This class is designed to apply multiple Annotators to an Annotation. 
	         * The idea is that you first build up the pipeline by adding Annotators, 
	         * and then you take the objects you wish to annotate and pass them in and 
	         * get in return a fully annotated object.
	         * 
	         *  StanfordCoreNLP loads a lot of models, so you probably
	         *  only want to do this once per execution
	         */
	        //this.pipeline = new StanfordCoreNLP(props);
	    }

	    public String lemmatize(String word) {
	       
	        String tokenLemma = new Sentence(word).lemma(0);
			return tokenLemma;
	        
	    }
	    
		/**
		 * @param args
		 */
		public static void main(String[] args) {
			//Test
			StanfordLemmatizer lemm = new StanfordLemmatizer();
			//String lemma = lemm.lemmatize("chronic");
			String lemma = lemm.lemmatize("deli meats");
			System.out.println(lemma);

		}

}
