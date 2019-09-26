/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.exp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ApiInvocationException;
import com.ibm.pem.utilities.sfg2pem.Constants;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.imp.ImportHelper;
import com.ibm.pem.utilities.util.ApiResponse;

/**
 * Generate Data in csv format using custom columns and custom delimiters
 * 
 * @author Anu
 *
 */
public class SFGPartnerExportHandler {

	private static final Logger LOG = LoggerFactory.getLogger(SFGPartnerExportHandler.class);

	private Configuration config;

	public SFGPartnerExportHandler(Configuration config) {
		this.config = config;
	}

	public void execute() throws IOException, ApiInvocationException, ImportException {
		SFGPartnerExportHeaderInfo headerInfo = new SFGPartnerExportHeaderInfo(config.getProps());

		if (LOG.isInfoEnabled()) {
			LOG.info("Exporting data from Production SFG.");
		}
		createInputForSFGPartnerData(true, headerInfo, config.getProdOutPutFile());

		if (LOG.isInfoEnabled()) {
			LOG.info("Exporting data from Test SFG.");
		}
		createInputForSFGPartnerData(false, headerInfo, config.getTestOutPutFile());
	}

	private void createInputForSFGPartnerData(boolean getFromProductionSfg, SFGPartnerExportHeaderInfo headerInfo,
			File csvFile) throws IOException, ApiInvocationException, ImportException {
		if (csvFile.exists()) {
			FileUtils.forceDelete(csvFile);
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("Creating file: " + csvFile);
		}
		csvFile.createNewFile();

		getSFGPartnerData(getFromProductionSfg, headerInfo, csvFile);
	}

	private void getSFGPartnerData(boolean getFromProductionSfg, SFGPartnerExportHeaderInfo headerInfo, File csvFile)
			throws ApiInvocationException, ImportException {
		List<ApiResponse> response = ImportHelper.getResourceListFromSFG(getFromProductionSfg, config,
				Constants.SFG_PARTNER_REST_URI);

		if (LOG.isInfoEnabled()) {
			LOG.info("Response count = " + (response != null ? response.size() : 0));
		}

		String type = getFromProductionSfg ? Constants.PROD : Constants.TEST;
		writeApiResponse(response, config, headerInfo, csvFile, type);
	}

	private static void writeApiResponse(List<ApiResponse> responses, Configuration config,
			SFGPartnerExportHeaderInfo headerInfo, File csvFile, String type) throws ImportException {
		try {
			SFGPartnerDataWriter writer = new SFGPartnerDataWriter(config, headerInfo, csvFile);
			writer.writeHeaders();
			for (ApiResponse apiResponse : responses) {
				writer.writeCSV(apiResponse, null, type);
			}
		} catch (IOException e) {
			throw new ImportException(e);
		}
	}

}
