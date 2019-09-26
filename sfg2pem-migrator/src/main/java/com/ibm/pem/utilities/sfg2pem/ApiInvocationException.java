/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem;

public class ApiInvocationException extends Exception {

	private static final long serialVersionUID = -316664440958700728L;

	public ApiInvocationException() {
		super();
	}

	public ApiInvocationException(String message) {
		super(message);
	}

	public ApiInvocationException(Throwable e) {
		super(e);
	}

	public ApiInvocationException(String message, Throwable e) {
		super(message, e);
	}

}
