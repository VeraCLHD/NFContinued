/**
 * 
 */
package run;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.ConnectionsWriter;
import processing.CorpusVariantsWriter;
import processing.DocDumpParser;
import processing.DocLinksFilter;
import processing.DocLinksFilterFactory;
import processing.DocLinksFilterFullTextMode;
import processing.DocumentDuplicatesFilter;
import processing.IDNormalizer;
import processing.NFDumpParser;
import processing.ProcessingProperties;
import processing.QueryFilter;
import processing.RelevanceGrader;
import processing.TextPreprocessor;
import processing.TopicLinksAdder;
import processing.corpus_division.CorpusDivider;
import processing.corpus_division.CorpusDividerExcludingMutualDocuments;
import processing.corpus_division.SimpleQueryCorpusDivider;
import io.Editor;
import io.Reader;
import crawling_docs.DelayManager;
import crawling_docs.DocCrawler;
import crawling_docs.DocCrawlerFactory;
import crawling_docs.DocCrawlerFullTextMode;
import crawling_docs.DocProperties;
import crawling_docs.ExpectedConnectionException;
import crawling_queries.NFCrawler;
import crawling_queries.Properties;

/**
 * A main class that runs all other classes:
 * 1. crawls/ updates NutritonFacts dump: only the crawling itself could be interrupted.
 * 2. crawls Document dump
 * 3. Processes the data and divides into 4 data sets.
 * For reference about the arguments, see: QCL_MEDIR_README.txt
 * 
 * @author Vera Boteva, Demian Gholipour
 */
public class createQCLIRCorpus {
	
	// the object that manages the delay depending on the domain
	private static final DelayManager delay_manager = new DelayManager();
	private static boolean testBool = false;
	private static Integer testMax = 0;
	
	
	public static void processDumps() {
		System.out.println("DELETING DOC DUPLICATES...");
		DocumentDuplicatesFilter.filterDuplicates();
		System.out.println("UNIFORM IDs...");
		IDNormalizer.replaceNFDumpIds();
		IDNormalizer.replaceDocDumpIds();
		System.out.println("PARSING NF DUMP...");
		NFDumpParser.process();
		System.out.println("PARSING DOC DUMP...");
		DocDumpParser.process();
		
		/*
		 System.out.println("CONNECTIONS...");
		ConnectionsWriter cw = new ConnectionsWriter();
		cw.writeConnections();
		System.out.println("TOPIC LINKS...");
		TopicLinksAdder.addTopicLinks();
		System.out.println("DELETING QUERIES WITHOUT DOCS...");
		QueryFilter qf = new QueryFilter(1);
		qf.filterQueries();
		System.out.println("RELEVANCE...");
		RelevanceGrader.writeRelevanceFile();
		System.out.println("PREPROCESSING DOCS TEXTS...");
		TextPreprocessor.preProcessDocs();
		System.out.println("PREPROCESSING QUERY TEXTS INCLUDING COMMENTS...");
		TextPreprocessor.preProcessQueries(ProcessingProperties.QUERY_TEXTS_PATH, ProcessingProperties.PREPROCESSED_QUERIES_PATH);
		System.out.println("WRITING QUERIES VARIANT WITHOUT COMMENTS...");
		CorpusVariantsWriter.collectFinalQueryIds();
		CorpusVariantsWriter.createVariantWC();
		System.out.println("PREPROCESSING QUERY TEXTS WITHOUT COMMENTS...");
		TextPreprocessor.preProcessQueries(ProcessingProperties.QUERIES_WITHOUT_COMMENTS_PATH, ProcessingProperties.PREPROCESSED_QUERIES_WC_PATH);*/
		
	}
	/*
	public static void runAllWithSimple() {
		String dataset_path = ProcessingProperties.DATASETS_PATH + "complete_queries_shared_docs/";
		System.out.println("SPLIT INTO TRAIN, DEV, TEST: COMPLETE QUERIES + SIMPLE DIVIDER WITH SHARED DOCUMENTS...");
		CorpusDivider divider = new SimpleQueryCorpusDivider();
		divider.divideCorpus(ProcessingProperties.QUERY_TEXTS_PATH, dataset_path);
	}
	
	public static void runWCWithSimple() {
		String dataset_path = ProcessingProperties.DATASETS_PATH + "no_comments_shared_docs/";
		System.out.println("SPLIT INTO TRAIN, DEV, TEST: NO COMMENTS + SIMPLE DIVIDER WITH SHARED DOCUMENTS...");
		CorpusDivider divider = new SimpleQueryCorpusDivider();
		divider.divideCorpus(ProcessingProperties.QUERIES_WITHOUT_COMMENTS_PATH, dataset_path);
	}
	
	public static void runAllWithDep() {
		String dataset_path = ProcessingProperties.DATASETS_PATH + "complete_queries_no_shared_docs/";
		System.out.println("SPLIT INTO TRAIN, DEV, TEST: COMPLETE QUERIES + NO SHARED DOCUMENTS...");
		CorpusDivider divider = new CorpusDividerExcludingMutualDocuments();
		divider.divideCorpus(ProcessingProperties.QUERY_TEXTS_PATH, dataset_path);
	}
	
	public static void runWCWithDep() {
		String dataset_path = ProcessingProperties.DATASETS_PATH + "no_comments_no_shared_docs/";
		System.out.println("SPLIT INTO TRAIN, DEV, TEST: NO COMMENTS + NO SHARED DOCUMENTS...");
		CorpusDivider divider = new CorpusDividerExcludingMutualDocuments();
		divider.divideCorpus(ProcessingProperties.QUERIES_WITHOUT_COMMENTS_PATH, dataset_path);
	}*/
	
	
	
	
	public static void doOnlyProcessing(){
		createQCLIRCorpus.processDumps();
		/*createQCLIRCorpus.runAllWithSimple();
		createQCLIRCorpus.runWCWithSimple();
		createQCLIRCorpus.runAllWithDep();
		createQCLIRCorpus.runWCWithDep();*/
	}
	
