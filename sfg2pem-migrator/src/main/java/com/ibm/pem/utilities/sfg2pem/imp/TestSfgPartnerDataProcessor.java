/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.CsvUtils;
import com.ibm.pem.utilities.sfg2pem.HeaderInfo;
import com.ibm.pem.utilities.sfg2pem.ValidationException;

public class TestSfgPartnerDataProcessor {

	private Configuration config;
	private String delimiter;

	public TestSfgPartnerDataProcessor(Configuration config) {
		this.config = config;
		init(config);
	}

	private void init(Configuration config) {
		this.delimiter = config.getDelimiter();
		if (delimiter.equals("|")) {
			delimiter = "\\" + delimiter;
		}
	}

	public HashMap<String, String> execute() throws IOException, ValidationException {
		InputStreamReader testIn = new InputStreamReader(new FileInputStream(config.getTestSfgFilePath()), "UTF-8");
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

	private HashMap<String, String> processData(BufferedReader reader, HeaderInfo headerInfo)
			throws IOException, ValidationException {
		ExecutorService executorService = Executors.newFixedThreadPool(1);

		HashMap<String, String> testSfgPartnerMap = new HashMap<>();
		String lineData = null;
		int linecounter = 0;
		while ((lineData = reader.readLine()) != null) {
			linecounter++;
			if (lineData.isEmpty()) {
				continue;
			}
			executorService.execute(new TestSfgPartnerDataProcessingTask(headerInfo, testSfgPartnerMap, lineData,
					linecounter, delimiter));
		}

		executorService.shutdown();
		while (!executorService.isTerminated()) {
			// Wait until all tasks are processed.
		}
		return testSfgPartnerMap;
	}

	public static class TestSfgPartnerDataProcessingTask implements Runnable {

		private static final Logger LOG = LoggerFactory.getLogger(TestSfgPartnerDataProcessingTask.class);

		private HeaderInfo headerInfo;
		private HashMap<String, String> testSfgPartnerMap;
		private String partnerData;
		private int rowId;
		private String delimiter;

		public TestSfgPartnerDataProcessingTask(HeaderInfo headerInfo, HashMap<String, String> testSfgPartnerMap,
				String partnerData, int rowId, String delimiter) {
			this.headerInfo = headerInfo;
			this.testSfgPartnerMap = testSfgPartnerMap;
			this.partnerData = partnerData;
			this.rowId = rowId;
			this.delimiter = delimiter;
		}

		@Override
		public void run() {
			if (LOG.isInfoEnabled()) {
				LOG.info(String.format("Processing row %s.", rowId));
			}
			PartnerInfo partnerInfo = CsvUtils.translateRow(partnerData.split(delimiter), headerInfo);
			try {
				validateData(partnerInfo);
				processPartnerInfo(partnerInfo);
			} catch (ValidationException e) {
				if (LOG.isErrorEnabled()) {
					LOG.error(e.getMessage());
				}
			} catch (Exception e) {
				if (LOG.isErrorEnabled()) {
					LOG.error("", e);
				}
			}
		}

		private void validateData(PartnerInfo partnerInfo) throws ValidationException {
			String sfgPartnerKey = partnerInfo.getSfgPartnerKey();
			if (sfgPartnerKey == null || sfgPartnerKey.trim().isEmpty()) {
				throw new ValidationException("SFG Partner Key not provided.");
			}
			String prSystemRef = partnerInfo.getPrSystemRef();
			// if (prSystemRef == null || prSystemRef.trim().isEmpty()) {
			// throw new ValidationException("PR System Ref not provided.");
			// }
			if (testSfgPartnerMap.containsKey(prSystemRef)) {
				throw new ValidationException("The specified PR System Ref has been used for another record.");
			}
		}

		private void processPartnerInfo(PartnerInfo partnerInfo) {
			String testSfgPartnerKey = partnerInfo.getSfgPartnerKey();
			String prSystemRef = partnerInfo.getPrSystemRef();
			if (prSystemRef != null && !prSystemRef.trim().isEmpty()) {
				testSfgPartnerMap.put(prSystemRef, testSfgPartnerKey);
			}
		}

	}

}
