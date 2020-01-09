/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.util;

public class ResourceFactory {

	public ResourceFactory() {
	}

	public HttpClientUtil createHttpClientInstance() {
		return new HttpClientUtil();
	}

}
