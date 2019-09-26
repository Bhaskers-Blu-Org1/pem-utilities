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
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.ManagedSshkeysXSLT;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SftpResourceHelper;

public class SshAuthUserkeyImportProcess extends PrConfigurationProcessor {

	private static final String GET_MANAGED_SSH_KEY_DATA_URL = "svc/sshauthorizeduserkeys/";
	private static final String CREATE_MANAGED_SSH_KEY_API_URL = "{PROTOCOL}://{PR.HOST_NAME}{:PR.PORT}/mdrws/sponsors/{CONTEXT_URI}/managedsshkeys/";

	private String getKeyData(boolean getFromProductionSfg)
			throws ApiInvocationException, ImportException, ParserConfigurationException, SAXException, IOException {
		Document sshRemoteProfileData = getFromProductionSfg ? getPartnerInfo().getProdSfgPartnerDoc()
				: getPartnerInfo().getTestSfgPartnerDoc();
		String knownHostKey = SftpResourceHelper.getAttributeValueByTagName(sshRemoteProfileData, "TradingPartner",
				"authorizedUserKeyName");
		String knownHostData = SftpResourceHelper.getResourceFromSFG(getConfig(), getFromProductionSfg, GET_MANAGED_SSH_KEY_DATA_URL, knownHostKey)
				.getResponse();
		return SftpResourceHelper.getAttributeValueByTagName(knownHostData, "SSHAuthorizedUserKey", "keyData");
	}

	public SshAuthUserkeyImportProcess(PartnerInfo partnerInfo, Configuration config) {
		super(partnerInfo, config);
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
		return "AuthorizedUserKey";
	}

	@Override
	protected String getConfigResourceUri() {
		return "managedsshkeys/";
	}

	protected String getKeyType() {
		return "AUTHORIZED_USER_KEYS";
	}

	@Override
	protected String getConfigResource(String profileConfigKey) throws ImportException, ApiInvocationException {
		String url = getConfig().buildPRUrl(CREATE_MANAGED_SSH_KEY_API_URL) + String
				.format("?partner=%s&configurationId=%s", getPartnerInfo().getPemPartnerKey(), profileConfigKey);
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
}
