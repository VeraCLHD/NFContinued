package linguistic_processing;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.FSDirectory;

import io.Reader;

/** Simple command-line based search demo. */
public class LuceneSearcher {

  public LuceneSearcher() {}

 /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
	  LuceneSearcher ls = new LuceneSearcher();
	  Set<String> set = ls.doSearch("\"" + "uterine-stimulating" +"\"" + "AND" + "\"" + "may have" +"\"" );
	  for(String path: set){
		  String str = Reader.readContentOfFile(path);
		  System.out.println(path);
	  }
  }
  
  public Set<String> doSearch(String queryString) throws IOException, ParseException{
	  
	  String index = "IndexDirectory";
	    String field = "contents";
	   String queries = null;

	    boolean raw = true;
	    int hitsPerPage = 1;
	   
	   IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
	   IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer();

	   BufferedReader in = null;
	    if (queries != null) {
	      in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
	   } else {
	      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
	   }
	    
	   
	   QueryParser parser = new QueryParser(field, analyzer);
	 

	      String line = queryString != null ? queryString : in.readLine();
	      if(line !=null){
	    	  line = line.trim();
	      }
	      // escapes all the characters that have to be escaped
	      Query query = parser.parse(QueryParser.escape(line));
	  

	     //System.out.println("Searching for: " + query.toString(field));
	           
	      Set<String> paths = new HashSet<String>();
		  
		  TotalHitCountCollector collector = new TotalHitCountCollector();
		  searcher.search(query, collector);
		  TopDocs topDocs = searcher.search(query, Math.max(1, collector.getTotalHits()));
	    // Collect enough docs to show 5 pages
	    //TopDocs results = searcher.search(query);
	    //ScoreDoc[] hits = results.scoreDocs;
	    
	    //int numTotalHits = results.totalHits;
	    if(topDocs.totalHits> 0 ){
	  	  ScoreDoc[] docs = topDocs.scoreDocs;
	      for (int i = 0; i < topDocs.totalHits; i++) {

	        Document doc = searcher.doc(docs[i].doc);
	        String path = doc.get("path");
	        if (path != null) {
	        	
	     	 
	          paths.add(path);
	        } else {
	          System.out.println((i+1) + ". " + "No path for this document");
	         }
	                   
	       }
	    }
	
	      //res = doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);

	  
	    reader.close();
	    
	    
		
		return paths;
  }
  
 
}
