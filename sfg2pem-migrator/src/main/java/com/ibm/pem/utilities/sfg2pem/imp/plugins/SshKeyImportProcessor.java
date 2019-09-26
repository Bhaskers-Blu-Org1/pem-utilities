/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp.plugins;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ApiInvocationException;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo;
import com.ibm.pem.utilities.sfg2pem.imp.PrConfigurationProcessor;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.SftpConfigImportProcessor.SftpConfigInfo;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.ManagedSshkeysXSLT;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SftpResourceHelper;

public class SshKeyImportProcessor extends PrConfigurationProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(SshKeyImportProcessor.class);

	private static final String GET_MANAGED_SSH_KEY_DATA_URL = "svc/sshknownhostkeys/";
	private static final String CREATE_MANAGED_SSH_KEY_API_URL = "{PROTOCOL}://{PR.HOST_NAME}{:PR.PORT}/mdrws/sponsors/{CONTEXT_URI}/managedsshkeys/";

	private SftpConfigInfo configInfo;

	private String getKeyData(boolean getFromProductionSfg)
			throws ApiInvocationException, ImportException, ParserConfigurationException, SAXException, IOException {
		Document sshRemoteProfileData = getFromProductionSfg ? configInfo.getProdRemoteProfileDoc()
				: configInfo.getTestRemoteProfileDoc();
		String knownHostKey = SftpResourceHelper.getAttributeValueByTagName(sshRemoteProfileData, "KnownHostKeyName",
				"name");
		String knownHostData = SftpResourceHelper.getResourceFromSFG(getConfig(), getFromProductionSfg, GET_MANAGED_SSH_KEY_DATA_URL, knownHostKey)
				.getResponse();
		return SftpResourceHelper.getAttributeValueByTagName(knownHostData, "SSHKnownHostKey", "keyData");
	}

	private PartnerInfo partnerInfo;
	
	public SshKeyImportProcessor(PartnerInfo partnerInfo, Configuration config, SftpConfigInfo configInfo) {
		super(partnerInfo, config);
		this.configInfo = configInfo;
		this.partnerInfo = partnerInfo;
	}

	@Override
	protected String getResourceType() {
		return "SSH_KEY";
	}

	@Override
	protected String getSubResourceType() {
		return "SSH_KEY";
	}

	@Override
	protected String getResourceNamePostfix() {
		return "SSH_KEY";
	}

	@Override
	protected String getConfigResourceUri() {
		return "managedsshkeys/";
	}

	protected String getKeyType() {
		return "KNOWN_HOST_KEYS";
	}

	@Override
	protected String getConfigResource(String profileConfigKey) throws ImportException, ApiInvocationException {
		String url = getConfig().buildPRUrl(CREATE_MANAGED_SSH_KEY_API_URL) + String
				.format("?configurationId=%s", profileConfigKey);	
		return SftpResourceHelper.getResourceFromPR(getConfig(), url).getResponse();
	}

	@Override
	protected String createConfigResource(String profileConfigKey) throws ImportException {
		try {
			String requestXml = SftpResourceHelper.createXml(ManagedSshkeysXSLT.createManagedSshkeys(getConfig(),
					profileConfigKey, getKeyData(true), getKeyData(false), getKeyType()));
			return SftpResourceHelper.callCreateApi(getConfig(), requestXml, getConfig().buildPRUrl(CREATE_MANAGED_SSH_KEY_API_URL), "managedSshKeyKey");
		} catch (TransformerException | IOException | ParserConfigurationException | SAXException
				| ApiInvocationException e) {
			throw new ImportException(e);
		}
	}

	@Override
	protected String getResourceKeyAttributeName() {
		return "managedSshKeyKey";
	}

	@Override
	protected String getResourceKeyTagName() {
		return "ManagedSshKey";
	}

	@Override
	protected String getProfileConfigKey() throws ImportException {
		if (!partnerInfo.isRemoteProfileExists()) {
			if (partnerInfo.getHostIdentityKeyProfileConfig() != null
					& !partnerInfo.getHostIdentityKeyProfileConfig().trim().isEmpty()) {
				return partnerInfo.getHostIdentityKeyProfileConfig();
			} else {
				if (LOG.isInfoEnabled()) {
					String message = "Profile Configuration for " + getSubResourceType()
							+ " is not available as part of tool";
					LOG.info(message);
					throw new ImportException(message);
				}
			}
		}
		return null;
	}
}
