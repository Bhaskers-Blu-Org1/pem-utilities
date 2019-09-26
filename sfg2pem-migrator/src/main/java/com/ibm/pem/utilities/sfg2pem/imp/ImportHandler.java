/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ValidationException;

public class ImportHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ImportHandler.class);

	private Configuration config;

	public ImportHandler(Configuration config) {
		this.config = config;
	}

	public void execute() throws IOException, ValidationException {
		if (LOG.isInfoEnabled()) {
			LOG.info("Processing data from Test SFG.");
		}
		TestSfgPartnerDataProcessor testSfgPartnerDataProcessor = new TestSfgPartnerDataProcessor(config);
		HashMap<String, String> testSfgPartnerMap = testSfgPartnerDataProcessor.execute();

		if (LOG.isInfoEnabled()) {
			LOG.info("Processing data from Production SFG.");
		}

		ProdSfgPartnerDataProcessor prodSfgPartnerDataProcessor = new ProdSfgPartnerDataProcessor(config);
		prodSfgPartnerDataProcessor.setTestSfgPartnerMap(testSfgPartnerMap);
		
		BulkUploadReportDataProcessor bulkUploadReportDataProcessor = new BulkUploadReportDataProcessor(config);
		HashMap<String, String> partnerGroupIdMap = bulkUploadReportDataProcessor.execute();
		prodSfgPartnerDataProcessor.setPartnerGroupIdMap(partnerGroupIdMap);
		
		prodSfgPartnerDataProcessor.execute();
	}

}
