/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.exp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo;
import com.ibm.pem.utilities.util.ApiResponse;

public class SFGPartnerDataWriter {

	private static final String CODE_TRUE = "true";
	private Configuration config;
	private SFGPartnerExportHeaderInfo headerInfo;
	private File csvFile;

	public SFGPartnerDataWriter(Configuration config, SFGPartnerExportHeaderInfo headerInfo, File csvFile) {
		this.config = config;
		this.headerInfo = headerInfo;
		this.csvFile = csvFile;
	}

	void writeHeaders() throws IOException {
		String delimiter = config.getDelimiter();
		List<String> headersToPrint = headerInfo.getHeaders();

		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFile, true))) {
			StringBuilder strBuilder = new StringBuilder();
			for (int j = 0; j < headersToPrint.size(); j++) {
				String header = headersToPrint.get(j);
				if (j == 0) {
					strBuilder.append(headerInfo.getHeaderDisplayValue(header));
				} else {
					strBuilder.append(delimiter);
					strBuilder.append(headerInfo.getHeaderDisplayValue(header));
				}
			}
			strBuilder.append(System.lineSeparator());
			csvWriter.write(strBuilder.toString());
		}
	}

	/**
	 * Write csv file from response containing json array
	 */
	void writeCSV(ApiResponse response, Map<String, String> additionalAttributes, String type)
			throws ImportException, IOException {
		JSONArray arrayOfObjects = null;
		try {
			arrayOfObjects = new JSONArray(response.getResponse());
		} catch (JSONException e) {
			throw new ImportException(e);
		}

		String delimiter = config.getDelimiter();
		List<String> headers = headerInfo.getHeaders();

		try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFile, true))) {
			if (arrayOfObjects != null && arrayOfObjects.length() > 0) {
				for (int i = 0; i < arrayOfObjects.length(); i++) {
					StringBuilder buf = new StringBuilder();

					JSONObject jsonObj = arrayOfObjects.getJSONObject(i);

					String isListeningProducer = jsonObj.has("producerSshConfiguration") ? "Y" : "N";
					String isListeningConsumer = jsonObj.has("consumerSshConfiguration") ? "Y" : "N";
					String doesUseSSH = jsonObj.getJSONObject("doesUseSSH").get("code").toString();
					String isInitiatingProducer = (doesUseSSH.equals(CODE_TRUE)
							&& jsonObj.getJSONObject("isInitiatingProducer").get("code").toString().equals(CODE_TRUE))
									? "Y"
									: "N";
					String isInitiatingConsumer = (doesUseSSH.equals(CODE_TRUE)
							&& jsonObj.getJSONObject("isInitiatingConsumer").get("code").toString().equals(CODE_TRUE))
									? "Y"
									: "N";

					for (int j = 0; j < headers.size(); j++) {
						String header = headers.get(j);
						Object headerValue = "";
						if (additionalAttributes != null) {
							headerValue = jsonObj.has(header) ? jsonObj.get(header)
									: (additionalAttributes.get(header) != null ? additionalAttributes.get(header)
											: "");
						} else {
							if (header.equals("sfgPartnerKey")) {
								header = "_id";
							} else if (header.equals("userName")) {
								header = "username";
							} else if (header.equals("sponsorDivisionKey")) {
								header = "divisionKey";
							} else if (header.equals("lastName")) {
								header = "surname";
							}
							headerValue = jsonObj.has(header) ? jsonObj.get(header) : "";
						}

						headerValue = parseResponse(header, headerValue.toString(), type, isListeningProducer,
								isListeningConsumer, isInitiatingProducer, isInitiatingConsumer);

						if (j == 0) {
							buf.append(headerValue.toString());
						} else {
							buf.append(delimiter);
							buf.append(headerValue);
						}
					}
					buf.append(System.lineSeparator());
					csvWriter.write(buf.toString());
				}
			}
		}
	}

	private static Object parseResponse(String header, String headerValue, String type, String isListeningProducer,
			String isListeningConsumer, String isInitiatingProducer, String isInitiatingConsumer) {
		if (header.equals("processingStatus")) {
			headerValue = PartnerInfo.ProcessingStatus.NOT_PROCESSED.getCode();
		}
		if (header.equals("type")) {
			headerValue = type;
		}
		if (header.equals("isListeningProducer")) {
			headerValue = isListeningProducer;
		}
		if (header.equals("isListeningConsumer")) {
			headerValue = isListeningConsumer;
		}
		if (header.equals("isInitiatingProducer")) {
			headerValue = isInitiatingProducer;
		}
		if (header.equals("isInitiatingConsumer")) {
			headerValue = isInitiatingConsumer;
		}
		return headerValue;
	}

}
