/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.Mockito;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.mockito.XmlEquals;
import com.ibm.pem.utilities.util.ApiResponse;
import com.ibm.pem.utilities.util.HttpClientUtil;

public class MockInvokeApi {

	public static final String SUCCESS_RESPONSE_STATUS_UPDATED_SUCCESSFULLY = "<success response=\"Status updated successfully\"/>";

	public static void pemPartnerGet(HttpClientUtil httpClient, Configuration config, String partnerUniqueId,
			String pemPartnerKey, String pemPartnerName) throws Exception {
		String url = String.format("%spartners/%s/", config.getPrRestURL(), pemPartnerKey);
		String response = Sfg2PemTest.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemPartner-get-response.xml");
		response = String.format(response, partnerUniqueId, pemPartnerKey, pemPartnerName, pemPartnerName);
		pemGetApi(httpClient, config, url, response);
	}

	private static void pemGetApi(HttpClientUtil httpClient, Configuration config, String url, String response)
			throws Exception {
		String userName = config.getUserName();
		char[] password = config.getPassword();
		String host = config.getPrHostName();
		ApiResponse apiOutput = new ApiResponse();
		apiOutput.setStatusCode("200");
		apiOutput.setStatusLine("HTTP/1.1 200 OK");
		apiOutput.setResponse(response);
		Mockito.when(httpClient.doGet(eq(url), anyMap(), eq(userName), eq(password), eq(config), eq(host)))
				.thenReturn(apiOutput);
	}

	public static void sfgListeningPartnerGet(HttpClientUtil httpClient, Configuration config,
			boolean getFromProductionSfg, String sfgPartnerKey, String pemPartnerName, String resourcePrefix,
			String remoteProfileKey) throws Exception {
		String url = getFromProductionSfg ? config.getSfgProdRestURL() : config.getSfgTestRestURL();
		url += Constants.SFG_PARTNER_REST_URI + sfgPartnerKey;
		String response = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/sfgPartnerListening-get-response.xml");
		response = String.format(response, sfgPartnerKey, sfgPartnerKey, pemPartnerName, resourcePrefix, sfgPartnerKey,
				sfgPartnerKey, remoteProfileKey, remoteProfileKey);
		sfgGetApi(httpClient, config, getFromProductionSfg, url, response);
	}

	public static void sfgInitiatingPartnerGet(HttpClientUtil httpClient, Configuration config,
			boolean getFromProductionSfg, String sfgPartnerKey, String pemPartnerName, String resourcePrefix,
			String sfgUserAccountKey) throws Exception {
		String url = getFromProductionSfg ? config.getSfgProdRestURL() : config.getSfgTestRestURL();
		url += Constants.SFG_PARTNER_REST_URI + sfgPartnerKey;
		String response = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/sfgPartnerInitiating-get-response.xml");
		response = String.format(response, sfgPartnerKey, sfgPartnerKey, pemPartnerName, sfgUserAccountKey,
				sfgPartnerKey);
		sfgGetApi(httpClient, config, getFromProductionSfg, url, response);
	}

	private static void sfgGetApi(HttpClientUtil httpClient, Configuration config, boolean getFromProductionSfg,
			String url, String response) throws Exception {
		String userName = getFromProductionSfg ? config.getSfgProdUserName() : config.getSfgTestUserName();
		char[] password = getFromProductionSfg ? config.getSfgProdPassword() : config.getSfgTestPassword();
		String host = getFromProductionSfg ? config.getSfgProdHost() : config.getSfgTestHost();
		ApiResponse apiOutput = new ApiResponse();
		apiOutput.setStatusCode("200");
		apiOutput.setStatusLine("HTTP/1.1 200 OK");
		apiOutput.setResponse(response);
		Mockito.when(httpClient.doGet(eq(url), anyMap(), eq(userName), eq(password), eq(config), eq(host)))
				.thenReturn(apiOutput);
	}

