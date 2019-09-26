/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp.plugins;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ApiInvocationException;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.ValidationException;
import com.ibm.pem.utilities.sfg2pem.imp.ImportHelper;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo;
import com.ibm.pem.utilities.sfg2pem.imp.PrConfigurationImportHandler;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo.ProcessingStatus;
import com.ibm.pem.utilities.sfg2pem.imp.PrConfigurationProcessor.ConfigInfo;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.SftpConfigImportProcessor.SftpConfigInfo;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.SystemImportProcessor.SystemConfigInfo;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SftpResourceHelper;
import com.ibm.pem.utilities.util.ApiResponse;
import com.ibm.pem.utilities.util.HttpClientUtil;

public class SftpConfigImportHandler extends PrConfigurationImportHandler {

	private static final Logger LOG = LoggerFactory.getLogger(SftpConfigImportHandler.class);

	private static final String CODE = "code";

	private static final String ELEMENT_PRODUCER_SSH_CONFIGURATION = "producerSshConfiguration";
	private static final String ELEMENT_CONSUMER_SSH_CONFIGURATION = "consumerSshConfiguration";
	private static final String GET_SFTP_PROFILE_DATA_URL = "svc/sshremoteprofiles/";
	private static final String ELEMENT_INITIATING_PRODUCER_SSH_CONFIGURATION = "isInitiatingProducer";
	private static final String ELEMENT_INITIATING_CONSUMER_SSH_CONFIGURATION = "isInitiatingConsumer";
	private static final String SYSTEM_API_URL = "{PROTOCOL}://{PR.HOST_NAME}{:PR.PORT}/mdrws/sponsors/{CONTEXT_URI}/systems/";

	public SftpConfigImportHandler(Configuration config) {
		super(config);
	}

	@Override
	public boolean accept(Document prodSfgPartner, Document testSfgPartner) {
		if (hasSftpInboundPullConfig(prodSfgPartner, testSfgPartner)) {
			return true;
		} else if (hasSftpOutboundPushConfig(prodSfgPartner, testSfgPartner)) {
			return true;
		} else if (hasSftpInboundPushConfig(prodSfgPartner, testSfgPartner)) {
			return true;
		} else if (hasSftpOutboundPullConfig(prodSfgPartner, testSfgPartner)) {
			return true;
		}
		return false;
	}

	public static boolean hasSftpInboundPushConfig(Document prodSfgPartner, Document testSfgPartner) {
		return SftpResourceHelper.isInitiater(prodSfgPartner, ELEMENT_INITIATING_PRODUCER_SSH_CONFIGURATION)
				&& SftpResourceHelper.isInitiater(testSfgPartner, ELEMENT_INITIATING_PRODUCER_SSH_CONFIGURATION);
	}

	public static boolean hasSftpOutboundPullConfig(Document prodSfgPartner, Document testSfgPartner) {
		return SftpResourceHelper.isInitiater(prodSfgPartner, ELEMENT_INITIATING_CONSUMER_SSH_CONFIGURATION)
				&& SftpResourceHelper.isInitiater(testSfgPartner, ELEMENT_INITIATING_CONSUMER_SSH_CONFIGURATION);
	}

	public static boolean hasSftpOutboundPushConfig(Document prodSfgPartner, Document testSfgPartner) {
		return prodSfgPartner.getElementsByTagName(ELEMENT_CONSUMER_SSH_CONFIGURATION).getLength() > 0
				&& testSfgPartner.getElementsByTagName(ELEMENT_CONSUMER_SSH_CONFIGURATION).getLength() > 0;
	}

	public static boolean hasSftpInboundPullConfig(Document prodSfgPartner, Document testSfgPartner) {
		return prodSfgPartner.getElementsByTagName(ELEMENT_PRODUCER_SSH_CONFIGURATION).getLength() > 0
				&& testSfgPartner.getElementsByTagName(ELEMENT_PRODUCER_SSH_CONFIGURATION).getLength() > 0;
	}

