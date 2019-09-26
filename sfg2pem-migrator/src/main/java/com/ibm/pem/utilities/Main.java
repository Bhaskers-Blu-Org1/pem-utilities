/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.pem.utilities.sfg2pem.Constants;
import com.ibm.pem.utilities.sfg2pem.ValidationException;
import com.ibm.pem.utilities.sfg2pem.exp.SFGPartnerExportHandler;
import com.ibm.pem.utilities.sfg2pem.imp.ImportHandler;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	private static final String HTTP_PROTOCOL = "http";
	private static final String HTTPS_PROTOCOL = "https";

	public static void main(String[] args) throws Exception {
		try {
			String configFilePath = args[0].trim();
			execute(configFilePath);
		} catch (ValidationException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(e.getMessage());
			}
		} catch (Throwable e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("", e);
			}
		}
	}

	public static void execute(String configFilePath) throws Exception {
		Configuration config = new Configuration();
		config.load(configFilePath);

		String protocol = config.getProtocol();
		if (protocol == null
				|| !(protocol.equalsIgnoreCase(HTTP_PROTOCOL) || protocol.equalsIgnoreCase(HTTPS_PROTOCOL))) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Please provide valid protocol. http or https");
			}
			throw new ValidationException("Provide a valid value for protocol.");
		}
		if (protocol.equalsIgnoreCase(HTTPS_PROTOCOL)) {
			if (System.getProperty("javax.net.ssl.trustStore") == null
					|| System.getProperty("javax.net.ssl.trustStorePassword") == null) {
				if (LOG.isErrorEnabled()) {
					LOG.error("Please set the TrustStore and TrustStorePassword");
				}
				throw new ValidationException("System properties not defined for trustStore and trustStorePassword.");
			} else {
				if (LOG.isInfoEnabled()) {
					LOG.info("javax.net.ssl.trustStore: " + System.getProperty("javax.net.ssl.trustStore"));
					LOG.info("javax.net.ssl.trustStorePassword: "
							+ System.getProperty("javax.net.ssl.trustStorePassword"));
				}
			}
		}
		execute(config);
	}

	private static void execute(Configuration config) throws Exception {
		switch (config.getMode()) {
		case Constants.MODE_EXTRACT_SFG_PARTNER_DATA:
			new SFGPartnerExportHandler(config).execute();
			if (LOG.isInfoEnabled()) {
				LOG.info("Extracting SFG PartnerData is completed.");
			}
			break;
		case Constants.MODE_IMPORT_SFG_PARTNER_DATA_TO_PEM:
			new ImportHandler(config).execute();
			if (LOG.isInfoEnabled()) {
				LOG.info("Importing SFG PartnerData To PEM is completed.");
			}
			break;
		default:
			if (LOG.isErrorEnabled()) {
				LOG.error("Unsupported mode: " + config.getMode());
			}
		}
	}
}
