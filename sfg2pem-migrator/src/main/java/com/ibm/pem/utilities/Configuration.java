/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import com.ibm.pem.utilities.sfg2pem.Constants;
import com.ibm.pem.utilities.sfg2pem.ValidationException;
import com.ibm.pem.utilities.util.CreateFileUtil;

public class Configuration {

	/*
	 * Internally tool will replace the values for the parameters and constructs the
	 * URL for PROD SFG instance.
	 */
	private static final String SFG_PROD_API_URL = "{SFG.PROD.PROTOCOL}://{SFG.PROD.HOST_NAME}{:SFG.PROD.PORT}/{SFG.PROD.CONTEXT_URI}/";

	/*
	 * Internally tool will replace the values for the parameters and constructs the
	 * URL PR instance.
	 */
	private static final String PR_API_URL = "{PROTOCOL}://{PR.HOST_NAME}{:PR.PORT}/mdrws/sponsors/{CONTEXT_URI}/";

	/*
	 * Internally tool will replace the values for the parameters and constructs the
	 * URL TEST SFG instance.
	 */
	private static final String SFG_TEST_API_URL = "{SFG.TEST.PROTOCOL}://{SFG.TEST.HOST_NAME}{:SFG.TEST.PORT}/{SFG.TEST.CONTEXT_URI}/";

	private String protocol;
	private String contextURI;

	private String userName;
	private char password[];

	private String prHostName;
	private String prPort;

	private String prRestURL;
	private String prUserName;
	private char prPassword[];

	private String sfgProdHost;
	private String sfgProdPort;
	private String sfgProdProtocol;
	private String sfgProdContextURI;

	private String sfgTestHost;
	private String sfgTestPort;
	private String sfgTestProtocol;
	private String sfgTestContextURI;

	private String sfgProdRestURL;
	private String sfgProdUserName;
	private char sfgProdPassword[];

	private String sfgTestRestURL;
	private String sfgTestUserName;
	private char sfgTestPassword[];

	private String proxyHost;
	private String proxyPort;

	private String proxyUsername;
	private char proxyPassword[];

	private String mode;
	private String delimiter;
	private Properties props;

	private String installDirectory;
	private File outputFile;
	private String outputFileDir;

	private String prodSFGPartnerDataFileName;
	private String testSFGPartnerDataFileName;
	private File testOutPutFile;
	private File prodOutPutFile;
	private File pemPartnerBulkUploadInputFile;
	private String pemPartnerBulkUploadInputFileName;
	private String pemPartnerBulkUploadOutputFileName;