	@Override
	protected void doExecute(PartnerInfo partnerInfo)
			throws ImportException, ApiInvocationException, ValidationException {
		SftpConfigInfo sftpConfigInfo = new SftpConfigInfo();

		Document prodSfgPartnerDoc = partnerInfo.getProdSfgPartnerDoc();
		Document testSfgPartnerDoc = partnerInfo.getTestSfgPartnerDoc();
		validateSystemConfiguration(partnerInfo);
		if (hasSftpInboundPullConfig(prodSfgPartnerDoc, testSfgPartnerDoc)
				|| hasSftpOutboundPushConfig(prodSfgPartnerDoc, testSfgPartnerDoc)) {
			try {
				Document prodSshRemoteProfileData = getSftpRemoteProfileData(true, prodSfgPartnerDoc);
				Document testSshRemoteProfileData = getSftpRemoteProfileData(false, testSfgPartnerDoc);
				sftpConfigInfo.setProdRemoteProfileDoc(prodSshRemoteProfileData);
				sftpConfigInfo.setTestRemoteProfileDoc(testSshRemoteProfileData);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				throw new ImportException(e);
			}

			String sshKeyProfileConfigKey = null;
			if (doesRemoteProfileHasKnownHostKey(sftpConfigInfo)) {
				sshKeyProfileConfigKey = importSshKey(partnerInfo, sftpConfigInfo);
			}

			String preferredAuthenticationType = getProdPreferredAuthenticationType(
					sftpConfigInfo.getProdRemoteProfileDoc());

			String userIdentityKeyProfileConfigKey = importUserIdentityKey(partnerInfo, sftpConfigInfo,
					preferredAuthenticationType);

			String userCredProfileConfigKey = null;
			if (doesRemoteProfileHasRemoteUser(sftpConfigInfo)) {
				userCredProfileConfigKey = importUserCredential(partnerInfo, sftpConfigInfo);
			}

			sftpConfigInfo.setManagedSshKeyProfileConfigKey(sshKeyProfileConfigKey)
					.setUserIdentityKeyProfileConfigKey(userIdentityKeyProfileConfigKey)
					.setUserCredentialProfileConfigKey(userCredProfileConfigKey);

			String sftpInbPullProfileConfigKey = null;
			String sftpOutPushProfileConfigKey = null;
			if (hasSftpInboundPullConfig(prodSfgPartnerDoc, testSfgPartnerDoc)) {
				sftpInbPullProfileConfigKey = importSftpInboundpull(partnerInfo, sftpConfigInfo);
			}

			if (hasSftpOutboundPushConfig(prodSfgPartnerDoc, testSfgPartnerDoc)) {
				sftpOutPushProfileConfigKey = importSftpOutBoundPush(partnerInfo, sftpConfigInfo);
			}

			importSystem(partnerInfo, sshKeyProfileConfigKey, userIdentityKeyProfileConfigKey, userCredProfileConfigKey,
					sftpInbPullProfileConfigKey, sftpOutPushProfileConfigKey);

		} else if (hasSftpInboundPushConfig(prodSfgPartnerDoc, testSfgPartnerDoc)
				|| hasSftpOutboundPullConfig(prodSfgPartnerDoc, testSfgPartnerDoc)) {
			if (!containsMultipleAUK(partnerInfo)) {
				partnerInfo.setRemoteProfileExists(false);

				String sshAuthUserKeyProfileConfigKey = null;
				if (SftpResourceHelper.doesPartnerProfileHasAUK(partnerInfo)) {
					sshAuthUserKeyProfileConfigKey = importSshAuthUserKey(partnerInfo);
					partnerInfo.setSshAuthorizedUserkeyProfileConfigKey(sshAuthUserKeyProfileConfigKey);
				}

				String userCredProfileConfigKey = importUserCredential(partnerInfo, sftpConfigInfo);
				sftpConfigInfo.setUserCredentialProfileConfigKey(userCredProfileConfigKey);

				String sftpOutPullProfileConfigKey = null;
				if (hasSftpOutboundPullConfig(prodSfgPartnerDoc, testSfgPartnerDoc)) {
					sftpOutPullProfileConfigKey = importSftpOutBoundPull(partnerInfo, sftpConfigInfo);
				}

				String sftpInbPushProfileConfigKey = null;
				if (hasSftpInboundPushConfig(prodSfgPartnerDoc, testSfgPartnerDoc)) {
					sftpInbPushProfileConfigKey = importSftpInboundpush(partnerInfo, sftpConfigInfo);
				}

				String hostIdentityKeyProfileConfig = importSshKey(partnerInfo, sftpConfigInfo);

				SystemImportProcessor systemImportProcessor = new SystemImportProcessor(partnerInfo, getConfig(),
						new SystemConfigInfo().setSshAuthUserKeyProfileConfigKey(sshAuthUserKeyProfileConfigKey)
								.setSftpInbPushProfileConfigKey(sftpInbPushProfileConfigKey)
								.setSftpOutPullProfileConfigKey(sftpOutPullProfileConfigKey)
								.setUserCredentialProfileConfigKey(userCredProfileConfigKey)
								.setHostIdentityKeyProfileConfig(hostIdentityKeyProfileConfig));

				systemImportProcessor.execute();

			}
		} else {
			throw new ImportException(
					"Failed to import as one or both of producer and consumer configurations are not available.");
		}
	}

