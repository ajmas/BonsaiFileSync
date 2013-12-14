package osj.filesync;

/**
 * General exception use when a file sync failed.
 *  
 * @author ajmas
 *
 */
public class FileSyncException extends Exception {

	/** */
	private static final long serialVersionUID = 2918648943928136642L;
	protected String details;
	
	public FileSyncException() {
		super();
	}

	public FileSyncException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FileSyncException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public FileSyncException(String message, Throwable cause, String details) {
		super(message, cause);
		this.details = details;
	}

	public FileSyncException(String message) {
		super(message);
	}
	
	public FileSyncException(String message, String details) {
		super(message);
		this.details = details;
	}

	public FileSyncException(Throwable cause) {
		super(cause);
	}

	
	public String getDetails() {
		return details;
	}
	
}