	public static void handleTextMode(String[] args){
			DocCrawler crawler_textmode = DocCrawlerFactory.createCrawler(args[2]);
			DocLinksFilter filter = DocLinksFilterFactory.createFilter(args[2]);
			
			if(filter != null && crawler_textmode !=null){
				if(!args[0].equalsIgnoreCase("-nf_crawled") && args[1].equalsIgnoreCase("-proceed_crawl_doc") || !args[0].equalsIgnoreCase("-nf_crawled") && args[1].equalsIgnoreCase("-doc_crawled")){
					System.err.println("Wrong argument combination. If the nf is not yet crawled, you cannot proceed with document crawling or process a dump that doesn't exist!");
					return;
				}
					
				if(args[0].equalsIgnoreCase("-crawl_nf_new")){
					System.out.println("CREATING FILE STRUCTURE FOR CRAWLING...");
					Editor.createFileStructureForProject();
					List<File> files_to_delete = new ArrayList<File>();
					Editor.deleteFile(Properties.NFDUMP_PATH);
					Editor.deleteFile(DocProperties.FILTERED_NFDUMP_PATH);
					Editor.deleteFile(Properties.PATH_TO_QUERY_FORECAST);
					File querydumps = new File(Properties.PATH_TO_QUERYDUMPS);
					File querylinks = new File(Properties.LINKS_PATH);
					files_to_delete.addAll(Arrays.asList(querydumps.listFiles()));
					files_to_delete.addAll(Arrays.asList(querylinks.listFiles()));
						
					for(File file: files_to_delete){
						Editor.deleteFile(file.getAbsolutePath());
					}
					NFCrawler.crawl();
					
					filter.readAndRewriteNFDump();
					createQCLIRCorpus.crawlAndProcessDocuments(args[1], crawler_textmode);
					
				} else if(args[0].equalsIgnoreCase("-proceed_crawl_nf")){
					crawling_queries.NFCrawler.crawl();
					
					filter.readAndRewriteNFDump();
					createQCLIRCorpus.crawlAndProcessDocuments(args[1], crawler_textmode);
				} else if(args[0].equalsIgnoreCase("-update_nf")){
					NFCrawler.update();
					
				
					filter.readAndRewriteNFDump();
					createQCLIRCorpus.crawlAndProcessDocuments(args[1], crawler_textmode);
				} else if(args[0].equalsIgnoreCase("-nf_crawled")){
					// added the next line because doclinks need to be filtered: weren't filtered the first time
					filter.readAndRewriteNFDump();
					createQCLIRCorpus.crawlAndProcessDocuments(args[1], crawler_textmode);
				}
					
				else{
					System.err.println("Invalid first argument. Possible for nf crawling: -crawl_nf_new, -proceed_crawl_nf, -update_nf, -nf_crawled");
					return;
				}
			} 
			
			else{
				System.err.println("Invalid third argument. For reference For reference see QCL_MEDIR_README");
				return;
			}

}
	
