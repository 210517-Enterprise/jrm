package com.revature.exceptions;

public class MultiplePrimaryKeyException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8557207795079964812L;

	public MultiplePrimaryKeyException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MultiplePrimaryKeyException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public MultiplePrimaryKeyException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public MultiplePrimaryKeyException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public MultiplePrimaryKeyException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	
}
