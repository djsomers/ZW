package com.zincworks.db;

import com.zincworks.exception.BaseException;

/**
 * handle Db package exceptions
 */
public class DbException extends BaseException {

	private static final long serialVersionUID = 1144284737291651033L;

	public DbException() {
		super();
	}
	
	public DbException(final Exception ex) {
		super(ex);
	}
}