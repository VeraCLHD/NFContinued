package crawling_docs;

import io.Writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A class that is responsible for getting the content of a direct PDF link.
 * 
 * @author Vera Boteva, Demian Gholipour
 */

public class PDFCrawler {

	/**
	 * Sets a connection to a PDF link and returns the content of the PDF. Decrypts the pdf page if necessary.
	 * Content type errors are caugth.
	 */
	public static String crawlTextFromPDFLink(DocPageCrawler doc_page_crawler){
		
		String link = doc_page_crawler.getLinkToSite();
		String text = "";
		PDDocument pdf;
		
		try {
			pdf = PDDocument.load(new URL(link));
			if (pdf.isEncrypted()) {
		        try {
		        	pdf.decrypt("");
		        	pdf.setAllSecurityToBeRemoved(true);
		        }
		        catch (Exception e) {
		            System.err.println("The document is encrypted, and we can't decrypt it.");
		        }
		    }
			PDFTextStripper pdf_stripper = new PDFTextStripper();		
			text += pdf_stripper.getText(pdf);
			pdf.close();
		} catch (IOException e) {
			 System.err.println("IOException");
		}	
		return text;
	}
	
	/**
	 * reads the stream of a pdf locally.
	 * @return String the stream of a pdf
	 */
	public static String readStreamAsPDF() {
		
		String text = "";
		File file = new File(DocProperties.TEMP_PDF_PATH);
		PDDocument pdf;
		
		try {
			pdf = PDDocument.load(file);
			PDFTextStripper pdf_stripper = new PDFTextStripper();		
			text += pdf_stripper.getText(pdf);
			// normalize whitespaces
			text = text.replaceAll("\\s+", " ");
			pdf.close();
		} catch (IOException e) {
			System.err.println("IOException");
		}	
		return text;
	}
	/**
	 * Saves the inputStream of a pdf in a temporary file.
	 * Used to save the content of a pdf locally if for some reason it is not possible to
	 * parse the pdf content online.
	 * @param doc_page_crawler a lower level object always created for a single link.
	 */
	public static void getInputStreamFromLink(DocPageCrawler doc_page_crawler){
		String docLink = doc_page_crawler.getLinkToSite();
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(docLink);
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
			   InputStream inputStream = entity.getContent();
			   File file = new File(DocProperties.TEMP_PDF_PATH);
			   PDFCrawler.writeInputStreamToFile(inputStream, file);
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			System.err.println("ClientProtocolException");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("IOException");
		}
		
	}
	/**
	 * Writes the temporary pdf inputstream to a file.
	 * @param in Inputstream to be written
	 * @param file temporary stream file
	 */
	public static void writeInputStreamToFile( InputStream in, File file ) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	       System.err.println("Exception");
	    }
	}
}