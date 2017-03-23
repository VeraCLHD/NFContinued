/**
 * 
 */
package processing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import crawling_docs.DocProperties;
import crawling_queries.Properties;

/**
 * @author Vera
 *
 */
public abstract class DocLinksFilter{
	
	
	/**
	 * Returns false, if a string doesn't match a pattern; true otherwise
	 * @param string the string to be checked
	 * @param pattern the pattern to be matched
	 * @return true, if string matches the pattern; false: else.
	 */
	public static boolean matchesPattern(String string, Pattern pattern) {
		Matcher matcher = pattern.matcher(string);
		return matcher.matches();
	}
	
	/**
	 * reads the initial nf_dump and rewrites it after filtering the non-relevant document links from it. 
	 */
	public void readAndRewriteNFDump() {
		io.Editor.deleteFile(DocProperties.FILTERED_NFDUMP_PATH);
		try {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Properties.NFDUMP_PATH), "UTF-8"));
			while(br.ready()){
				String nextLine = br.readLine();
				
				this.writeFilteredDumpLine(nextLine);
			}	
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public abstract void writeFilteredDumpLine(String dumpline);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
