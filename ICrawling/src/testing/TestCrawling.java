package testing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import crawling_docs.DocPageCrawler;
import crawling_docs.DocProperties;
import crawling_docs.ExpectedConnectionException;

public class TestCrawling {

	public TestCrawling() {
		// TODO Auto-generated constructor stub
	}
	
	public static Document testConnection() throws IOException{
		
	Document doc = null;
	//past-week-april-14-21-2017/
			try {
	Response connection = Jsoup.connect("https://clipdemos.umiacs.umd.edu/cgi-bin/catvar/webCVsearch.pl?query=ooo&submit=CatVariate%21")
    .execute();
			
	doc = connection.parse();
	Elements releventElements = doc.select("td").select("tr");
	for (int i=0; i < releventElements.size(); i++){
		
		if(releventElements.get(i).text() != null && !releventElements.get(i).text().equals("") && !releventElements.get(i).text().isEmpty() && !releventElements.get(i).text().equals("CATVAR 2.0 Main") && !releventElements.get(i).text().equals("_")){
			System.out.println("ELEMENT: " + releventElements.get(i).select("b").text());
		}
		
		
	}

			} catch (java.net.SocketTimeoutException e) {}
			return doc;

	}
	public static void main(String[] args) throws IOException {
		TestCrawling.testConnection();
    }



}
