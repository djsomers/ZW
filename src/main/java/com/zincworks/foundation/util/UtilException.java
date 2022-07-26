package com.zincworks.foundation.util;

import com.zincworks.exception.BaseException;

/**
 * handle Util package exceptions
 */
public class UtilException extends BaseException {

	private static final long serialVersionUID = 1144284737291651033L;

	public UtilException() {
		super();
	}
	
	public UtilException(final Exception ex) {
		super(ex);
	}
	
	public UtilException(final String err) {
		super(err);
	}
}