	public static void pemSystemsGetBySfgPartnerExtension(HttpClientUtil httpClient, Configuration config,
			String pemPartnerKey, String sfgPartnerKey) throws Exception {
		String response = "<Systems/>";
		pemSystemsGetBySfgPartnerExtension(httpClient, config, pemPartnerKey, sfgPartnerKey, response);
	}

	public static void pemSystemsGetBySfgPartnerExtension(HttpClientUtil httpClient, Configuration config,
			String pemPartnerKey, String sfgPartnerKey, String response) throws Exception {
		String url = String.format(
				"%ssystems/?resourceType=SYSTEM&subResourceType=SYSTEM&partner=%s&extensionName=SFGPartnerKey&extensionValue=%s",
				config.getPrRestURL(), pemPartnerKey, sfgPartnerKey);
		pemGetApi(httpClient, config, url, response);
	}

	public static String pemProfileConfigCreate(HttpClientUtil httpClient, Configuration config,
			String profileConfigName, String partnerKey, String resourceType, String subResourceType,
			String sponsorDivisionKey) throws Exception {
		String url = String.format("%s%s", config.getPrRestURL(), "profileconfigurations/");
		String request = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemProfileConfig-create-request.xml");
		request = String.format(request, profileConfigName, partnerKey, resourceType, subResourceType,
				sponsorDivisionKey);
		String profileConfigKey = profileConfigName + "PCKey";
		String response = String.format("<success location=\"%s\" profileConfigurationKey=\"%s\" />",
				(url + profileConfigKey), profileConfigKey);
		createApi(httpClient, config, url, request, response);
		return profileConfigKey;
	}

	private static void createApi(HttpClientUtil httpClient, Configuration config, String url, String request,
			String response) throws Exception {
		String userName = config.getUserName();
		char[] password = config.getPassword();
		String host = config.getPrHostName();
		ApiResponse apiOutput = new ApiResponse();
		apiOutput.setStatusCode("201");
		apiOutput.setStatusLine("HTTP/1.1 201 Created");
		apiOutput.setResponse(response);
		Mockito.when(httpClient.doPost(eq(url), anyMap(), argThat(new XmlEquals(request)), eq(userName), eq(password),
				eq(config), eq(host))).thenReturn(apiOutput);
	}

	public static void pemSshKeysGet(HttpClientUtil httpClient, Configuration config, String sshKeyProfileConfigKey)
			throws Exception {
		String url = String.format("%smanagedsshkeys/?configurationId=%s", config.getPrRestURL(),
				sshKeyProfileConfigKey);
		String response = "<ManagedSshKeys/>";
		pemGetApi(httpClient, config, url, response);
	}

	public static void sfgSshKnownHostKeyGet(HttpClientUtil httpClient, Configuration config,
			boolean getFromProductionSfg, String sshKHKKey) throws Exception {
		String url = getFromProductionSfg ? config.getSfgProdRestURL() : config.getSfgTestRestURL();
		url += "svc/sshknownhostkeys/" + sshKHKKey;
		String response = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/sfgSshKnownHostKey-get-response.xml");
		response = String.format(response, sshKHKKey, sshKHKKey);
		sfgGetApi(httpClient, config, getFromProductionSfg, url, response);
	}

	public static void sfgSshRemoteProfilesGet(HttpClientUtil httpClient, Configuration config,
			boolean getFromProductionSfg, String remoteProfileKey, String sshKnownHostKeyName) throws Exception {
		String url = getFromProductionSfg ? config.getSfgProdRestURL() : config.getSfgTestRestURL();
		url += "svc/sshremoteprofiles/" + remoteProfileKey;
		String response = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/sfgSshRemoteProfile-get-response.xml");
		response = String.format(response, remoteProfileKey, remoteProfileKey, sshKnownHostKeyName);
		sfgGetApi(httpClient, config, getFromProductionSfg, url, response);
	}

	public static void pemProfileConfigsGet(HttpClientUtil httpClient, Configuration config, String pemPartnerKey,
			String resourceType, String subResourceType, String configName) throws Exception {
		String url = String.format("%sprofileconfigurations/?partner=%s&resourceType=%s&subResourceType=%s&name=%s",
				config.getPrRestURL(), pemPartnerKey, resourceType, subResourceType, configName);
		String response = "<ProfileConfigurations/>";
		pemGetApi(httpClient, config, url, response);
	}

