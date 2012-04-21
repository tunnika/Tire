package exceptions;

public class UnableToAuthenticateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5106116384722112351L;

	@SuppressWarnings("unused")
	private UnableToAuthenticateException() {
	}

	public UnableToAuthenticateException(String arg0) {
		super(arg0);
	}

	public UnableToAuthenticateException(Throwable arg0) {
		super(arg0);
	}

	public UnableToAuthenticateException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
