package testing;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import crawling_docs.DocPageCrawler;
import crawling_docs.DocProperties;
import crawling_docs.ExpectedConnectionException;

public class TestCrawlingLogIn {

	public TestCrawlingLogIn() {
		// TODO Auto-generated constructor stub
	}
	
	public Document testAuthorityNutrition() throws IOException{
		
	Document doc = null;
		
			try {
	Response connection = Jsoup.connect("https://authoritynutrition.com/research/past-week-april-14-21-2017/")
    .data("loginField", "vera.boteva@yahoo.de")
    .data("passwordField", "vera2561")
    .method(Method.POST)
    .execute();
			
	doc = connection.parse();
	
	Elements content = doc.select("div[id=content");
	System.out.println(content);
	
			} catch (java.net.SocketTimeoutException e) {}
			return doc;

	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
