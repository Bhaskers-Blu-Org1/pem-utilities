/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem;

public class ValidationException extends Exception {

	private static final long serialVersionUID = -316664440958700728L;

	private String processingStatus = null;

	public ValidationException() {
		super();
	}

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(Throwable e) {
		super(e);
	}

	public ValidationException(String processingStatus, String message) {
		super(message);
		this.processingStatus = processingStatus;
	}

	public ValidationException(String message, Throwable e) {
		super(message, e);
	}

	public String getStatus() {
		return processingStatus;
	}

}
