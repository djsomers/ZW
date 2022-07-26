package com.zincworks.service;

import com.zincworks.exception.BaseException;

/**
 * handle Service package exceptions
 */
public class ServiceException extends BaseException {

	private static final long serialVersionUID = 1144284737291651033L;

	public ServiceException() {
		super();
	}
	
	public ServiceException(final Exception ex) {
		super(ex);
	}
	
	public ServiceException(final String err) {
		super(err);
	}
}
