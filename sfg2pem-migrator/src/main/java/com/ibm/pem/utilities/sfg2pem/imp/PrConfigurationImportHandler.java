/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ApiInvocationException;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.ValidationException;
import com.ibm.pem.utilities.util.ApiResponse;

public abstract class PrConfigurationImportHandler {

	private static final Logger LOG = LoggerFactory.getLogger(PrConfigurationImportHandler.class);

	private Configuration config;

	public PrConfigurationImportHandler(Configuration config) {
		this.config = config;
	}

	public abstract boolean accept(Document partnerInfo, Document testSfgPartner);

	public void execute(PartnerInfo partnerInfo) throws ImportException, ApiInvocationException, ValidationException {
		// Fetch the partner data from the test SFG instance.
		String testSfgPartnerKey = partnerInfo.getTestSfgPartnerKey();
		String testSfgPartnerDataApiResponse = validateTestSfgPartnerExistsAndGet(testSfgPartnerKey);
		Document testSfgPartner;
		try {
			testSfgPartner = ImportHelper.buildDomDoc(testSfgPartnerDataApiResponse);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ImportException(e);
		}
		partnerInfo.setTestSfgPartnerDoc(testSfgPartner);
		doExecute(partnerInfo);
	}

	protected abstract void doExecute(PartnerInfo partnerInfo)
			throws ImportException, ApiInvocationException, ValidationException;

	protected Configuration getConfig() {
		return config;
	}

	private String validateTestSfgPartnerExistsAndGet(String sfgPartnerKey)
			throws ApiInvocationException, ValidationException {
		ApiResponse apiResponse = ImportHelper.getTestSFGPartner(getConfig(), sfgPartnerKey);
		if (!apiResponse.getStatusCode().equals("200")) {
			if (LOG.isInfoEnabled()) {
				ImportHelper.printApiResponse(apiResponse);
				LOG.info("Failed to get a SFG partner with key " + sfgPartnerKey + ".");
			}
			String message = apiResponse.getResponse();
			if (message == null || message.isEmpty()) {
				message = apiResponse.getStatusLine();
			}
			throw new ValidationException("code = " + apiResponse.getStatusCode() + ", message = " + message);
		}
		return apiResponse.getResponse();
	}

}
