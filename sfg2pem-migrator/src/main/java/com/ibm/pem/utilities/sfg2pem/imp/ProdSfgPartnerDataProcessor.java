/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ApiInvocationException;
import com.ibm.pem.utilities.sfg2pem.Constants;
import com.ibm.pem.utilities.sfg2pem.CsvUtils;
import com.ibm.pem.utilities.sfg2pem.HeaderInfo;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.ValidationException;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo.PartnerInfoField;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo.ProcessingStatus;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SftpResourceHelper;
import com.ibm.pem.utilities.util.ApiResponse;
import com.ibm.pem.utilities.util.CreateFileUtil;

public class ProdSfgPartnerDataProcessor {

	private Configuration config;
	private ImportHandlerFactory handlerFactory;
	private String delimiter;

	private HashMap<String, String> testSfgPartnerMap;
	private List<String> partnerGroupIdList;
	private HashMap<String, String> partnerGroupIdMap;

	public ProdSfgPartnerDataProcessor(Configuration config) throws ValidationException {
		this.config = config;
		init(config);
	}

	private void init(Configuration config) throws ValidationException {
		this.handlerFactory = ImportHandlerFactory.buildInstance(config);
		this.delimiter = config.getDelimiter();
		this.partnerGroupIdList = new ArrayList<>();
		if (delimiter.equals("|")) {
			delimiter = "\\" + delimiter;
		}
	}

	public void setTestSfgPartnerMap(HashMap<String, String> testSfgPartnerMap) {
		this.testSfgPartnerMap = testSfgPartnerMap;
	}
	
	public void setPartnerGroupIdMap(HashMap<String, String> partnerGroupIdMap) {
		this.partnerGroupIdMap = partnerGroupIdMap;
	}