	public static void pemSshKeyCreate(HttpClientUtil httpClient, Configuration config,
			String pemSshKeyProfileConfigKey, String pemSshKeyKey) throws Exception {
		String url = String.format("%smanagedsshkeys/", config.getPrRestURL());
		String request = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemSshKnownHostKey-create-request.xml");
		request = String.format(request, pemSshKeyProfileConfigKey);
		String response = String.format("<success location=\"%s\" managedSshKeyKey=\"%s\" />", (url + pemSshKeyKey),
				pemSshKeyKey);
		createApi(httpClient, config, url, request, response);
	}

	public static void pemSshKeyMarkComplete(HttpClientUtil httpClient, Configuration config, String pemSshKeyKey)
			throws Exception {
		String url = String.format("%smanagedsshkeys/%s/actions/markcomplete", config.getPrRestURL(), pemSshKeyKey);
		String response = SUCCESS_RESPONSE_STATUS_UPDATED_SUCCESSFULLY;
		pemPostApi(httpClient, config, url, null, response);
	}

	public static void pemUserCredentialsGet(HttpClientUtil httpClient, Configuration config, String pemPartnerKey,
			String userCredProfileConfigKey) throws Exception {
		String url = String.format("%smanagedusercredentials/?partner=%s&configurationId=%s", config.getPrRestURL(),
				pemPartnerKey, userCredProfileConfigKey);
		String response = "<ManagedUserCredentials/>";
		pemGetApi(httpClient, config, url, response);
	}

	public static void pemUserCredCreate(HttpClientUtil httpClient, Configuration config,
			String pemUserCredProfileConfigKey, String testUserName, String prodUserName, String pemUserCredKey)
			throws Exception {
		String url = String.format("%smanagedusercredentials/", config.getPrRestURL());
		String request = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemUserCredential-create-request.xml");
		request = String.format(request, pemUserCredProfileConfigKey, testUserName, prodUserName);
		String response = String.format("<success location=\"%s\" managedUserCredentialKey=\"%s\" />",
				(url + pemUserCredKey), pemUserCredKey);
		createApi(httpClient, config, url, request, response);
	}

	public static void pemUserCredMarkComplete(HttpClientUtil httpClient, Configuration config, String pemSshKeyKey)
			throws Exception {
		String url = String.format("%smanagedusercredentials/%s/actions/markcomplete", config.getPrRestURL(),
				pemSshKeyKey);
		String response = SUCCESS_RESPONSE_STATUS_UPDATED_SUCCESSFULLY;
		pemPostApi(httpClient, config, url, null, response);
	}

	public static void pemSftpInbPullsGet(HttpClientUtil httpClient, Configuration config, String pemPartnerKey,
			String sftpInbPullProfileConfigKey) throws Exception {
		String url = String.format("%ssftpinboundpulls/?partner=%s&configurationId=%s", config.getPrRestURL(),
				pemPartnerKey, sftpInbPullProfileConfigKey);
		String response = "<SftpInboundPulls/>";
		pemGetApi(httpClient, config, url, response);
	}

	public static void pemSftpInbPullCreate(HttpClientUtil httpClient, Configuration config,
			String pemSftpInbPullProfileConfigKey, String remoteProfileName, String pemUserCredProfileConfigKey,
			String pemSshKhkProfileConfigKey, String pemSftpInbPullKey) throws Exception {
		String url = String.format("%ssftpinboundpulls/", config.getPrRestURL());
		String request = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemSftpInboundPull-create-request.xml");
		request = String.format(request, pemSftpInbPullProfileConfigKey, remoteProfileName, pemUserCredProfileConfigKey,
				pemSshKhkProfileConfigKey, remoteProfileName, pemUserCredProfileConfigKey, pemSshKhkProfileConfigKey);
		String response = String.format("<success location=\"%s\" sftpInboundPullKey=\"%s\" />",
				(url + pemSftpInbPullKey), pemSftpInbPullKey);
		createApi(httpClient, config, url, request, response);
	}

