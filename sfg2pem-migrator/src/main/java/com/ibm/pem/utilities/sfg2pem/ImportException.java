/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem;

public class ImportException extends Exception {

	private static final long serialVersionUID = -316664440958700728L;

	public ImportException() {
		super();
	}

	public ImportException(String message) {
		super(message);
	}

	public ImportException(Throwable e) {
		super(e);
	}

	public ImportException(String message, Throwable e) {
		super(message, e);
	}

}
