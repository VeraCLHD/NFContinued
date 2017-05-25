package linguistic_processing;

import java.io.File;

import org.unix4j.Unix4j;
import org.unix4j.unix.Grep;
import org.unix4j.unix.Wc;

public class UnixTools {
	
	  public static void main(String[] args) {
		  //File f = new File("denied/tuples_of_terms.txt");
		 //System.out.println(Unix4j.grep("degenerative diseasel_o_v_ehigh-fiber food", f).wc().);
		  File f = new File("denied/test.txt");
			String str = Unix4j.grep(Grep.Options.c, "la", f).toStringResult();
				Integer.parseInt(String.valueOf(str.charAt(0)));	
	  }
	
}
