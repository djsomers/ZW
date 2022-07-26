package com.zincworks.exception;

/**
 * Custom exception handling
 */
public class BaseException extends Exception {

	private static final long serialVersionUID = 1148042125519458891L;

	public BaseException() {
		super();
		
		// TODO: any custom exception handling
	}
	
	
	public BaseException(final Exception ex) {
		super(ex);
		
		// TODO: any custom exception handling
	}
	
	public BaseException(final String err) {
		super(err);
		
		// TODO: any custom exception handling
	}
}
