/**
 * 
 */
package processing.corpus_division;

import io.Writer;

/**
 * Divides the corpus into train, test, dev by excluding mutual documents from each set.
 * Thus, the same document would never be in two of the sets at the same time.
 * Caution: this leads to "loosing" queries.
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class CorpusDividerExcludingMutualDocuments extends CorpusDivider {
	
	
	public boolean docIsAllowedInTrainSet(String doc_id) {
		boolean is_in_dev = this.getDevDocIds().contains(doc_id);
		boolean is_in_test = this.getTestDocIds().contains(doc_id);
		return (!(is_in_dev||is_in_test));
	}
	
	public boolean docIsAllowedInDevSet(String doc_id) {
		boolean is_in_train = this.getTrainDocIds().contains(doc_id);
		boolean is_in_test = this.getTestDocIds().contains(doc_id);
		return (!(is_in_train||is_in_test));
	}
	
	public boolean docIsAllowedInTestSet(String doc_id) {
		boolean is_in_train = this.getTrainDocIds().contains(doc_id);
		boolean is_in_dev = this.getDevDocIds().contains(doc_id);
		return (!(is_in_train||is_in_dev));
	}
	
	/**
	 * 
	 */
	public CorpusDividerExcludingMutualDocuments() {
		// TODO Auto-generated constructor stub
	}


	@Override
	public void writeRelsEntry(String entry, String doc_id, String output_path) {
		// train set
		if (this.getIndex_for_partition() > 2) {
			if (this.docIsAllowedInTrainSet(doc_id)) {
				this.setCurrentQueryHasQrelsEntries(true);
				this.getTrainDocIds().add(doc_id);
				Writer.appendLineToFile(entry, output_path + "train.qrel");
			}
		// dev set
		} if (this.getIndex_for_partition() == 1) {
			if (this.docIsAllowedInDevSet(doc_id)) {
				this.setCurrentQueryHasQrelsEntries(true);
				this.getDevDocIds().add(doc_id);
				Writer.appendLineToFile(entry, output_path + "dev.qrel");
			}
		// test set
		} if (this.getIndex_for_partition() == 2) {
			if (this.docIsAllowedInTestSet(doc_id)) {
				this.setCurrentQueryHasQrelsEntries(true);
				this.getTestDocIds().add(doc_id);
				Writer.appendLineToFile(entry, output_path + "test.qrel");
			}
		}
	}





}
