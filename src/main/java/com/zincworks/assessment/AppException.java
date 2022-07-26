package com.zincworks.assessment;

import com.zincworks.exception.BaseException;

/**
 * handle App package exceptions
 */
public class AppException extends BaseException {

	private static final long serialVersionUID = 1144284737291651033L;

	public AppException() {
		super();
	}
	
	public AppException(final Exception ex) {
		super(ex);
	}
	
	public AppException(final String err) {
		super(err);
	}
}
