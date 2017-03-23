package crawling_queries;

import io.Reader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that contains different patterns, paths, and pages that are used during the crawling process of queries.
 * @author Vera Boteva, Demian Gholipour
 *
 */

public abstract class Properties {
	public static final double INFINITY = Double.MAX_VALUE;
	
	public static final  String NF_MAIN_PAGE = "http://nutritionfacts.org/";
	public static final Pattern NF_PAGE_PATTERN = Pattern.compile("http:\\/\\/(www\\.)?nutritionfacts.*");
	public static final String NFDUMP_PATH = "nfdump.txt";
	public static final String UNFILTERED_NFDUMP_PATH = "unfiltered_nfdump.txt";
	public static final String PATH_TO_QUERYDUMPS = "crawling_queries/querydumps/";
	public static final String PATH_TO_QUERYPROCESSING = "crawling_queries/queryprocessing/";
	
	public static final String[] PATHS_TO_QUERYDUMPS = {PATH_TO_QUERYDUMPS + "articledump.txt",
														PATH_TO_QUERYDUMPS + "q_diet_dump.txt",
														PATH_TO_QUERYDUMPS + "q_doctor_dump.txt",
														PATH_TO_QUERYDUMPS + "topicdump.txt", 
														PATH_TO_QUERYDUMPS + "videodump.txt"};
	
	public static final String LINKS_PATH = "crawling_queries/querylinks/";
	public static final String ARTICLE_MAINPAGE = NF_MAIN_PAGE + "blog/";
	public static final String ARTICLE_LINKS_PATH = "crawling_queries/querylinks/articlelinks.txt";
	public static final Pattern ARTICLE_PAGE_PATTERN = Pattern.compile("http:\\/\\/nutritionfacts.org\\/(\\d)*\\/(\\d)*\\/(\\d)*\\/([a-z]*\\-)*[a-z]*\\/");
	
	public static final String QUESTION_DOCTOR_LINKS_PATH = "crawling_queries/querylinks/doctor_questionlinks.txt";
	public static final String QUESTION_DIETITIAN_LINKS_PATH = "crawling_queries/querylinks/dietitian_questionlinks.txt";

	
	// auch Zahlen sollten zugelassen sein
	public static final Pattern QUESTIONS_PAGE_PATTERN = Pattern.compile("http:\\/\\/nutritionfacts.org\\/(rd)?questions\\/([a-z0-9]*\\-)*[a-z0-9]*\\/");
	public static final Pattern QUESTIONS_DIETITIAN_PAGE_PATTERN = Pattern.compile("http:\\/\\/nutritionfacts.org\\/rdquestions\\/([a-z0-9]*\\-)*[a-z0-9]*\\/");
	public static final Pattern QUESTIONS_DOCTOR_PAGE_PATTERN = Pattern.compile("http:\\/\\/nutritionfacts.org\\/questions\\/([a-z0-9]*\\-)*[a-z0-9]*\\/");
	
	public static final String TOPIC_LINKS_PATH = "crawling_queries/querylinks/topiclinks.txt";
	public static final Pattern TOPICS_PAGE_PATTERN = Pattern.compile("http:\\/\\/nutritionfacts.org\\/topics\\/.*\\/");
	public static final Pattern TOPICS_PAGE_LINK = Pattern.compile("http://nutritionfacts.org/topics/");
	
	public static final String VIDEO_LINKS_PATH = "crawling_queries/querylinks/videolinks.txt";
	public static final String VIDEO_PAGES_LINK = "http://nutritionfacts.org/videos/page/";
	public static final Pattern VIDEO_PAGE_PATTERN = Pattern.compile("http:\\/\\/nutritionfacts\\.org\\/video\\/.*/");
	public static final String VIDEO_MAIN_PAGE = "http://nutritionfacts.org/videos/";

	public static final Pattern URL_PATTERN = Pattern.compile("http:\\/\\/.*");
	public static final Pattern MAIN_PAGE_PATTERN = Pattern.compile("http:\\/\\/(www\\.)?([a-z]+\\.)+[a-z]+\\/?");
	public static final Pattern TEST = Pattern.compile("/([a-z]*\\-)*[a-z]*\\/");
	
	public static final String PATH_TO_QUERY_FORECAST = "crawling_queries/query_forecast.txt";
	public static final String USER_AGENT = "Student Project Uni Heidelberg (boteva@cl.uni-heidelberg.de, gholipour@stud.uni-heidelberg.de)";
}