	public static void crawlAndProcessDocuments(String arg, DocCrawler doc_crawler){
		if(arg.equalsIgnoreCase("-crawl_doc_new")){
			Editor.deleteFile(DocProperties.STATUS_PATH);
			Editor.deleteFile(DocProperties.USELESS_DOCLINKS_PATH); 
			Editor.deleteFile(DocProperties.DOC_DUMP_PATH);
			Editor.deleteFile(DocProperties.LAST_QUERY_AND_DOCUMENT_PATH); 
			Editor.deleteFile(DocProperties.TEMP_PDF_PATH);	
			doc_crawler.traverseDumpAndCrawlDocuments();
			createQCLIRCorpus.doOnlyProcessing();
	} else if(arg.equalsIgnoreCase("-proceed_crawl_doc")){
		doc_crawler.traverseDumpAndCrawlDocuments();
			createQCLIRCorpus.doOnlyProcessing();
	} else if(arg.equalsIgnoreCase("-doc_crawled")){
		createQCLIRCorpus.doOnlyProcessing();
	} else{
		System.err.println("Invalid argument. The argument should be either \"-crawl_doc_new\" if you want to crawl all documents from the dump, \"-proceed_crawl_doc\" if you want to proceed the crawling process after it being interrupted or \"doc_crawled\" if you only want to do the processing.");
		return;
	}


	}
	
	

	public static void main(String[] args) {

			if(args.length == 1 && args[0].equalsIgnoreCase("-help")){
				Reader.readAndPrintLinesFromFile("/help.txt");
				return;
			} else if(args.length == 3){
				createQCLIRCorpus.setTestBool(false);
				createQCLIRCorpus.handleTextMode(args);
			} else if(args.length == 4 && args[3].matches("-test=\\d+") && args[0].equalsIgnoreCase("-crawl_nf_new")){
				System.out.println("RUNNING PROGRAMM IN TESTING MODE...");
				createQCLIRCorpus.setTestBool(true);
				Integer max = Integer.parseInt(args[3].split("=")[1].trim());
				createQCLIRCorpus.setTestMax(max);
				createQCLIRCorpus.handleTextMode(args);
			}
				else{
			
				System.err.println("Invalid number of arguments or argument format. For reference see QCL_MEDIR_README.README");
				return;
				}

		
	}
	
	public static final DelayManager getDelay_manager() {
		return delay_manager;
	}

	public static boolean isTestBool() {
		return testBool;
	}

	public static void setTestBool(boolean testBool) {
		createQCLIRCorpus.testBool = testBool;
	}

	public static Integer getTestMax() {
		return testMax;
	}

	public static void setTestMax(Integer testMax) {
		createQCLIRCorpus.testMax = testMax;
	}
	
	

}
