package linguistic_processing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import io.Reader;

public class LargeFileReader {

	public static void main(String[] args) {
		String str = Reader.readContentOfFile("denied/tuples_of_terms.txt");

		String search = "degenerative diseasel_o_v_enasal lavages";
		
		
		try {
			byte[] array = Files.readAllBytes(new File("denied/tuples_of_terms.txt").toPath());
			Byte b = Byte.valueOf(search);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
