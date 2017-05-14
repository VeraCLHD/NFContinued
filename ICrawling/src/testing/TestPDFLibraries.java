package testing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL; 

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import crawling_docs.DocCrawler;
import crawling_docs.DocPageCrawler;
import crawling_docs.Document;

/**
 * Only for trying out the libraries that deal with PDFs.
 * 
 * @author Vera Boteva, Demian Gholipour
 */

public class TestPDFLibraries {
	public static String crawlTextFromPDFLink(DocPageCrawler doc_page_crawler) {
		String link = doc_page_crawler.getLinkToSite();
		String text = "";
		PDDocument pdf;
		
		try {
			//File file = new File("C:\\Users\\Vera\\Downloads\\nejmp1110421.pdf");
			//pdf = PDDocument.load(file);
			pdf = PDDocument.load(new URL(link));
			//pdf.save(new File("C:\\Users\\Vera\\Downloads"));
			System.out.println(pdf.getNumberOfPages());
			
			PDFTextStripper pdf_stripper = new PDFTextStripper();
		
			text += pdf_stripper.getText(pdf);
				
			System.out.println(text);
			pdf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
		return text;
	}
	
	public static void testHttpClient(){
		String text = "";
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://pubs.acs.org/doi/pdf/10.1021/jf800974z");
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
			    //long len = entity.getContentLength();
			    InputStream inputStream = entity.getContent();
			    // write the file to whether you want it.
			  
			   File file = new File("test_pdf");
			   TestPDFLibraries.copyInputStreamToFile(inputStream, file);
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void copyInputStreamToFile( InputStream in, File file ) {
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
	        e.printStackTrace();
	    }
	}

}
