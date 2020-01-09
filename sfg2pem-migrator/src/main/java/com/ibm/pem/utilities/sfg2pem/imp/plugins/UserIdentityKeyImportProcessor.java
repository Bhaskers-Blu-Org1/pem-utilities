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
import com.ibm.pem.utilities.sfg2pem.Constants;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo;
import com.ibm.pem.utilities.sfg2pem.imp.PrConfigurationProcessor;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.SftpConfigImportProcessor.SftpConfigInfo;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.ProfileConfigurationXSLT;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SftpResourceHelper;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.UserIdentityKeysXSLT;
import com.ibm.pem.utilities.util.DOMUtils;

/**
 * 
 * @author ManojKumar
 *
 *         Class to process the data for UserIdentityKey in SSHRemoteProfile.
 *         Public part of UserIdentityKey would be checked to PR as Authorized
 *         user Key.
 */
public class UserIdentityKeyImportProcessor extends PrConfigurationProcessor {

	private SftpConfigInfo configInfo;
	private static final String GET_MANAGED_USERIDENTITY_KEY_DATA_URL = "svc/sshuseridentitykeys/";
	private static final String CREATE_MANAGED_SSH_KEY_API_URL = "{PROTOCOL}://{PR.HOST_NAME}{:PR.PORT}/mdrws/sponsors/{CONTEXT_URI}/managedsshkeys/";

	public String getKeyData(boolean getFromProductionSfg)
			throws ApiInvocationException, ImportException, ParserConfigurationException, SAXException, IOException {
		Document sshRemoteProfileData = getFromProductionSfg ? configInfo.getProdRemoteProfileDoc()
				: configInfo.getTestRemoteProfileDoc();
		String userIdentityKey = DOMUtils.getAttributeValueByTagName(sshRemoteProfileData, "SSHRemoteProfile",
				"userIdentityKey");
		String userIdentityKeyData = SftpResourceHelper.getResourceFromSFG(getConfig(), getFromProductionSfg,
				GET_MANAGED_USERIDENTITY_KEY_DATA_URL, userIdentityKey).getResponse();
		// get public part of the key
		return DOMUtils.getAttributeValueByTagName(userIdentityKeyData, "SSHUserIdentityKey", "publicKeyData");
	}

	public String getKeyName(boolean getFromProductionSfg) {
		Document sshRemoteProfileData = getFromProductionSfg ? configInfo.getProdRemoteProfileDoc()
				: configInfo.getTestRemoteProfileDoc();
		String userIdentityKey = DOMUtils.getAttributeValueByTagName(sshRemoteProfileData, "SSHRemoteProfile",
				"userIdentityKey");
		return userIdentityKey;
	}

	public UserIdentityKeyImportProcessor(PartnerInfo partnerInfo, Configuration config, SftpConfigInfo configInfo) {
		super(partnerInfo, config);
		this.configInfo = configInfo;
	}

	@Override
	protected String getResourceNamePostfix() {
		return "UserIdentityKey";
	}

	protected String getKeyType() {
		// Key will be imported as Authorized User Key
		return "AUTHORIZED_USER_KEYS";
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
	protected String getConfigResourceUri() {
		return "managedsshkeys/";
	}

	@Override
	protected String getConfigResource(String profileConfigKey) throws ImportException, ApiInvocationException {
		String url = getConfig().buildPRUrl(CREATE_MANAGED_SSH_KEY_API_URL)
				+ String.format("?configurationId=%s", profileConfigKey);
		return SftpResourceHelper.getResourceFromPR(getConfig(), url).getResponse();
	}

	@Override
	protected String createProfileConfiguration(String resourceType, String subResourceType, String resourceName)
			throws ApiInvocationException, ImportException {
		// validate whether SG profile configuration provided exists
		validateSGProfileConfiguration(getPartnerInfo().getAuthUserKeySGProfileConfig(), resourceType, subResourceType);
		String configurationId = null;
		try {
			String requestXML = DOMUtils.createXml(new ProfileConfigurationXSLT().createProfileConfiguration(
					getConfig(), resourceName, resourceType, subResourceType, Constants.SERVER_TYPE_DG,
					getPartnerInfo().getPemPartnerKey(), getPartnerInfo().getSponsorDivisionKey(),
					getPartnerInfo().getAuthUserKeySGProfileConfig()));
			String createProfileConfigRequest = requestXML;

			configurationId = SftpResourceHelper.callCreateApi(getConfig(), createProfileConfigRequest,
					getConfig().buildPRUrl(CREATE_PROFILECONFIGURATION_API_URL), "profileConfigurationKey");
		} catch (TransformerException | IOException | ParserConfigurationException | SAXException e) {
			throw new ImportException(e);
		}
		return configurationId;
	}

	@Override
	protected String createConfigResource(String profileConfigKey) throws ImportException {
		try {
			String requestXml = DOMUtils
					.createXml(UserIdentityKeysXSLT.createManagedSshkeys(getConfig(), profileConfigKey,
							getKeyData(true), getKeyData(false), getKeyType(), getKeyName(true), getKeyName(false)));
			return SftpResourceHelper.callCreateApi(getConfig(), requestXml,
					getConfig().buildPRUrl(CREATE_MANAGED_SSH_KEY_API_URL), getResourceKeyAttributeName());
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
	protected String getProfileConfigurationURL(String resourceType, String subResourceType, String resourceName) {
		return getConfig().buildPRUrl(CREATE_PROFILECONFIGURATION_API_URL)
				+ String.format("?serverType=%s&resourceType=%s&subResourceType=%s&name=%s", Constants.SERVER_TYPE_DG,
						resourceType, subResourceType, resourceName);
	}

	private void validateSGProfileConfiguration(String sgProfileConfigId, String resourceType, String subResourceType)
			throws ApiInvocationException, ImportException {
		if (sgProfileConfigId == null) {
			throw new ImportException(
					"AuthorizedUserKey Profile Configuration for " + getSubResourceType() + " is not specified.");
		}
		try {
			String url = getConfig().buildPRUrl(CREATE_PROFILECONFIGURATION_API_URL)
					+ String.format("%s", sgProfileConfigId);
			String apiResponse = SftpResourceHelper.getResourceFromPR(getConfig(), url).getResponse();
			String profileResourceType = DOMUtils.getAttributeValueByTagName(apiResponse, "resourceType", "code");
			String profileSubResourceType = DOMUtils.getAttributeValueByTagName(apiResponse, "subResourceType", "code");
			String profileServerType = DOMUtils.getAttributeValueByTagName(apiResponse, "serverType", "code");
			if (!Constants.SERVER_TYPE_SG.equals(profileServerType) || !resourceType.equals(profileResourceType)
					|| !subResourceType.equals(profileSubResourceType)) {
				throw new ImportException(
						"AuthorizedUserKey Profile Configuration for " + getSubResourceType() + " must be of type SG.");
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ImportException(e);
		}
	}
}
