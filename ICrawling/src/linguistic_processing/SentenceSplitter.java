package linguistic_processing;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

import java.util.List;

public class SentenceSplitter {
	List<Sentence> sentences;
	
	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}

	public SentenceSplitter(String string) {
		Document doc = new Document(string);
		this.setSentences(doc.sentences());
	}

	public static void main(String[] args) {
		

	}

}
