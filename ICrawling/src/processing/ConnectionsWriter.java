package processing;

import io.Editor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import crawling_docs.DocProperties;
import crawling_docs.ExpectedConnectionException;
import crawling_queries.Properties;

/**
 * 
 * Writes a file that saves the connections between all queries and their linked other queries and their related (linked) queries and relevant documents by using their IDs.
 * <p>
 * Required Input Files: NF dump file (nfdump.txt), Query Link -> ID file, Doc Link -> ID file.
 * <p>
 * Process: <br>
 * - load link -> id maps from files <br>
 * - iterate over NF dump lines (queries) <br>
 * - get links of query and replace them by their IDs
 * 
 * @author Vera Boteva, Demian Gholipour
 * 
 */
public class ConnectionsWriter {
	
	private HashMap<String, String> query_identity;
	private HashMap<String, String> doc_identity;
	
	public ConnectionsWriter() {
		this.setQuery_identity(readIdentityMap(ProcessingProperties.QUERY_IDENTITY_PATH));
		this.setDoc_identity(readIdentityMap(ProcessingProperties.DOC_IDENTITY_PATH));
	}
	
	public HashMap<String, String> readIdentityMap(String path) {
		HashMap<String, String> id_map = new HashMap<String, String>();
		ArrayList<String> lines = io.Reader.readLinesList(path);
		for (String line: lines) {
			String[] elements = line.split("\t");
			String link = elements[0];
			String id = elements[1];
			id_map.put(link, id);
		}
		return id_map;
	}
	
	/**
	 * Takes one line of the NFDump and writes a corresponding
	 * line for the connection file where each link is replaced
	 * by the id.
	 */
	
	public String replaceLinksByIds(String line) {
		String id_line = "";
		String[] components = line.split("\t");
		assert components.length == 13;
		String own_id = components[0];
		String[] articlelinks = components[8].split(",");
		String[] questionlinks = components[9].split(",");
		String[] topiclinks = components[10].split(",");
		String[] videolinks = components[11].split(","); 
		String[] doclinks = components[12].split(",");
		String[][] querylink_arrays = {articlelinks, questionlinks, topiclinks, videolinks};
		List<String> querylinks = new ArrayList<String>();
		for (String[] array: querylink_arrays) {
			querylinks.addAll(Arrays.asList(array));
		}
		boolean has_querylinks = false;
		id_line += own_id + "\t";
		for (String link: querylinks) {
			if (!link.equals("-")) {
				String query_id = query_identity.get(link);
				if (query_id == null) {
					continue;
				}
				has_querylinks = true;
				id_line += query_id + ",";
			}
		}
		if (!has_querylinks) {
			id_line += "-";
		} else {
			id_line = id_line.substring(0, id_line.length() - 1);
		}
		id_line += "\t";

		String doc_part = "";
		for (String doclink: doclinks) {
			String doc_id = doc_identity.get(doclink);
			if (doc_id == null) {
				continue;
			}
			doc_part += doc_id + ",";
		}		
		if (doc_part.isEmpty()) {
			id_line += "-";
			//System.out.println("NO DOCS: " + line);
		} else {
			id_line += doc_part;
			id_line = id_line.substring(0, id_line.length() - 1);	
		}
		return id_line;
	}
	
	/**
	 * Writes the connections file.
	 */
	
	public void writeConnections() {
		
		Editor.deleteFile(ProcessingProperties.CONNECTIONS_PATH);
		File cnfile = new File(ProcessingProperties.CONNECTIONS_PATH);
		FileOutputStream fos;
		try (BufferedReader br = new BufferedReader(new FileReader(DocProperties.FILTERED_NFDUMP_PATH))) {
			fos = new FileOutputStream(cnfile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		    while (br.ready()) {
		    	String nf_dump_line = br.readLine();
		    	String id_line = this.replaceLinksByIds(nf_dump_line);
				bw.write(id_line);
				bw.newLine();
		    }
		    bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ConnectionsWriter cw = new ConnectionsWriter();
		cw.writeConnections();
	}
	
	public HashMap<String, String> getQuery_identity() {
		return query_identity;
	}

	public void setQuery_identity(HashMap<String, String> query_identity) {
		this.query_identity = query_identity;
	}

	public HashMap<String, String> getDoc_identity() {
		return doc_identity;
	}

	public void setDoc_identity(HashMap<String, String> doc_identity) {
		this.doc_identity = doc_identity;
	}
	
}
