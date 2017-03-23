package processing.corpus_division;

import io.Editor;
import io.Writer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.util.HashSet;
import java.util.Set;

import processing.ProcessingProperties;

/**
 * Divides the nf, doc and relevance data into train, dev, and test, sets.
 * In order to randomize the selection in an easy way, an index is used
 * that is set to 0 if it reaches 10, while going through a query or doc file.
 * Index 1 means dev set, 2 means test set, the rest goes to train set.
 * 
 * @author Vera Boteva, Demian Gholipour
 */

public abstract class CorpusDivider {

	private int index_for_partition = 1;
	private boolean currentQueryHasQrelsEntries;
	
	/**
	 * Takes a qrel file entry and writes it in the train, test or dev qrel file.
	 * Based on this, the document gets also sorted.
	 * @param entry qrel entry
	 * @param doc_id current document
	 * @param output_path directory that contains resulting output files
	 */
	public abstract void writeRelsEntry(String entry, String doc_id, String output_path);
	
	private Set<String> trainDocIds = new HashSet<String>();
	private Set<String> devDocIds = new HashSet<String>();
	private Set<String> testDocIds = new HashSet<String>();
	
	/**
	 * Writes the .docs files for train/dev/test.
	 * @param output_path directory that contains resulting output files
	 */
	
	public void writeDocsFiles(String output_path) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ProcessingProperties.DOC_TEXTS_PATH), "UTF-8"));
			// go through all documents
			while (br.ready()) {
				String line = br.readLine();
				String[] components = line.split("\t");
				String id = components[0].trim();
				// check which set the id has been assigned to; append doc entry to respective file
				if (this.getTrainDocIds().contains(id)) {
					Writer.appendLineToFile(line, output_path + "train.docs");
				} if (this.getDevDocIds().contains(id)) {
					Writer.appendLineToFile(line, output_path + "dev.docs");
				} if (this.getTestDocIds().contains(id)) {
					Writer.appendLineToFile(line, output_path + "test.docs");
				}
			}
			br.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Divides queries, docs and rels into train/dev/test sets.
	 * All files are written into the same output path.
	 * @param path_to_all_queries path to all.queries file
	 * @param output_path output_path directory that contains resulting output files
	 */
	
	public void divideCorpus(String path_to_all_queries, String output_path) {
		
		int num_failed_dev = 0;
		int num_failed_test = 0;
		int num_failed_train = 0;
		
		// delete all files that will be written by this method
		
		Editor.deleteFile(output_path + "train.queries");
		Editor.deleteFile(output_path + "dev.queries");
		Editor.deleteFile(output_path + "test.queries");
		Editor.deleteFile(output_path + "train.docs");
		Editor.deleteFile(output_path + "dev.docs");
		Editor.deleteFile(output_path + "test.docs");
		Editor.deleteFile(output_path + "train.qrel");
		Editor.deleteFile(output_path + "dev.qrel");
		Editor.deleteFile(output_path + "test.qrel");
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path_to_all_queries), "UTF-8"));
			
			// go through queries
			while (br.ready()) {
				
				// get data from query
				String query_line = br.readLine();
				String[] query_components = query_line.split("\t");
				String id = query_components[0];
				String text = query_components[1];
				String entry = id + "\t" + text;
				
				this.setCurrentQueryHasQrelsEntries(false);
				BufferedReader br_rel = new BufferedReader(new InputStreamReader(new FileInputStream(ProcessingProperties.RELEVANCE_PATH), "UTF-8"));
				
				// go through original relevance file and write entries in the train, test dev relevance files
				while (br_rel.ready()) {
					String rel_line = br_rel.readLine();
					String[] rel_line_components = rel_line.split("\t");
					String id2 = rel_line_components[0];
					String doc_id = rel_line_components[1];
					
					// skip entries that don't belong to current query
					if (!id2.equals(id)) {
						continue;
					}
					// write new relevance entry and sort the document to train, test or dev
					this.writeRelsEntry(rel_line, doc_id, output_path);
				}
				br_rel.close();
				
				// document how much queries the data sets loose
				if (!this.currentQueryHasQrelsEntries()) {
					if (this.getIndex_for_partition() == 2) {
						num_failed_test ++;
					} else if (this.getIndex_for_partition() == 1) {
						num_failed_dev ++;
					} else {
						num_failed_train ++;
					}
					continue;
				}
				
				// write query file in train, test or dev file, depending on partition index
				
				if (this.getIndex_for_partition() > 2) {
					Writer.appendLineToFile(entry, output_path + "train.queries");
				} else if(this.getIndex_for_partition() == 1) {
					Writer.appendLineToFile(entry, output_path + "dev.queries");
				} else if (this.getIndex_for_partition() == 2) {
					Writer.appendLineToFile(entry, output_path + "test.queries");
				}
				
				// increment or reset partition index
				
				if (this.getIndex_for_partition() == 10) {
					this.setIndex_for_partition(1);
				} else {
					
					this.setIndex_for_partition(this.getIndex_for_partition() +1);
				}			
			}
			br.close();
			//System.out.println(num_failed_dev);
			//System.out.println(num_failed_test);
			//System.out.println(num_failed_train);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// write train/test/dev doc files
		this.writeDocsFiles(output_path);

	}
	
	
	
	public boolean currentQueryHasQrelsEntries() {
		return currentQueryHasQrelsEntries;
	}
	
	public void setCurrentQueryHasQrelsEntries(
			boolean currentQueryHasQrelsEntries) {
		this.currentQueryHasQrelsEntries = currentQueryHasQrelsEntries;
	}
	
	public Set<String> getTrainDocIds() {
		return trainDocIds;
	}

	public void setTrainDocIds(Set<String> trainDocIds) {
		this.trainDocIds = trainDocIds;
	}

	public Set<String> getDevDocIds() {
		return devDocIds;
	}

	public void setDevDocIds(Set<String> devDocIds) {
		this.devDocIds = devDocIds;
	}

	public Set<String> getTestDocIds() {
		return testDocIds;
	}

	public void setTestDocIds(Set<String> testDocIds) {
		this.testDocIds = testDocIds;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public int getIndex_for_partition() {
		return index_for_partition;
	}

	public void setIndex_for_partition(int index_for_partition) {
		this.index_for_partition = index_for_partition;
	}

}