	private boolean containsMultipleAUK(PartnerInfo partnerInfo)
			throws ApiInvocationException, ValidationException, ImportException {
		boolean containsMultipleAuk = true;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/xml");
		try {
			ApiResponse prodApiOutput = getProdAUK(partnerInfo.getProdSfgUserName(), headers);
			ApiResponse testApiOutput = getTestAUK(partnerInfo.getTestSfgUserName(), headers);

			boolean prodAuk = ImportHelper.buildDomDoc(prodApiOutput.getResponse())
					.getElementsByTagName("AuthorizedUserKeyName").getLength() > 1;
			boolean testAuk = ImportHelper.buildDomDoc(testApiOutput.getResponse())
					.getElementsByTagName("AuthorizedUserKeyName").getLength() > 1;
			if (prodAuk) {
				throw new ValidationException(
						"Contains multiple AUK for the sfg partner " + partnerInfo.getSfgPartnerKey());
			} else if (testAuk) {
				throw new ValidationException(
						"Contains multiple AUK for the sfg partner " + partnerInfo.getTestSfgPartnerKey());
			} else {
				containsMultipleAuk = false;
			}
		} catch (KeyManagementException | NoSuchAlgorithmException | CertificateException | KeyStoreException
				| HttpException | IOException | URISyntaxException | ParserConfigurationException | SAXException e) {
			throw new ImportException(e);
		}
		return containsMultipleAuk;
	}

	private ApiResponse getProdAUK(String sfgProdPartnerGivenName, Map<String, String> headers)
			throws HttpException, IOException, URISyntaxException, KeyManagementException, NoSuchAlgorithmException,
			CertificateException, KeyStoreException, ValidationException {
		String url = getConfig().getSfgProdRestURL();
		url += "svc/useraccounts/" + sfgProdPartnerGivenName;
		if (LOG.isInfoEnabled()) {
			LOG.info("Running API: GET " + url);
		}
		ApiResponse apiOutput = HttpClientUtil.doGet(url, headers, getConfig().getSfgProdUserName(),
				getConfig().getSfgProdPassword(), getConfig(), getConfig().getSfgProdHost());
		if (LOG.isInfoEnabled()) {
			LOG.info("Response:\n" + apiOutput.getStatusCode() + apiOutput.getResponse());
		}
		return apiOutput;
	}

	private ApiResponse getTestAUK(String sfgTestPartnerGivenName, Map<String, String> headers)
			throws HttpException, IOException, URISyntaxException, KeyManagementException, NoSuchAlgorithmException,
			CertificateException, KeyStoreException, ValidationException {
		String url = getConfig().getSfgTestRestURL();
		url += "svc/useraccounts/" + sfgTestPartnerGivenName;
		if (LOG.isInfoEnabled()) {
			LOG.info("Running API: GET " + url);
		}
		ApiResponse apiOutput = HttpClientUtil.doGet(url, headers, getConfig().getSfgTestUserName(),
				getConfig().getSfgTestPassword(), getConfig(), getConfig().getSfgTestHost());
		if (LOG.isInfoEnabled()) {
			LOG.info("Response:\n" + apiOutput.getStatusCode() + apiOutput.getResponse());
		}
		return apiOutput;
	}

	private void importSystem(PartnerInfo partnerInfo, String sshKeyProfileConfigKey,
			String userIdentityKeyProfileConfigKey, String userCredProfileConfigKey, String sftpInbPullProfileConfigKey,
			String sftpOutPushProfileConfigKey) throws ImportException, ApiInvocationException {
		SystemImportProcessor systemImportProcessor = new SystemImportProcessor(partnerInfo, getConfig(),
				new SystemConfigInfo().setManagedSshKeyProfileConfigKey(sshKeyProfileConfigKey)
						.setSftpInbPullProfileConfigKey(sftpInbPullProfileConfigKey)
						.setSftpOutPushProfileConfigKey(sftpOutPushProfileConfigKey)
						.setUserCredentialProfileConfigKey(userCredProfileConfigKey)
						.setUserIdentityKeyProfileConfigKey(userIdentityKeyProfileConfigKey));

		systemImportProcessor.execute();
	}

