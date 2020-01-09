/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Test;
import org.w3c.dom.Document;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.MockInvokeApi;
import com.ibm.pem.utilities.sfg2pem.Sfg2PemTest;
import com.ibm.pem.utilities.sfg2pem.ValidationException;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.SftpConfigImportHandler;
import com.ibm.pem.utilities.util.DOMUtils;
import com.ibm.pem.utilities.util.HttpClientUtil;
import com.ibm.pem.utilities.util.ResourceFactory;

public class SftpConfigImportHandlerTest extends Sfg2PemTest {

	private static final String MSG_HANDLER_ACCEPTED_THE_CONFIG_UNEXPECTEDLY = "Handler accepted the config unexpectedly!";

	private static final String CONFIG_IMPORT_PROPERTIES_FILE_NAME = "/config-import.properties";

	private static final String SFTP_IB_PULL_PARTNER = "<TradingPartner><isListeningProducer code=\"true\" />"
			+ "	<producerSshConfiguration remoteProfile=\"id01ibtest01\" /></TradingPartner>";
	private static final String SFTP_OB_PUSH_PARTNER = "<TradingPartner><consumerSshConfiguration remoteProfile=\"id01ibtest01\" />"
			+ "	<isListeningConsumer code=\"true\" /></TradingPartner>";
	private static final String SFTP_IB_PUSH_PARTNER = "<TradingPartner><doesUseSSH code=\"true\" />"
			+ "	<isInitiatingProducer code=\"true\" /></TradingPartner>";
	private static final String SFTP_OB_PULL_PARTNER = "<TradingPartner><doesUseSSH code=\"true\" />"
			+ "	<isInitiatingConsumer code=\"true\" /></TradingPartner>";

	@Test
	public void testAcceptSftpInboundPullOnlyPartner() throws Exception {
		SftpConfigImportHandler handler = new SftpConfigImportHandler(mock(Configuration.class));
		Document prodSfgPartnerDoc = DOMUtils.toDocument(SFTP_IB_PULL_PARTNER);
		Document testSfgPartnerDoc = DOMUtils.toDocument(SFTP_IB_PULL_PARTNER);
		assertTrue("Handler failed to accept SFTP Inbound Pull Config.",
				handler.accept(prodSfgPartnerDoc, testSfgPartnerDoc));
		assertFalse(MSG_HANDLER_ACCEPTED_THE_CONFIG_UNEXPECTEDLY,
				handler.accept(DOMUtils.toDocument(SFTP_OB_PUSH_PARTNER), testSfgPartnerDoc));
	}

	@Test
	public void testAcceptSftpOutboundPushOnlyPartner() throws Exception {
		SftpConfigImportHandler handler = new SftpConfigImportHandler(mock(Configuration.class));
		Document prodSfgPartnerDoc = DOMUtils.toDocument(SFTP_OB_PUSH_PARTNER);
		Document testSfgPartnerDoc = DOMUtils.toDocument(SFTP_OB_PUSH_PARTNER);
		assertTrue("Handler failed to accept SFTP Outbound Push Config.",
				handler.accept(prodSfgPartnerDoc, testSfgPartnerDoc));
		assertFalse(MSG_HANDLER_ACCEPTED_THE_CONFIG_UNEXPECTEDLY,
				handler.accept(DOMUtils.toDocument(SFTP_IB_PULL_PARTNER), testSfgPartnerDoc));
	}

	@Test
	public void testAcceptSftpInboundPushOnlyPartner() throws Exception {
		SftpConfigImportHandler handler = new SftpConfigImportHandler(mock(Configuration.class));
		Document prodSfgPartnerDoc = DOMUtils.toDocument(SFTP_IB_PUSH_PARTNER);
		Document testSfgPartnerDoc = DOMUtils.toDocument(SFTP_IB_PUSH_PARTNER);
		assertTrue("Handler failed to accept SFTP Inbound Push Config.",
				handler.accept(prodSfgPartnerDoc, testSfgPartnerDoc));
		assertFalse(MSG_HANDLER_ACCEPTED_THE_CONFIG_UNEXPECTEDLY,
				handler.accept(DOMUtils.toDocument(SFTP_OB_PULL_PARTNER), testSfgPartnerDoc));
	}

