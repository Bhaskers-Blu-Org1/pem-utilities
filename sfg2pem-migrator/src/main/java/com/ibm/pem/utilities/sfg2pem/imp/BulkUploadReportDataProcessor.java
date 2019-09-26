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

public class BulkUploadReportDataProcessor {
	
	private Configuration config;
	private String delimiter;
	
	public BulkUploadReportDataProcessor (Configuration config) {
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
		HashMap<String, String> partnerGroupIdMap = new HashMap<>();

		if (config.getPemPartnerBulkUploadOutputFileName() != null && !"".equals(config.getPemPartnerBulkUploadOutputFileName())) {
			
			InputStreamReader bulkUploadFileReader = new InputStreamReader(new FileInputStream(
					config.getPartnerBulkUploadOutputFilePath()), "UTF-8");

			BufferedReader reader = new BufferedReader(bulkUploadFileReader);
			try {
			HeaderInfo headerInfo = CsvUtils.parseHeaders(reader, delimiter, config);
			partnerGroupIdMap = processData(reader, headerInfo);
			} finally {
				if (reader != null) {
					reader.close();
				}
				if (bulkUploadFileReader != null) {
					bulkUploadFileReader.close();
				}
			}
		}

		return partnerGroupIdMap;
	}
	
	private HashMap<String, String> processData(BufferedReader reader, HeaderInfo headerInfo)
			throws IOException, ValidationException {
		ExecutorService executorService = Executors.newFixedThreadPool(1);

		HashMap<String, String> pgidPartnerMap = new HashMap<>();
		String lineData = null;
		int linecounter = 0;
		while ((lineData = reader.readLine()) != null) {
			linecounter++;
			if (lineData.isEmpty()) {
				continue;
			}
			executorService.execute(new BulkUploadPartnerDataProcessingTask(headerInfo, pgidPartnerMap, lineData,
					linecounter, delimiter));
		}

		executorService.shutdown();
		while (!executorService.isTerminated()) {
			// Wait until all tasks are processed.
		}
		return pgidPartnerMap;
	}
	
	public static class BulkUploadPartnerDataProcessingTask implements Runnable {

		private static final Logger LOG = LoggerFactory.getLogger(BulkUploadPartnerDataProcessingTask.class);

		private HeaderInfo headerInfo;
		private HashMap<String, String> pgidPartnerMap;
		private String partnerData;
		private int rowId;
		private String delimiter;

		public BulkUploadPartnerDataProcessingTask(HeaderInfo headerInfo, HashMap<String, String> pgidPartnerMap,
				String partnerData, int rowId, String delimiter) {
			this.headerInfo = headerInfo;
			this.pgidPartnerMap = pgidPartnerMap;
			this.partnerData = partnerData;
			this.rowId = rowId;
			this.delimiter = delimiter;
		}

		@Override
		public void run() {
			if (LOG.isInfoEnabled()) {
				LOG.info(String.format("Processing row %s.", rowId));
			}
			PemPartnerInfo partnerInfo = CsvUtils.translatePemPartnerRow(partnerData.split(delimiter), headerInfo);
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

		private void validateData(PemPartnerInfo partnerInfo) throws ValidationException {
			String pemPartnerKey = partnerInfo.getPartnerKey();
			if (pemPartnerKey == null || pemPartnerKey.trim().isEmpty()) {
				throw new ValidationException("Not a valid Partner info.");
			}
			String partnerGroupId = partnerInfo.getPartnerGroupId();
			if (partnerGroupId == null || partnerGroupId.trim().isEmpty()) {
				throw new ValidationException("Correlation Id not provided.");
			}
			if (pgidPartnerMap.containsKey(partnerGroupId)) {
				throw new ValidationException("There are mulitple partner mapped to same group Id.");
			}
		}

		private void processPartnerInfo(PemPartnerInfo partnerInfo) {
			String partnerKey = partnerInfo.getPartnerKey();
			String partnerGroupId = partnerInfo.getPartnerGroupId();
			pgidPartnerMap.put(partnerGroupId, partnerKey);
		}

	}
}