	public void execute() throws IOException, ValidationException {

		// Read partners from partners-prod.csv
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(config.getProdSfgFilePath()), "UTF-8"));
		
		// Initialize the writer
		FileWriter writer = null;
		FileWriter partnerBulkUploadWriter = null;
		try {
			writer = new FileWriter(config.getProdOutPutFile(), true);
			partnerBulkUploadWriter = new FileWriter(config.getPemPartnerBulkUploadInputFile(), false);

			HeaderInfo headerInfo = CsvUtils.parseHeaders(reader, delimiter, config);
			if (!headerInfo.getHeaders().contains("message")) {
				headerInfo.addHeader("message",
						config.getProps().getProperty(HeaderInfo.HEADER_KEY_PREFIX + "message"));
			}
			writeHeaders(headerInfo, writer);

			/* Gets headers info of pem partner bulk upload file */
			HeaderInfo partnerBulkUploadHeaderInfo = new PartnerBulkUploadHeaderInfo(config.getProps());;
			writeHeaders(partnerBulkUploadHeaderInfo, partnerBulkUploadWriter);

			processData(reader, headerInfo, writer, partnerBulkUploadHeaderInfo, partnerBulkUploadWriter);

			backuptheInputFile();
			renameTheOutPutFile();
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.flush();
				writer.close();
			}
			
			if (partnerBulkUploadWriter != null) {
				partnerBulkUploadWriter.flush();
				partnerBulkUploadWriter.close();
			}
		}
	}

	private void renameTheOutPutFile() {
		String filePath = config.getProdOutPutFile().getAbsoluteFile().getAbsolutePath();
		File oldFileName = new File(filePath);
		File newFileName = new File(config.getProdSfgFilePath());
		oldFileName.renameTo(newFileName);
	}

	private void backuptheInputFile() {
		String filePath = new File(config.getProdSfgFilePath()).getAbsolutePath();
		File oldFileName = new File(filePath);
		if (FilenameUtils.getExtension(filePath).equals("csv")) {
			filePath = FilenameUtils.removeExtension(filePath);
		}
		File newFileName = new File(filePath + "_" + CreateFileUtil.generateTimeStamp() + ".csv");
		oldFileName.renameTo(newFileName);
	}

	// TODO rename params
	private void processData(BufferedReader reader, HeaderInfo headerInfo, FileWriter writer,
			HeaderInfo partnerBulkUploadHeaderInfo, FileWriter partnerBulkUploadWriter) throws IOException {
		ExecutorService executorService = Executors.newFixedThreadPool(1);

		// For each partner
		String lineData = null;
		int linecounter = 0;
		while ((lineData = reader.readLine()) != null) {
			linecounter++;
			if (lineData.isEmpty()) {
				continue;
			}
			executorService.execute(new ProdSfgPartnerDataProcessingTask(headerInfo, testSfgPartnerMap, partnerGroupIdMap, lineData,
					linecounter, delimiter, config, handlerFactory, writer, this.partnerGroupIdList,
					partnerBulkUploadHeaderInfo, partnerBulkUploadWriter));
		}

		executorService.shutdown();
		while (!executorService.isTerminated()) {
			// Wait until all tasks are processed.
		}
	}

	static class ProdSfgPartnerDataProcessingTask implements Runnable {

		private static final Logger LOG = LoggerFactory.getLogger(ProdSfgPartnerDataProcessingTask.class);

		private Configuration config;
		private HeaderInfo headerInfo;
		private HashMap<String, String> testSfgPartnerMap;
		private HashMap<String, String> partnerGroupIdMap;
		private String partnerData;
		private int rowId;
		private String delimiter;
		private FileWriter writer;
		private ImportHandlerFactory handlerFactory;
		private List<String> partnerGroupIdList;
		private HeaderInfo partnerBulkUploadHeaderInfo;
		private FileWriter partnerBulkUploadWriter;

		public ProdSfgPartnerDataProcessingTask(HeaderInfo headerInfo, HashMap<String, String> testSfgPartnerMap,
				HashMap<String, String> partnerGroupIdMap, String partnerData, int rowId, String delimiter, Configuration config,
				ImportHandlerFactory handlerFactory, FileWriter writer, List<String> partnerGroupIdList,
				HeaderInfo partnerBulkUploadHeaderInfo, FileWriter partnerBulkUploadWriter) {
			this.headerInfo = headerInfo;
			this.testSfgPartnerMap = testSfgPartnerMap;
			this.partnerData = partnerData;
			this.rowId = rowId;
			this.delimiter = delimiter;
			this.config = config;
			this.handlerFactory = handlerFactory;
			this.writer = writer;
			this.partnerGroupIdList = partnerGroupIdList;
			this.partnerBulkUploadHeaderInfo = partnerBulkUploadHeaderInfo;
			this.partnerBulkUploadWriter = partnerBulkUploadWriter;
			this.partnerGroupIdMap = partnerGroupIdMap;
		}

		@Override
		public void run() {
			if (LOG.isInfoEnabled()) {
				LOG.info(String.format("Processing row %s.", rowId));
			}
			PartnerInfo partnerInfo = CsvUtils.translateRow(partnerData.split(delimiter), headerInfo);
			try {
				validateData(partnerInfo);
				processPartnerInfo(partnerInfo, headerInfo, writer);
			} catch (ValidationException e) {
				if (LOG.isErrorEnabled()) {
					LOG.error(e.getMessage());
				}
				if ((ProcessingStatus.IGNORED).equals(ProcessingStatus.parse(e.getStatus()))) {
					partnerInfo.setMessage(e.getMessage());
					partnerInfo.setProcessingStatus(ProcessingStatus.IGNORED);
				} else {
					partnerInfo.setProcessingStatus(ProcessingStatus.ERROR);
					partnerInfo.setMessage(e.getMessage());
				}
				try {
					writeRow(partnerInfo, headerInfo, writer);
				} catch (IOException e1) {
					if (LOG.isErrorEnabled()) {
						LOG.error("", e);
					}
				}
			} catch (Exception e) {
				if (LOG.isErrorEnabled()) {
					LOG.error("", e);
				}
				partnerInfo.setProcessingStatus(ProcessingStatus.ERROR);
				partnerInfo.setMessage(e.getMessage());
				try {
					writeRow(partnerInfo, headerInfo, writer);
				} catch (IOException e1) {
					if (LOG.isErrorEnabled()) {
						LOG.error("", e);
					}
				}
			}
		}

		private void processPartnerInfo(PartnerInfo partnerInfo, HeaderInfo headerInfo, FileWriter writer)
				throws ApiInvocationException, ValidationException, ParserConfigurationException, SAXException,
				IOException, ImportException {
			String pemPartnerKey = partnerInfo.getPemPartnerKey().trim();

			ProcessingStatus processingStatus = partnerInfo.getProcessingStatus();
			
			if (processingStatus != ProcessingStatus.NOT_PROCESSED && processingStatus != ProcessingStatus.ERROR ) {
				// Invalid status for processing. Write the data as it is.
				if (partnerGroupIdMap != null && partnerGroupIdMap.containsKey(pemPartnerKey)) {
					pemPartnerKey = partnerGroupIdMap.get(pemPartnerKey);
					partnerInfo.setData(PartnerInfoField.pemPartnerKey.getCode(), pemPartnerKey);
				} else if (!pemPartnerKey.startsWith(Constants.PARTNER_GROUP_ID_PREFIX)) {
					writeRow(partnerInfo, headerInfo, writer);
					return;
				}
			}


			if (pemPartnerKey.startsWith(Constants.PARTNER_GROUP_ID_PREFIX)) {
				synchronized (pemPartnerKey) {
					if (!this.partnerGroupIdList.contains(pemPartnerKey)) {
						this.partnerGroupIdList.add(pemPartnerKey);
						PemPartnerInfo pemPartnerInfo = new PemPartnerInfo(partnerInfo);
						writeRow(pemPartnerInfo, this.partnerBulkUploadHeaderInfo, this.partnerBulkUploadWriter);
					}
				}
				partnerInfo.setMessage("Partner needs to be imported to PEM.");
				partnerInfo.setProcessingStatus(ProcessingStatus.PARTNER_IMPORT_PENDING);
			} else {
				// Validate if a valid partner exists in PEM
				validatePemPartnerExists(pemPartnerKey);
				String testSfgPartnerKey = testSfgPartnerMap.get(partnerInfo.getPrSystemRef());
				// Allow import if both test and prod partners are available.
				if (testSfgPartnerKey == null || testSfgPartnerKey.trim().isEmpty()) {
					throw new ValidationException("No partner reference specified from SFG-Test env.");
				}

				// Fetch partner and configuration info from SFG-prod
				ApiResponse prodApiResponse = ImportHelper.getProdSFGPartner(config, 
						partnerInfo.getSfgPartnerKey().trim());
				String getProdSfgPartnerApiResponse = validateSfgPartnerExistsAndGet(prodApiResponse);
				
				// Fetch partner and configuration info from SFG-test
				ApiResponse testApiResponse = ImportHelper.getTestSFGPartner(config, testSfgPartnerKey);
				String getTestSfgPartnerApiResponse = validateSfgPartnerExistsAndGet(testApiResponse);
		
				Document prodSfgPartner = ImportHelper.buildDomDoc(getProdSfgPartnerApiResponse);
				Document testSfgPartner = ImportHelper.buildDomDoc(getTestSfgPartnerApiResponse);

				PrConfigurationImportHandler handler = handlerFactory.getConfigurationHandler(prodSfgPartner, testSfgPartner);
				if (handler == null) {
					throw new ValidationException("SFTP Profile does not exists as part of SFG Partner.");
				}

				String prodSfgUserName = SftpResourceHelper.getAttributeValueByTagName(prodApiResponse.getResponse(), "TradingPartner", "username");
				String testSfgUserName = SftpResourceHelper.getAttributeValueByTagName(testApiResponse.getResponse(), "TradingPartner", "username");
			
				partnerInfo.setProdSfgPartnerDoc(prodSfgPartner);
				partnerInfo.setTestSfgPartnerKey(testSfgPartnerKey);
				partnerInfo.setTestSfgUserName(testSfgUserName);
				partnerInfo.setProdSfgUserName(prodSfgUserName);
				handler.execute(partnerInfo);

				// Configuration import completed successfully.
				partnerInfo.setProcessingStatus(ProcessingStatus.UPLOADED_TO_PEM);
				partnerInfo.setMessage("");
			}
			writeRow(partnerInfo, headerInfo, writer);
		}

		private String validateSfgPartnerExistsAndGet(ApiResponse apiResponse)
				throws ApiInvocationException, ValidationException {
			if (LOG.isInfoEnabled()) {
				ImportHelper.printApiResponse(apiResponse);
			}
			if (!apiResponse.getStatusCode().equals("200")) {
				if (LOG.isInfoEnabled()) {
					LOG.info("Failed to get a SFG partner with key " + apiResponse.getStatusLine() + ".");
				}
				String message = apiResponse.getResponse();
				if (message == null || message.isEmpty()) {
					message = apiResponse.getStatusLine();
				}
				throw new ValidationException("code = " + apiResponse.getStatusCode() + ", message = " + message);
			}
			return apiResponse.getResponse();
		}

		private void validatePemPartnerExists(String pemPartnerKey) throws ApiInvocationException, ValidationException {
			ApiResponse getPemPartnerApiResponse = ImportHelper.getPEMPartner(config, pemPartnerKey);
			if (LOG.isInfoEnabled()) {
				ImportHelper.printApiResponse(getPemPartnerApiResponse);
			}
			if (!getPemPartnerApiResponse.getStatusCode().equals("200")) {
				if (LOG.isInfoEnabled()) {
					LOG.info("Failed to get a PEM partner with key " + pemPartnerKey + ".");
				}
				throw new ValidationException("code = " + getPemPartnerApiResponse.getStatusCode() + ", message = "
						+ getPemPartnerApiResponse.getStatusLine());
			}
		}

		private void validateData(PartnerInfo partnerInfo) throws ValidationException {
			ProcessingStatus processingStatus = partnerInfo.getProcessingStatus();
			if (processingStatus == null) {
				throw new ValidationException("Processing Status not provided or is not supported.");
			}
			String pemPartnerKey = partnerInfo.getPemPartnerKey();
			if (pemPartnerKey == null || pemPartnerKey.trim().isEmpty()) {
				throw new ValidationException("PEM Partner Key not provided.");
			}
			String sfgPartnerKey = partnerInfo.getSfgPartnerKey();
			if (sfgPartnerKey == null || sfgPartnerKey.trim().isEmpty()) {
				throw new ValidationException("SFG Partner Key not provided.");
			}
			String sponsorDivisionKey = partnerInfo.getSponsorDivisionKey();
			if (sponsorDivisionKey == null || sponsorDivisionKey.trim().isEmpty()) {
				throw new ValidationException("Sponsor Division Key not provided.");
			}
			String prSystemRef = partnerInfo.getPrSystemRef();
			if (prSystemRef == null || prSystemRef.trim().isEmpty()) {
				throw new ValidationException("PR System Ref not provided.");
			}
		}

		private void writeRow(PartnerInfo partnerInfo, HeaderInfo headerInfo, FileWriter writer) throws IOException {
			ArrayList<String> newRowValues = new ArrayList<String>();
			headerInfo.getHeaders().forEach(header -> {
				newRowValues.add(partnerInfo.getData(header));
			});
			synchronized (writer) {
				CsvUtils.writeLine(writer, newRowValues, delimiter);
			}
		}

		private void writeRow(PemPartnerInfo partnerInfo, HeaderInfo headerInfo, FileWriter writer) throws IOException {
			ArrayList<String> newRowValues = new ArrayList<String>();
			headerInfo.getHeaders().forEach(header -> {
				newRowValues.add(partnerInfo.getData(header));
			});
			synchronized (writer) {
				CsvUtils.writeLine(writer, newRowValues, delimiter);
			}
		}

	}

	private void writeHeaders(HeaderInfo headerInfo, FileWriter writer) throws IOException {
		List<String> headerCodes = headerInfo.getHeaders();
		List<String> headerDisplayValues = new ArrayList<>(headerCodes.size());
		for (String code : headerCodes) {
			headerDisplayValues.add(headerInfo.getHeaderDisplayValue(code));
		}
		CsvUtils.writeLine(writer, headerDisplayValues, delimiter);
	}

}