	@Test
	public void testAcceptSftpOutboundPullOnlyPartner() throws Exception {
		SftpConfigImportHandler handler = new SftpConfigImportHandler(mock(Configuration.class));
		Document prodSfgPartnerDoc = DOMUtils.toDocument(SFTP_OB_PULL_PARTNER);
		Document testSfgPartnerDoc = DOMUtils.toDocument(SFTP_OB_PULL_PARTNER);
		assertTrue("Handler failed to accept SFTP Outbound Pull Config.",
				handler.accept(prodSfgPartnerDoc, testSfgPartnerDoc));
		assertFalse(MSG_HANDLER_ACCEPTED_THE_CONFIG_UNEXPECTEDLY,
				handler.accept(DOMUtils.toDocument(SFTP_IB_PUSH_PARTNER), testSfgPartnerDoc));
	}

	@Test
	public void testAcceptNone() throws Exception {
		String apiresponse = "<TradingPartner></TradingPartner>";
		SftpConfigImportHandler handler = new SftpConfigImportHandler(mock(Configuration.class));
		Document sfgPartnerDoc = DOMUtils.toDocument(apiresponse);
		assertFalse(MSG_HANDLER_ACCEPTED_THE_CONFIG_UNEXPECTEDLY, handler.accept(sfgPartnerDoc, sfgPartnerDoc));
	}

	@Test
	public void testImportFailsWhenConfigIsAlreadyInProdProvCompleteStatus() throws Exception {
		String testDirName = "testSftpConfigImportHandler/partnerIsAlreadyImportedError";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);
		ResourceFactory resourceFactory = mock(ResourceFactory.class);
		HttpClientUtil httpClient = mock(HttpClientUtil.class);
		when(resourceFactory.createHttpClientInstance()).thenReturn(httpClient);
		Configuration config = newConfig(installDir, resourceFactory);

		PartnerInfo partnerInfo = mockAPIs(testDirName, config, httpClient);
		SftpConfigImportHandler handler = new SftpConfigImportHandler(config);
		try {
			handler.execute(partnerInfo);
			fail("Method call successful but was expected to fail!");
		} catch (ValidationException e) {
			assertEquals("ignored, System created and in provision to production complete status", e.getMessage());
		}
	}

	@Test
	public void testImportFailsWhenConfigIsBeingHandledInActivity() throws Exception {
		String testDirName = "testSftpConfigImportHandler/configIsBeingHandledInActivity";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);
		ResourceFactory resourceFactory = mock(ResourceFactory.class);
		HttpClientUtil httpClient = mock(HttpClientUtil.class);
		when(resourceFactory.createHttpClientInstance()).thenReturn(httpClient);
		Configuration config = newConfig(installDir, resourceFactory);

		PartnerInfo partnerInfo = mockAPIs(testDirName, config, httpClient);
		SftpConfigImportHandler handler = new SftpConfigImportHandler(config);
		try {
			handler.execute(partnerInfo);
			fail("Method call successful but was expected to fail!");
		} catch (ValidationException e) {
			assertEquals("ignored, as system created with some other activity", e.getMessage());
		}
	}

	private PartnerInfo mockAPIs(String testDirName, Configuration config, HttpClientUtil httpClient)
			throws Exception, IOException {
		String resourcePrefix = "test01";
		String pemPartnerName = resourcePrefix + "-partner";
		String pemPartnerKey = pemPartnerName + "Key";
		String sfgPartnerKey = resourcePrefix + "-id01";

		MockInvokeApi.sfgListeningPartnerGet(httpClient, config, true, sfgPartnerKey, pemPartnerName, resourcePrefix,
				"dummyRemoteProfileKey");
		MockInvokeApi.sfgListeningPartnerGet(httpClient, config, false, sfgPartnerKey, pemPartnerName, resourcePrefix,
				"dummyRemoteProfileKey");

		String response = Sfg2PemTest.readFileAsString(
				Sfg2PemTest.getTestInstallDir(testDirName) + "/pemSystem-sftpListen-get-response.xml");

		MockInvokeApi.pemSystemsGetBySfgPartnerExtension(httpClient, config, pemPartnerKey, sfgPartnerKey, response);

		PartnerInfo partnerInfo = mock(PartnerInfo.class);
		when(partnerInfo.getTestSfgPartnerKey()).thenReturn(sfgPartnerKey);
		when(partnerInfo.getPemPartnerKey()).thenReturn(pemPartnerKey);
		when(partnerInfo.getSfgPartnerKey()).thenReturn(sfgPartnerKey);
		return partnerInfo;
	}
}
