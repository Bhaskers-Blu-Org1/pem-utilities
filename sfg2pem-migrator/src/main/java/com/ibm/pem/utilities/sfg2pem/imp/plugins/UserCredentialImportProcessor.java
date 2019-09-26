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
import com.ibm.pem.utilities.sfg2pem.imp.plugins.SftpConfigImportProcessor.SftpConfigInfo;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.AssociateAuthUserToUserCredXSLT;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SftpResourceHelper;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.UserCredentialXSLT;
import com.ibm.pem.utilities.util.ApiResponse;

public class UserCredentialImportProcessor extends PrConfigurationProcessor {

	private static final String CRETAE_MANAGED_USERCREDENTIAL_API_URL = "{PROTOCOL}://{PR.HOST_NAME}{:PR.PORT}/mdrws/sponsors/{CONTEXT_URI}/managedusercredentials/";

	private SftpConfigInfo sftpConfigInfo;
	private PartnerInfo partnerInfo;

	public UserCredentialImportProcessor(PartnerInfo partnerInfo, Configuration config, SftpConfigInfo configInfo) {
		super(partnerInfo, config);
		this.sftpConfigInfo = configInfo;
		this.partnerInfo = partnerInfo;
		this.sftpConfigInfo = configInfo;
	}

	@Override
	protected String getResourceType() {
		return "USER_CRED";
	}

	@Override
	protected String getSubResourceType() {
		return "USER_CRED";
	}

	@Override
	protected String getResourceNamePostfix() {
		return "UserCred";
	}

	@Override
	protected String getConfigResource(String profileConfigKey) throws ImportException, ApiInvocationException {
		String url = getConfig().buildPRUrl(CRETAE_MANAGED_USERCREDENTIAL_API_URL) + String
				.format("?partner=%s&configurationId=%s", getPartnerInfo().getPemPartnerKey(), profileConfigKey);
		return SftpResourceHelper.getResourceFromPR(getConfig(), url).getResponse();
	}

	@Override
	protected String createConfigResource(String profileConfigKey) throws ImportException {
		try {
			String getProdUserName;
			String getTestUserName;
			if (!partnerInfo.isRemoteProfileExists()) {
				getProdUserName = getUserName(true);
				getTestUserName = getUserName(false);
			} else {
				getProdUserName = getRemoteUserName(sftpConfigInfo.getProdRemoteProfileDoc());
				getTestUserName = getRemoteUserName(sftpConfigInfo.getTestRemoteProfileDoc());
			}

			String requestXml = SftpResourceHelper.createXml(new UserCredentialXSLT().createUserCerdential(getConfig(),
					profileConfigKey, getProdUserName, getTestUserName));
			return SftpResourceHelper.callCreateApi(getConfig(), requestXml,
					getConfig().buildPRUrl(CRETAE_MANAGED_USERCREDENTIAL_API_URL), "managedUserCredentialKey");
		} catch (TransformerException | IOException | ParserConfigurationException | SAXException
				| ApiInvocationException e) {
			throw new ImportException(e);
		}
	}

	private String getRemoteUserName(Document doc) {
		return SftpResourceHelper.getAttributeValueByTagName(doc, "SSHRemoteProfile", "remoteUser");
	}

	private String getUserName(boolean isProd) {
		Document sshRemoteProfileData = isProd ? getPartnerInfo().getProdSfgPartnerDoc()
				: getPartnerInfo().getTestSfgPartnerDoc();
		return SftpResourceHelper.getAttributeValueByTagName(sshRemoteProfileData, "TradingPartner", "username");
	}

	@Override
	protected String getConfigResourceUri() {
		return "managedusercredentials/";
	}

	@Override
	protected String getResourceKeyAttributeName() {
		return "managedUserCredentialKey";
	}

	@Override
	protected String getResourceKeyTagName() {
		return "ManagedUserCredential";
	}

	protected void associateAuthUserKeytoUserCred(String profileConfigKey) throws ImportException {
		if (!partnerInfo.isRemoteProfileExists() && SftpResourceHelper.doesPartnerProfileHasAUK(partnerInfo)) {
			try {
				String apiURL = "{PROTOCOL}://{PR.HOST_NAME}{:PR.PORT}/mdrws/sponsors/{CONTEXT_URI}/"
						+ "profileconfigurations/" + profileConfigKey + "/userCredSshKeys/";
				if (getAssignedKey(profileConfigKey, apiURL) == null) {
					String requestXml = SftpResourceHelper
							.createXml(AssociateAuthUserToUserCredXSLT.associateAuthUserToUserCred(getConfig(),
									partnerInfo.getSshAuthorizedUserkeyProfileConfigKey()));
					SftpResourceHelper.callCreateApi(getConfig(), requestXml, getConfig().buildPRUrl(apiURL),
							"managedUserCredentialKey");
				}

			} catch (ApiInvocationException | TransformerException | IOException | ParserConfigurationException
					| SAXException e) {
				throw new ImportException(e);
			}
		}
	}

	private String getAssignedKey(String profileConfigKey, String apiURL) throws ImportException {
		try {
			String url = getConfig().buildPRUrl(apiURL + String.format("?profileConfigKey=%s", profileConfigKey));
			ApiResponse apiResponse = SftpResourceHelper.getResourceFromPR(getConfig(), url);
			return SftpResourceHelper.getAttributeValueByTagName(apiResponse.getResponse(), "managedSshKeyConfigId",
					"profileConfigurationKey");
		} catch (IOException | ParserConfigurationException | SAXException | ApiInvocationException e) {
			throw new ImportException(e);
		}
	}
}