	public static void pemSftpInbPullMarkComplete(HttpClientUtil httpClient, Configuration config,
			String pemSftpInbPullKey) throws Exception {
		String url = String.format("%ssftpinboundpulls/%s/actions/markcomplete", config.getPrRestURL(),
				pemSftpInbPullKey);
		String response = SUCCESS_RESPONSE_STATUS_UPDATED_SUCCESSFULLY;
		pemPostApi(httpClient, config, url, null, response);
	}

	public static void pemSftpOutPushsGet(HttpClientUtil httpClient, Configuration config, String pemPartnerKey,
			String sftpOutPushProfileConfigKey) throws Exception {
		String url = String.format("%ssftpoutboundpushs/?partner=%s&configurationId=%s", config.getPrRestURL(),
				pemPartnerKey, sftpOutPushProfileConfigKey);
		String response = "<SftpOutboundPushs/>";
		pemGetApi(httpClient, config, url, response);
	}

	public static void pemSftpOutPushCreate(HttpClientUtil httpClient, Configuration config,
			String pemSftpOutPushProfileConfigKey, String remoteProfileName, String pemUserCredProfileConfigKey,
			String pemSshKhkProfileConfigKey, String pemSftpOutPushKey) throws Exception {
		String url = String.format("%ssftpoutboundpushs/", config.getPrRestURL());
		String request = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemSftpOutboundPush-create-request.xml");
		request = String.format(request, pemSftpOutPushProfileConfigKey, remoteProfileName, pemUserCredProfileConfigKey,
				pemSshKhkProfileConfigKey, remoteProfileName, pemUserCredProfileConfigKey, pemSshKhkProfileConfigKey);
		String response = String.format("<success location=\"%s\" sftpOutboundPushKey=\"%s\" />",
				(url + pemSftpOutPushKey), pemSftpOutPushKey);
		createApi(httpClient, config, url, request, response);
	}

	public static void pemSftpOutPushMarkComplete(HttpClientUtil httpClient, Configuration config,
			String pemSftpInbPullKey) throws Exception {
		String url = String.format("%ssftpoutboundpushs/%s/actions/markcomplete", config.getPrRestURL(),
				pemSftpInbPullKey);
		String response = SUCCESS_RESPONSE_STATUS_UPDATED_SUCCESSFULLY;
		pemPostApi(httpClient, config, url, null, response);
	}

	public static void pemSystemsGet(HttpClientUtil httpClient, Configuration config, String pemPartnerKey,
			String sftpOutPushProfileConfigKey) throws Exception {
		String url = String.format("%ssystems/?partner=%s&configurationId=%s", config.getPrRestURL(), pemPartnerKey,
				sftpOutPushProfileConfigKey);
		String response = "<Systems/>";
		pemGetApi(httpClient, config, url, response);
	}

	public static void pemSystemSftpListenCreate(HttpClientUtil httpClient, Configuration config,
			String pemSystemProfileConfigKey, String sfgPartnerKey, String pemSftpInbPullProfileConfigKey,
			String pemSftpOutPushProfileConfigKey, String pemSshKhkProfileConfigKey, String pemUserCredProfileConfigKey,
			String pemSystemKey) throws Exception {
		String url = String.format("%ssystems/", config.getPrRestURL());
		String request = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemSystem-sftpListen-create-request.xml");
		request = String.format(request, pemSystemProfileConfigKey, sfgPartnerKey, sfgPartnerKey,
				pemSftpInbPullProfileConfigKey, pemSftpOutPushProfileConfigKey, pemSshKhkProfileConfigKey,
				pemUserCredProfileConfigKey, sfgPartnerKey, sfgPartnerKey, pemSftpInbPullProfileConfigKey,
				pemSftpOutPushProfileConfigKey, pemSshKhkProfileConfigKey, pemUserCredProfileConfigKey);
		String response = String.format("<success location=\"%s\" systemKey=\"%s\" />", (url + pemSystemKey),
				pemSystemKey);
		createApi(httpClient, config, url, request, response);
	}