	private String importSftpOutBoundPush(PartnerInfo partnerInfo, SftpConfigInfo sftpConfigInfo)
			throws ImportException, ApiInvocationException {
		String sftpOutPushProfileConfigKey;
		SftpOutboundPushImportProcessor sftpOutboundPushIP = new SftpOutboundPushImportProcessor(partnerInfo,
				getConfig(), sftpConfigInfo);
		ConfigInfo sftpOutboundPushCI = sftpOutboundPushIP.execute();
		sftpOutPushProfileConfigKey = sftpOutboundPushCI.getProfileConfigKey();
		return sftpOutPushProfileConfigKey;
	}

	private String importSftpOutBoundPull(PartnerInfo partnerInfo, SftpConfigInfo sftpConfigInfo)
			throws ImportException, ApiInvocationException {
		String sftpOutPullProfileConfigKey;
		SftpOutboundPullImportProcessor sftpOutboundPullIP = new SftpOutboundPullImportProcessor(partnerInfo,
				getConfig(), sftpConfigInfo);
		ConfigInfo sftpOutboundPullCI = sftpOutboundPullIP.execute();
		sftpOutPullProfileConfigKey = sftpOutboundPullCI.getProfileConfigKey();
		return sftpOutPullProfileConfigKey;
	}

	private void validateSystemConfiguration(PartnerInfo partnerInfo) throws ImportException, ValidationException {
		Document doc = null;
		try {
			String url = getConfig().buildPRUrl(SYSTEM_API_URL) + String.format(
					"?resourceType=SYSTEM&subResourceType=SYSTEM&partner=%s&extensionName=SFGPartnerKey&extensionValue=%s",
					partnerInfo.getPemPartnerKey(), partnerInfo.getSfgPartnerKey());
			ApiResponse apiResponse = SftpResourceHelper.getResourceFromPR(getConfig(), url);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(apiResponse.getResponse())));
			if(SftpResourceHelper.getAttributeValueByTagName(doc, "System", "systemKey") != null) {
				if (SftpResourceHelper.isAttributeValueExist(doc, "resourceType", CODE, "SFTP")) {
					if ("PROD_CFG_PVRN_COMPLETE"
							.equalsIgnoreCase(SftpResourceHelper.getAttributeValueByTagName(doc, "status", CODE))) {
						throw new ValidationException(ProcessingStatus.IGNORED.getCode(),
								"ignored, System created and in provision to production complete status");
					} else if (SftpResourceHelper.isAttributeValueExist(doc, "SystemTypeExtn", "extensionName",
							"ParticipantActivityKey")) {
						throw new ValidationException(ProcessingStatus.IGNORED.getCode(),
								"ignored, as system created with some other activity");
					}
				} else if (!SftpResourceHelper.isAttributeValueExist(doc, "resourceType", CODE, "SFTP")) {
					throw new ValidationException(ProcessingStatus.IGNORED.getCode(),
							"ignored, as configuration type is of type other than SFTP");
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException | ApiInvocationException e) {
			throw new ImportException(e);
		}
	}

	private String importSftpInboundpush(PartnerInfo partnerInfo, SftpConfigInfo sftpConfigInfo)
			throws ImportException, ApiInvocationException {
		String sftpInbPushProfileConfigKey;
		SftpInboundPushImportProcessor sftpInboundPushIP = new SftpInboundPushImportProcessor(partnerInfo, getConfig(),
				sftpConfigInfo);
		ConfigInfo sftpInboundPushCI = sftpInboundPushIP.execute();
		sftpInbPushProfileConfigKey = sftpInboundPushCI.getProfileConfigKey();
		return sftpInbPushProfileConfigKey;
	}

	private String importSftpInboundpull(PartnerInfo partnerInfo, SftpConfigInfo sftpConfigInfo)
			throws ImportException, ApiInvocationException {
		String sftpInbPullProfileConfigKey;
		SftpInboundPullImportProcessor sftpInboundPullIP = new SftpInboundPullImportProcessor(partnerInfo, getConfig(),
				sftpConfigInfo);
		ConfigInfo sftpInboundPullCI = sftpInboundPullIP.execute();
		sftpInbPullProfileConfigKey = sftpInboundPullCI.getProfileConfigKey();
		return sftpInbPullProfileConfigKey;
	}

	private String importUserCredential(PartnerInfo partnerInfo, SftpConfigInfo sftpConfigInfo)
			throws ImportException, ApiInvocationException {
		String userCredProfileConfigKey = null;
		UserCredentialImportProcessor userCredentialIP = new UserCredentialImportProcessor(partnerInfo, getConfig(),
				sftpConfigInfo);
		ConfigInfo userCredentialCI = userCredentialIP.execute();
		userCredProfileConfigKey = userCredentialCI.getProfileConfigKey();
		userCredentialIP.associateAuthUserKeytoUserCred(userCredProfileConfigKey);
		return userCredProfileConfigKey;
	}