	public void load(String configFile) throws Exception {
		loadProps(configFile);

		mode = getValue("MODE", true);
		validateMode();

		protocol = getValue("PROTOCOL", true);
		contextURI = getValue("CONTEXT_URI", true);

//		port = getValue("PORT", false);
//		hostName = getValue("HOST_NAME", true);

		prPort = getValue("PR.PORT", false);
		prHostName = getValue("PR.HOST_NAME", true);

		sfgProdProtocol = getValue("SFG.PROD.PROTOCOL", true);
		sfgProdPort = getValue("SFG.PROD.PORT", false);
		sfgProdHost = getValue("SFG.PROD.HOST_NAME", true);
		sfgProdContextURI = getValue("SFG.PROD.CONTEXT_URI", true);

		sfgTestProtocol = getValue("SFG.TEST.PROTOCOL", true);
		sfgTestPort = getValue("SFG.TEST.PORT", false);
		sfgTestHost = getValue("SFG.TEST.HOST_NAME", true);
		sfgTestContextURI = getValue("SFG.TEST.CONTEXT_URI", true);

		sfgProdRestURL = buildSFGProdUrl(SFG_PROD_API_URL);
		sfgTestRestURL = buildSFGTestUrl(SFG_TEST_API_URL);
		prRestURL = buildPRUrl(PR_API_URL);

		proxyHost = getValue("PROXY_HOST", false);
		proxyPort = getValue("PROXY_PORT", false);

		// console input should be disabled only during development.
		String disableConsole = getValue("disableConsole", false);
		boolean enableConsole = !Boolean.parseBoolean(disableConsole != null ? disableConsole : "false");
		Console console = null;
		if (enableConsole) {
			console = System.console();
			if (console == null) {
				throw new ValidationException("Unable to get the system console.");
			}

			if (mode.equals(Constants.MODE_IMPORT_SFG_PARTNER_DATA_TO_PEM)) {
				userName = console.readLine("Enter your username: ");
				password = console.readPassword("Enter your password: ");
			}

			if (mode.equals(Constants.MODE_EXTRACT_SFG_PARTNER_DATA)
					|| mode.equals(Constants.MODE_IMPORT_SFG_PARTNER_DATA_TO_PEM)) {
				sfgProdUserName = console.readLine("Enter your SFG PROD instance username: ");
				sfgProdPassword = console.readPassword("Enter your SFG PROD instance password: ");

				sfgTestUserName = console.readLine("Enter your SFG TEST instance username: ");
				sfgTestPassword = console.readPassword("Enter your SFG TEST instance password: ");
			}

		} else {
			userName = getValue("USERNAME", true);
			password = getValue("PASSWORD", true).toCharArray();
			sfgProdUserName = getValue("SFG.PROD.USERNAME", true);
			sfgProdPassword = getValue("SFG.PROD.PASSWORD", true).toCharArray();
			sfgTestUserName = getValue("SFG.TEST.USERNAME", true);
			sfgTestPassword = getValue("SFG.TEST.PASSWORD", true).toCharArray();
		}
		if (getValue("ProxyAuthentication", false).equalsIgnoreCase("true") && proxyHost != null
				&& proxyHost.length() > 0 && proxyPort != null) {
			if (enableConsole) {
				proxyUsername = console.readLine("Enter your proxy username: ");
				proxyPassword = console.readPassword("Enter your proxy password: ");
			} else {
				throw new UnsupportedOperationException("Not implemented yet!");
			}
		}

		delimiter = getValue("DELIMITER", true);
		validateDelimiter();

		outputFileDir = getValue("Output_File_Directory", true);

		if (getMode().equals(Constants.MODE_IMPORT_SFG_PARTNER_DATA_TO_PEM)) {
			pemPartnerBulkUploadOutputFileName = getValue("ImportSFGPartnerDataToPEM.input.pemPartnerBulkUploadFile",
					false);
			pemPartnerBulkUploadInputFileName = getValue("ImportSFGPartnerDataToPEM.output.pemPartnerBulkUploadFile",
					true);
			validateCsvFileExtension(pemPartnerBulkUploadInputFileName);
			if (pemPartnerBulkUploadOutputFileName != null && !"".equals(pemPartnerBulkUploadOutputFileName)) {
				validateCsvFileExtension(pemPartnerBulkUploadOutputFileName);
			}
		}

		if (getMode().equals(Constants.MODE_EXTRACT_SFG_PARTNER_DATA)
				|| getMode().equals(Constants.MODE_IMPORT_SFG_PARTNER_DATA_TO_PEM)) {
			prodSFGPartnerDataFileName = getValue("SFGToPEMMigration.SFGPartnerDataFileName.Prod", true);
			validateCsvFileExtension(prodSFGPartnerDataFileName);

			testSFGPartnerDataFileName = getValue("SFGToPEMMigration.SFGPartnerDataFileName.Test", true);
			validateCsvFileExtension(testSFGPartnerDataFileName);
		}

		if (getMode().equals(Constants.MODE_EXTRACT_SFG_PARTNER_DATA)) {
			prodOutPutFile = CreateFileUtil.createFile(prodSFGPartnerDataFileName, outputFileDir, getMode());
			testOutPutFile = CreateFileUtil.createFile(testSFGPartnerDataFileName, outputFileDir, getMode());
		} else if (getMode().equals(Constants.MODE_IMPORT_SFG_PARTNER_DATA_TO_PEM)) {
			prodOutPutFile = CreateFileUtil.createFile(prodSFGPartnerDataFileName, outputFileDir, getMode());
			pemPartnerBulkUploadInputFile = new File(outputFileDir + "/" + pemPartnerBulkUploadInputFileName);
			if (!pemPartnerBulkUploadInputFile.exists()) {
				pemPartnerBulkUploadInputFile.createNewFile();
			}
		}
	}