	public static void pemSystemMarkComplete(HttpClientUtil httpClient, Configuration config, String pemSystemKey)
			throws Exception {
		String url = String.format("%ssystems/%s/actions/markcomplete", config.getPrRestURL(), pemSystemKey);
		String response = SUCCESS_RESPONSE_STATUS_UPDATED_SUCCESSFULLY;
		pemPostApi(httpClient, config, url, null, response);
	}

	public static void pemSystemMarkComplete(HttpClientUtil httpClient, Configuration config, String pemSystemKey,
			ApiResponse apiOutput) throws Exception {
		String url = String.format("%ssystems/%s/actions/markcomplete", config.getPrRestURL(), pemSystemKey);
		pemPostApi(httpClient, config, url, null, apiOutput);
	}

	private static void pemPostApi(HttpClientUtil httpClient, Configuration config, String url, String request,
			String response) throws Exception {
		ApiResponse apiOutput = new ApiResponse();
		apiOutput.setStatusCode("200");
		apiOutput.setResponse(response);
		pemPostApi(httpClient, config, url, request, apiOutput);
	}

	private static void pemPostApi(HttpClientUtil httpClient, Configuration config, String url, String request,
			ApiResponse apiOutput) throws Exception {
		String userName = config.getUserName();
		char[] password = config.getPassword();
		String host = config.getPrHostName();
		Mockito.when(
				httpClient.doPost(eq(url), anyMap(), eq(request), eq(userName), eq(password), eq(config), eq(host)))
				.thenReturn(apiOutput);
	}

	public static void sfgUserAccountGet(HttpClientUtil httpClient, Configuration config, boolean getFromProductionSfg,
			String userAccountKey, String sfgPartnerKey, String pemPartnerName) throws Exception {
		String url = getFromProductionSfg ? config.getSfgProdRestURL() : config.getSfgTestRestURL();
		url += "svc/useraccounts/" + userAccountKey;
		String response = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/sfgUserAccount-get-response.xml");
		response = String.format(response, userAccountKey, pemPartnerName, userAccountKey, sfgPartnerKey, sfgPartnerKey,
				sfgPartnerKey, sfgPartnerKey);
		sfgGetApi(httpClient, config, getFromProductionSfg, url, response);
	}

	public static void pemProfileConfigPrGet(HttpClientUtil httpClient, Configuration config, String profileConfigKey,
			String profileConfigName, String sponsorKey, String resourceTypeCode, String subResourceTypeCode,
			String pemPartnerKey, String sponsorDivisionKey, String partnerDivisionKey) throws Exception {
		String url = String.format("%sprofileconfigurations/%s", config.getPrRestURL(), profileConfigKey);
		String response = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemProfileConfig-pr-get-response.xml");
		response = String.format(response, profileConfigKey, profileConfigName, sponsorKey, resourceTypeCode,
				subResourceTypeCode, pemPartnerKey, sponsorDivisionKey, partnerDivisionKey);
		pemGetApi(httpClient, config, url, response);
	}

	public static void pemProfileConfigDgGet(HttpClientUtil httpClient, Configuration config, String profileConfigKey,
			String profileConfigName, String sponsorKey, String resourceTypeCode, String subResourceTypeCode,
			String pemPartnerKey, String sponsorDivisionKey, String partnerDivisionKey, String parentProfileConfigKey)
			throws Exception {
		String url = String.format("%sprofileconfigurations/%s", config.getPrRestURL(), profileConfigKey);
		String response = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemProfileConfig-dg-get-response.xml");
		response = String.format(response, profileConfigKey, profileConfigName, sponsorKey, resourceTypeCode,
				subResourceTypeCode, pemPartnerKey, sponsorDivisionKey, partnerDivisionKey, parentProfileConfigKey);
		pemGetApi(httpClient, config, url, response);
	}