	private String importUserIdentityKey(PartnerInfo partnerInfo, SftpConfigInfo sftpConfigInfo,
			String preferredAuthenticationType) throws ImportException, ApiInvocationException {
		String userIdentityKeyProfileConfigKey = null;
		if (preferredAuthenticationType.equalsIgnoreCase("PUBLIC_KEY")) {
			UserIdentityKeyImportProcessor userIdentityKeyIP = new UserIdentityKeyImportProcessor(partnerInfo,
					getConfig(), sftpConfigInfo);
			ConfigInfo userIdentityKeyCI = userIdentityKeyIP.execute();
			userIdentityKeyProfileConfigKey = userIdentityKeyCI.getProfileConfigKey();
		}
		return userIdentityKeyProfileConfigKey;
	}

	private String importSshKey(PartnerInfo partnerInfo, SftpConfigInfo sftpConfigInfo)
			throws ImportException, ApiInvocationException {
		SshKeyImportProcessor sshKeyIP = new SshKeyImportProcessor(partnerInfo, getConfig(), sftpConfigInfo);
		ConfigInfo sshKeyCI = sshKeyIP.execute();
		return sshKeyCI.getProfileConfigKey();
	}

	private String importSshAuthUserKey(PartnerInfo partnerInfo) throws ImportException, ApiInvocationException {
		SshAuthUserkeyImportProcess sshAuthUserKeyIP = new SshAuthUserkeyImportProcess(partnerInfo, getConfig());
		ConfigInfo sshKeyCI = sshAuthUserKeyIP.execute();
		String sshAuthUserKeyProfileConfigKey = sshKeyCI.getProfileConfigKey();
		return sshAuthUserKeyProfileConfigKey;
	}

	private Document getSftpRemoteProfileData(boolean getFromProductionSfg, Document sfgPartnerDoc)
			throws ApiInvocationException, ImportException, ParserConfigurationException, SAXException, IOException {
		String sftpRemoteProfileKey = getSftpRemoteProfileKey(sfgPartnerDoc);
		String sshRemoteProfileData = ImportHelper
				.getResourceFromSFG(getFromProductionSfg, getConfig(), GET_SFTP_PROFILE_DATA_URL, sftpRemoteProfileKey)
				.getResponse();
		return ImportHelper.buildDomDoc(sshRemoteProfileData);
	}

	private static String getSftpRemoteProfileKey(Document sfgPartnerData) {
		return SftpResourceHelper.getAttributeValueByTagName(sfgPartnerData, getSftpTagName(sfgPartnerData),
				"remoteProfile");
	}

	private static String getSftpTagName(Document doc) {
		String sftpTagName = null;
		if (doc.getElementsByTagName(ELEMENT_PRODUCER_SSH_CONFIGURATION).getLength() > 0) {
			sftpTagName = ELEMENT_PRODUCER_SSH_CONFIGURATION;
		} else if (doc.getElementsByTagName(ELEMENT_CONSUMER_SSH_CONFIGURATION).getLength() > 0) {
			sftpTagName = ELEMENT_CONSUMER_SSH_CONFIGURATION;
		}
		return sftpTagName;
	}

	private static String getProdPreferredAuthenticationType(Document sfgPartner) {
		return SftpResourceHelper.getAttributeValueByTagName(sfgPartner, "preferredAuthenticationType", CODE);
	}

	private static boolean doesRemoteProfileHasKnownHostKey(SftpConfigInfo configInfo) {
		String prodKnownHostKey = SftpResourceHelper.getAttributeValueByTagName(configInfo.getProdRemoteProfileDoc(),
				"KnownHostKeyName", "name");

		String testKnownHostKey = SftpResourceHelper.getAttributeValueByTagName(configInfo.getTestRemoteProfileDoc(),
				"KnownHostKeyName", "name");

		if (prodKnownHostKey != null && testKnownHostKey != null) {
			return true;
		}
		return false;
	}

	private static boolean doesRemoteProfileHasRemoteUser(SftpConfigInfo configInfo) {
		String prodRemoteUser = SftpResourceHelper.getAttributeValueByTagName(configInfo.getProdRemoteProfileDoc(),
				"SSHRemoteProfile", "remoteUser");

		String testRemoteUser = SftpResourceHelper.getAttributeValueByTagName(configInfo.getTestRemoteProfileDoc(),
				"SSHRemoteProfile", "remoteUser");

		if (prodRemoteUser != null && testRemoteUser != null) {
			return true;
		}
		return false;
	}
}
