/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp.plugins;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ApiInvocationException;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo;
import com.ibm.pem.utilities.sfg2pem.imp.PrConfigurationProcessor;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SftpResourceHelper;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SftpXSLT;

public abstract class SftpConfigImportProcessor extends PrConfigurationProcessor {

	public static class SftpConfigInfo {

		private Document prodRemoteProfileDoc;
		private Document testRemoteProfileDoc;
		private String managedSshKeyProfileConfigKey;
		private String userIdentityKeyProfileConfigKey;
		private String userCredentialProfileConfigKey;

		public Document getProdRemoteProfileDoc() {
			return prodRemoteProfileDoc;
		}

		public SftpConfigInfo setProdRemoteProfileDoc(Document prodSshRemoteProfileData) {
			this.prodRemoteProfileDoc = prodSshRemoteProfileData;
			return this;
		}

		public Document getTestRemoteProfileDoc() {
			return testRemoteProfileDoc;
		}

		public SftpConfigInfo setTestRemoteProfileDoc(Document testRemoteProfileData) {
			this.testRemoteProfileDoc = testRemoteProfileData;
			return this;
		}

		public String getManagedSshKeyProfileConfigKey() {
			return managedSshKeyProfileConfigKey;
		}

		public SftpConfigInfo setManagedSshKeyProfileConfigKey(String managedSshKeyProfileConfigKey) {
			this.managedSshKeyProfileConfigKey = managedSshKeyProfileConfigKey;
			return this;
		}

		public String getUserIdentityKeyProfileConfigKey() {
			return userIdentityKeyProfileConfigKey;
		}

		public SftpConfigInfo setUserIdentityKeyProfileConfigKey(String userIdentityKeyProfileConfigKey) {
			this.userIdentityKeyProfileConfigKey = userIdentityKeyProfileConfigKey;
			return this;
		}

		public String getUserCredentialProfileConfigKey() {
			return userCredentialProfileConfigKey;
		}

		public SftpConfigInfo setUserCredentialProfileConfigKey(String userCredentialProfileConfigKey) {
			this.userCredentialProfileConfigKey = userCredentialProfileConfigKey;
			return this;
		}

	}

	private SftpConfigInfo configInfo;

	public SftpConfigImportProcessor(PartnerInfo partnerInfo, Configuration config, SftpConfigInfo configInfo) {
		super(partnerInfo, config);
		this.configInfo = configInfo;
	}

	@Override
	protected String getResourceType() {
		return "SFTP";
	}

	@Override
	protected String getConfigResource(String profileConfigKey) throws ImportException, ApiInvocationException {
		return null;
	}

	@Override
	protected String createConfigResource(String profileConfigKey) throws ImportException {
		try {
			Document sftpXML = new SftpXSLT().createSFTP(getConfig(), configInfo.getProdRemoteProfileDoc(),
					configInfo.getTestRemoteProfileDoc(), profileConfigKey,
					configInfo.getManagedSshKeyProfileConfigKey(), configInfo.getUserIdentityKeyProfileConfigKey(),
					configInfo.getUserCredentialProfileConfigKey(), getSubResourceType());
			String requestXml = SftpResourceHelper.createXml(sftpXML);
			return SftpResourceHelper.callCreateApi(getConfig(), requestXml, getConfig().buildPRUrl(getConfigResourceURL()),
					getResourceKeyAttributeName());
		} catch (TransformerException | IOException | ParserConfigurationException | SAXException
				| ApiInvocationException e) {
			throw new ImportException(e);
		}
	}

	protected abstract String getConfigResourceURL();

}
