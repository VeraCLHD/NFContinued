/**
 * 
 */
package processing.corpus_division;

import io.Writer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import processing.ProcessingProperties;

/**
 * Divides the corpus into training, test, dev sets with the following ratio:
 * 
 * 80%: training
 * 10%: test
 * 10%: development
 * 
 * In this division variant, the same document could be in more than one of the sets at the same time.
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class SimpleQueryCorpusDivider extends CorpusDivider {

	/**
	 * 
	 */
	@Override
	public void writeRelsEntry(String entry, String doc_id, String output_path) {
		// train set
		if (this.getIndex_for_partition() > 2) {
			this.setCurrentQueryHasQrelsEntries(true);
			this.getTrainDocIds().add(doc_id);
			Writer.appendLineToFile(entry, output_path + "train.qrel");
			
		// dev set
		} if (this.getIndex_for_partition() == 1) {
			this.setCurrentQueryHasQrelsEntries(true);
			this.getDevDocIds().add(doc_id);
			Writer.appendLineToFile(entry, output_path + "dev.qrel");
			
		// test set
		} if (this.getIndex_for_partition() == 2) {
			this.setCurrentQueryHasQrelsEntries(true);
			this.getTestDocIds().add(doc_id);
			Writer.appendLineToFile(entry, output_path + "test.qrel");
		}
	}
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	



}