	public static void pemSftpOutPullsDgGet(HttpClientUtil httpClient, Configuration config,
			String sftpOutPullProfileConfigKey, String sponsorKey, String sftpOutboundPullKey,
			String sftpOutPullProfileConfigName) throws Exception {
		String url = String.format("%ssftpoutboundpulls/?configurationId=%s", config.getPrRestURL(),
				sftpOutPullProfileConfigKey);
		String response = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemSftpOutboundPull-dg-get-response.xml");
		response = String.format(response, sponsorKey, sftpOutboundPullKey, sftpOutPullProfileConfigName,
				sftpOutPullProfileConfigKey);
		pemGetApi(httpClient, config, url, response);
	}

	public static void pemSftpOutPullMarkComplete(HttpClientUtil httpClient, Configuration config,
			String pemSftpOutPullKey) throws Exception {
		String url = String.format("%ssftpoutboundpulls/%s/actions/markcomplete", config.getPrRestURL(),
				pemSftpOutPullKey);
		String response = SUCCESS_RESPONSE_STATUS_UPDATED_SUCCESSFULLY;
		pemPostApi(httpClient, config, url, null, response);
	}

	public static void pemSftpInbPushsDgGet(HttpClientUtil httpClient, Configuration config,
			String sftpInbPushProfileConfigKey, String sponsorKey, String sftpInbPushKey,
			String sftpInbPushProfileConfigName) throws Exception {
		String url = String.format("%ssftpinboundpushs/?configurationId=%s", config.getPrRestURL(),
				sftpInbPushProfileConfigKey);
		String response = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemSftpInboundPush-dg-get-response.xml");
		response = String.format(response, sponsorKey, sftpInbPushKey, sftpInbPushProfileConfigName,
				sftpInbPushProfileConfigKey);
		pemGetApi(httpClient, config, url, response);
	}

	public static void pemSftpInbPushMarkComplete(HttpClientUtil httpClient, Configuration config,
			String pemSftpInbPushKey) throws Exception {
		String url = String.format("%ssftpinboundpushs/%s/actions/markcomplete", config.getPrRestURL(),
				pemSftpInbPushKey);
		String response = SUCCESS_RESPONSE_STATUS_UPDATED_SUCCESSFULLY;
		pemPostApi(httpClient, config, url, null, response);
	}

	public static void pemSshKeysDgGet(HttpClientUtil httpClient, Configuration config, String sshKeyProfileConfigKey,
			String sponsorKey, String sshKeyKey, String sshKeyProfileConfigName) throws Exception {
		String url = String.format("%smanagedsshkeys/?configurationId=%s", config.getPrRestURL(),
				sshKeyProfileConfigKey);
		String response = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemSshKey-dg-get-response.xml");
		response = String.format(response, sponsorKey, sshKeyKey, sshKeyProfileConfigName, sshKeyProfileConfigKey);
		pemGetApi(httpClient, config, url, response);
	}

	public static void pemSystemSftpInitiateCreate(HttpClientUtil httpClient, Configuration config,
			String pemSystemProfileConfigKey, String sfgPartnerKey, String pemSftpInbPushProfileConfigKey,
			String pemSftpOutPullProfileConfigKey, String pemSshHikProfileConfigKey, String pemUserCredProfileConfigKey,
			String pemSystemKey) throws Exception {
		String url = String.format("%ssystems/", config.getPrRestURL());
		String request = Sfg2PemTest
				.readFileAsString(Sfg2PemTest.getCommonDataDir() + "/pemSystem-sftpInitiate-create-request.xml");
		request = String.format(request, pemSystemProfileConfigKey, sfgPartnerKey, sfgPartnerKey,
				pemSftpInbPushProfileConfigKey, pemSftpOutPullProfileConfigKey, pemSshHikProfileConfigKey,
				pemUserCredProfileConfigKey, sfgPartnerKey, sfgPartnerKey, pemSftpInbPushProfileConfigKey,
				pemSftpOutPullProfileConfigKey, pemSshHikProfileConfigKey, pemUserCredProfileConfigKey);
		String response = String.format("<success location=\"%s\" systemKey=\"%s\" />", (url + pemSystemKey),
				pemSystemKey);
		createApi(httpClient, config, url, request, response);
	}

}
