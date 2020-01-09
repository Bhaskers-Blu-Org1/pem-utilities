/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo;

public class SfgPartnerReportReader {

	private Configuration config;
	private String delimiter;

	public SfgPartnerReportReader(Configuration config) {
		this.config = config;
		init(config);
	}

	private void init(Configuration config) {
		this.delimiter = config.getDelimiter();
		if (delimiter.equals("|")) {
			delimiter = "\\" + delimiter;
		}
	}

	public HashMap<String, PartnerInfo> execute(String filePath) throws IOException, ValidationException {
		InputStreamReader testIn = new InputStreamReader(new FileInputStream(filePath), "UTF-8");
		BufferedReader reader = new BufferedReader(testIn);

		try {
			HeaderInfo headerInfo = CsvUtils.parseHeaders(reader, delimiter, config);
			return processData(reader, headerInfo);
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (testIn != null) {
				testIn.close();
			}
		}
	}

	private HashMap<String, PartnerInfo> processData(BufferedReader reader, HeaderInfo headerInfo)
			throws IOException, ValidationException {
		HashMap<String, PartnerInfo> testSfgPartnerMap = new HashMap<>();
		String lineData = null;
		while ((lineData = reader.readLine()) != null) {
			if (lineData.isEmpty()) {
				continue;
			}
			PartnerInfo partnerInfo = CsvUtils.translateRow(lineData.split(delimiter), headerInfo);
			testSfgPartnerMap.put(partnerInfo.getSfgPartnerKey(), partnerInfo);
		}
		return testSfgPartnerMap;
	}

}
