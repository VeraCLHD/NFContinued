package crawling_docs;


/**
 * Every time an expected exception occurs, e.g. when we try to build a connection with jsoup to a pdf page -> MimeTypeException,
 * this Exception is thrown.
 * 
 * @author Vera Boteva, Demian Gholipour
 *
 */
public class ExpectedConnectionException extends Exception {
	
	public ExpectedConnectionException() {super();};

}
