package com.zincworks.bank;

import com.zincworks.exception.BaseException;


public class BankException extends BaseException {

	private static final long serialVersionUID = 1022881947468001921L;

	public BankException() {
		super();
	}
	
	public BankException(final Exception ex) {
		super(ex);
	}
	
	public BankException(final String err) {
		super(err);
	}
}