	private void validateMode() throws ValidationException {
		if (mode == null || mode.isEmpty()) {
			throw new ValidationException("Please provide a valid value for property: MODE");
		}
		if (!mode.equals(Constants.MODE_IMPORT_SFG_PARTNER_DATA_TO_PEM)
				&& !mode.equals(Constants.MODE_EXTRACT_SFG_PARTNER_DATA)) {
			throw new ValidationException("Please provide a valid value for property: MODE");
		}
	}

	private void validateDelimiter() throws ValidationException {
		if (!this.delimiter.equals("~") && !delimiter.equals("|")) {
			throw new ValidationException("Delimiter should be either ~ or |");
		}
	}

	private void validateCsvFileExtension(String fileName) throws ValidationException {
		if (!FilenameUtils.getExtension(fileName).equals("csv")) {
			throw new ValidationException("Please Upload only CSV File....");
		}
	}

	private void loadProps(String configFile) throws FileNotFoundException, IOException {
		props = new Properties();
		FileInputStream is = null;
		try {
			File file = new File(configFile);
			is = new FileInputStream(file);
			installDirectory = new File(file.getAbsolutePath()).getParent();
			if (installDirectory == null) {
				installDirectory = ".";
			}
			props.load(is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public String getProdSfgFilePath() {
		return outputFileDir + "/" + prodSFGPartnerDataFileName;
	}

	public String getTestSfgFilePath() {
		return outputFileDir + "/" + testSFGPartnerDataFileName;
	}

	public String getPartnerBulkUploadOutputFilePath() {
		return outputFileDir + "/" + pemPartnerBulkUploadOutputFileName;
	}

	public String buildPRUrl(String url) {
		String prPortStr = (prPort == null || prPort.isEmpty()) ? "" : (":" + prPort);
		return url.replace("{PROTOCOL}", protocol).replace("{PR.HOST_NAME}", prHostName)
				.replace("{:PR.PORT}", prPortStr).replace("{CONTEXT_URI}", contextURI);
	}

	public String buildSFGProdUrl(String url) {
		String sfgPortStr = (sfgProdPort == null || sfgProdPort.isEmpty()) ? "" : (":" + sfgProdPort);
		return url.replace("{SFG.PROD.PROTOCOL}", sfgProdProtocol).replace("{SFG.PROD.HOST_NAME}", sfgProdHost)
				.replace("{:SFG.PROD.PORT}", sfgPortStr).replace("{SFG.PROD.CONTEXT_URI}", sfgProdContextURI);
	}

	public String buildSFGTestUrl(String url) {
		String sfgPortStr = (sfgTestPort == null || sfgTestPort.isEmpty()) ? "" : (":" + sfgTestPort);
		return url.replace("{SFG.TEST.PROTOCOL}", sfgTestProtocol).replace("{SFG.TEST.HOST_NAME}", sfgTestHost)
				.replace("{:SFG.TEST.PORT}", sfgPortStr).replace("{SFG.TEST.CONTEXT_URI}", sfgTestContextURI);
	}

	private String getValue(String key) {
		String value = this.props.getProperty(key);
		if (value != null && !value.isEmpty()) {
			value = value.trim();
		}
		return value;
	}

	private String getValue(String key, boolean isMandatory) throws ValidationException {
		String valueFromConfigFile = getValue(key);
		if ((valueFromConfigFile == null || valueFromConfigFile.isEmpty()) && isMandatory) {
			throw new ValidationException("Provide a valid non-empty value for property: " + key);
		}
		return valueFromConfigFile;
	}

	public int getConnectTimeout() throws ValidationException {
		return Integer.parseInt(getValue("httpclient.connectionTimeout", true));
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getContextURI() {
		return contextURI;
	}

	public void setContextURI(String contextURI) {
		this.contextURI = contextURI;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public char[] getPassword() {
		return password;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}

	public String getPrHostName() {
		return prHostName;
	}

	public void setPrHostName(String prHostName) {
		this.prHostName = prHostName;
	}

	public String getPrPort() {
		return prPort;
	}

	public void setPrPort(String prPort) {
		this.prPort = prPort;
	}

	public String getPrRestURL() {
		return prRestURL;
	}

	public void setPrRestURL(String prRestURL) {
		this.prRestURL = prRestURL;
	}

	public String getPrUserName() {
		return prUserName;
	}

	public void setPrUserName(String prUserName) {
		this.prUserName = prUserName;
	}

	public char[] getPrPassword() {
		return prPassword;
	}

	public void setPrPassword(char[] prPassword) {
		this.prPassword = prPassword;
	}

	public String getSfgProdHost() {
		return sfgProdHost;
	}

	public void setSfgProdHost(String sfgProdHost) {
		this.sfgProdHost = sfgProdHost;
	}

	public String getSfgProdPort() {
		return sfgProdPort;
	}

	public void setSfgProdPort(String sfgProdPort) {
		this.sfgProdPort = sfgProdPort;
	}

	public String getSfgProdProtocol() {
		return sfgProdProtocol;
	}

	public void setSfgProdProtocol(String sfgProdProtocol) {
		this.sfgProdProtocol = sfgProdProtocol;
	}

	public String getSfgProdContextURI() {
		return sfgProdContextURI;
	}

	public void setSfgProdContextURI(String sfgProdContextURI) {
		this.sfgProdContextURI = sfgProdContextURI;
	}

	public String getSfgTestHost() {
		return sfgTestHost;
	}

	public void setSfgTestHost(String sfgTestHost) {
		this.sfgTestHost = sfgTestHost;
	}

	public String getSfgTestPort() {
		return sfgTestPort;
	}

	public void setSfgTestPort(String sfgTestPort) {
		this.sfgTestPort = sfgTestPort;
	}

	public String getSfgTestProtocol() {
		return sfgTestProtocol;
	}

	public void setSfgTestProtocol(String sfgTestProtocol) {
		this.sfgTestProtocol = sfgTestProtocol;
	}

	public String getSfgTestContextURI() {
		return sfgTestContextURI;
	}

	public void setSfgTestContextURI(String sfgTestContextURI) {
		this.sfgTestContextURI = sfgTestContextURI;
	}

	public String getSfgProdRestURL() {
		return sfgProdRestURL;
	}

	public void setSfgProdRestURL(String sfgProdRestURL) {
		this.sfgProdRestURL = sfgProdRestURL;
	}

	public String getSfgProdUserName() {
		return sfgProdUserName;
	}

	public void setSfgProdUserName(String sfgProdUserName) {
		this.sfgProdUserName = sfgProdUserName;
	}

	public char[] getSfgProdPassword() {
		return sfgProdPassword;
	}

	public void setSfgProdPassword(char[] sfgProdPassword) {
		this.sfgProdPassword = sfgProdPassword;
	}

	public String getSfgTestRestURL() {
		return sfgTestRestURL;
	}

	public void setSfgTestRestURL(String sfgTestRestURL) {
		this.sfgTestRestURL = sfgTestRestURL;
	}

	public String getSfgTestUserName() {
		return sfgTestUserName;
	}

	public void setSfgTestUserName(String sfgTestUserName) {
		this.sfgTestUserName = sfgTestUserName;
	}

	public char[] getSfgTestPassword() {
		return sfgTestPassword;
	}

	public void setSfgTestPassword(char[] sfgTestPassword) {
		this.sfgTestPassword = sfgTestPassword;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}

	public char[] getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(char[] proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	public String getInstallDirectory() {
		return installDirectory;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public String getOutputFileDir() {
		return outputFileDir;
	}

	public void setOutputFileDir(String outputFileDir) {
		this.outputFileDir = outputFileDir;
	}

	public File getTestOutPutFile() {
		return testOutPutFile;
	}

	public void setTestOutPutFile(File testOutPutFile) {
		this.testOutPutFile = testOutPutFile;
	}

	public File getProdOutPutFile() {
		return prodOutPutFile;
	}

	public void setProdOutPutFile(File prodOutPutFile) {
		this.prodOutPutFile = prodOutPutFile;
	}

	public File getPemPartnerBulkUploadInputFile() {
		return pemPartnerBulkUploadInputFile;
	}

	public String getPemPartnerBulkUploadOutputFileName() {
		return pemPartnerBulkUploadOutputFileName;
	}

}
