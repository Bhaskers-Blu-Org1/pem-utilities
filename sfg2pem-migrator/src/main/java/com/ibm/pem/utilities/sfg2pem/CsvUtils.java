/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo;
import com.ibm.pem.utilities.sfg2pem.imp.PemPartnerInfo;

public class CsvUtils {

	private CsvUtils() {
	}

	public static void writeLine(Writer writer, List<String> row, String delimiter) throws IOException {
		writeLine(writer, row, delimiter, ' ');
	}

	private static String followCVSformat(String value) {

		String result = value;
		if (result.contains("\"")) {
			result = result.replace("\"", "\"\"");
		}
		return result;

	}

	public static void writeLine(Writer writer, List<String> row, String delimiter, char customQuote)
			throws IOException {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		for (String value : row) {
			if (!first) {
				sb.append(delimiter);
			}
			if (value == null) {
				// Do nothing
			} else if (customQuote == ' ') {
				sb.append(followCVSformat(value));
			} else {
				sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
			}

			first = false;
		}
		sb.append('\n');
		writer.append(sb.toString());
	}

	/**
	 * Parses the first non-empty line as headers.
	 */
	public static HeaderInfo parseHeaders(BufferedReader reader, String delimiter, Configuration config)
			throws IOException {
		HeaderInfo headerInfo = null;
		String lineData = null;
		while ((lineData = reader.readLine()) != null) {
			if (!lineData.isEmpty()) {
				String rowValues[] = lineData.split(delimiter);
				headerInfo = new HeaderInfo(config.getProps(), rowValues);
				break;
			}
		}
		return headerInfo;
	}

	public static PartnerInfo translateRow(String[] rowValues, HeaderInfo headerInfo) {
		PartnerInfo info = new PartnerInfo();
		List<String> headers = headerInfo.getHeaders();
		for (int i = 0; i < headers.size(); i++) {
			String header = headers.get(i);
			if (rowValues.length >= i + 1) {
				info.setData(header, rowValues[i]);
			}
		}
		return info;
	}

	public static PemPartnerInfo translatePemPartnerRow(String[] rowValues, HeaderInfo headerInfo) {
		PemPartnerInfo info = new PemPartnerInfo();
		List<String> headers = headerInfo.getHeaders();
		for (int i = 0; i < headers.size(); i++) {
			String header = headers.get(i);
			if (rowValues.length >= i + 1) {
				info.setData(header, rowValues[i]);
			}
		}
		return info;
	}

}
