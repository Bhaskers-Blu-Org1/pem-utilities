/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HeaderInfo {

	public static final String HEADER_KEY_PREFIX = "header.";

	private List<String> headerCodes;

	private Map<String, String> headerCodeToDisplayMap;

	/**
	 * Loads the header information based on the headers available from an input
	 * CSV file.
	 */
	public HeaderInfo(Properties properties, String[] headerDisplayValues) {
		init(properties, headerDisplayValues);
	}
	
	public HeaderInfo() {
		headerCodes = new ArrayList<>();
		headerCodeToDisplayMap = new HashMap<>();
	}

	public List<String> getHeaders() {
		return headerCodes;
	}

	private void init(Properties properties, String[] headerDisplayValues) {
		headerCodes = new ArrayList<>(headerDisplayValues.length);
		headerCodeToDisplayMap = new HashMap<>(headerDisplayValues.length);
		HashMap<String, String> displayToKeyHeaderMap = getDisplayToCodeHeaderMap(properties);
		for (String headerDisplayValue : headerDisplayValues) {
			String headerCode = displayToKeyHeaderMap.get(headerDisplayValue);
			addHeader(headerCode, headerDisplayValue);
		}
	}

	public void addHeader(String code, String displayValue) {
		headerCodes.add(code != null ? code : displayValue);
		headerCodeToDisplayMap.put(code, displayValue);
	}

	private static HashMap<String, String> getDisplayToCodeHeaderMap(Properties properties) {
		HashMap<String, String> map = new HashMap<>();
		Enumeration<?> propertyNames = properties.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String key = (String) propertyNames.nextElement();
			if (key.startsWith(HEADER_KEY_PREFIX)) {
				String headerCode = key.substring(HEADER_KEY_PREFIX.length());
				String headerDisplayValue = properties.getProperty(HEADER_KEY_PREFIX + headerCode);
				map.put(headerDisplayValue, headerCode);
			}
		}
		return map;
	}

	public String getHeaderDisplayValue(String code) {
		return headerCodeToDisplayMap.get(code);
	}
}
