/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.MockInvokeApi;
import com.ibm.pem.utilities.sfg2pem.Sfg2PemTest;
import com.ibm.pem.utilities.sfg2pem.SfgPartnerReportReader;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo.ProcessingStatus;
import com.ibm.pem.utilities.util.ApiResponse;
import com.ibm.pem.utilities.util.HttpClientUtil;
import com.ibm.pem.utilities.util.ResourceFactory;

@RunWith(MockitoJUnitRunner.class)
public class SfgPartnerSftpImportTest extends Sfg2PemTest {

	private static final String CONFIG_IMPORT_PROPERTIES_FILE_NAME = "/config-import.properties";

	private static final String SYSTEM = "SYSTEM";

	private static final String SFTP = "SFTP";

	private static final String USER_CRED = "USER_CRED";

	private static final String SSH_KEY = "SSH_KEY";

	private static final String SPONSOR_DIVISION1_KEY = "sponsorDiv1Key";

	@Mock
	private ResourceFactory resourceFactory;

	@Mock
	private HttpClientUtil httpClient;

	@Before
	public void setUp() {
		when(resourceFactory.createHttpClientInstance()).thenReturn(httpClient);
	}

	@Test
	public void testImportSfgPartnersListeningSuccess() throws Exception {
		String testDirName = "testImportSfgPartnersListening/success01";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);
		Configuration config = newConfig(installDir, resourceFactory);

		mockListeningPartnerAPIs(httpClient, config, "test01", "id01", false);
		mockListeningPartnerAPIs(httpClient, config, "test02", "id02", false);

		new ImportHandler(config).execute();

		// Read the output and verify content.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);

		String sfgPartnerKey = "test01-id01";
		assertEquals(ProcessingStatus.UPLOADED_TO_PEM, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertNull(partnerMap.get(sfgPartnerKey).getMessage());

		sfgPartnerKey = "test02-id02";
		assertEquals(ProcessingStatus.UPLOADED_TO_PEM, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertNull(partnerMap.get(sfgPartnerKey).getMessage());

		assertEquals(2, partnerMap.size());
	}

	@Test
	public void testImportSfgPartnersListeningSuccessOn2ndAttempt() throws Exception {
		String testDirName = "testImportSfgPartnersListening/success02";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);

		Configuration config = newConfig(installDir, resourceFactory);
		mockListeningPartnerAPIs(httpClient, config, "test01", "id01", true);
		new ImportHandler(config).execute();

		// Read the output and verify content.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);

		String sfgPartnerKey = "test01-id01";
		assertEquals(ProcessingStatus.ERROR, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertEquals("dummy error1", partnerMap.get(sfgPartnerKey).getMessage());

		config = newConfig(installDir, resourceFactory);
		mockListeningPartnerAPIs(httpClient, config, "test01", "id01", false);
		new ImportHandler(config).execute();

		// Read the output and verify content.
		partnerMap = new SfgPartnerReportReader(config).execute(installDir + REPORT_PROD_CSV_FILE_NAME);

		assertEquals(ProcessingStatus.UPLOADED_TO_PEM, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertNull(partnerMap.get(sfgPartnerKey).getMessage());

		assertEquals(1, partnerMap.size());
	}

//	@Test
//	public void testImportFailsWhenPartnerIsAlreadyImported() throws Exception {
//		String testDirName = "testImportFailsWhenPartnerIsAlreadyImported";
//		String installDir = getTestInstallDir(testDirName);
//		copyTestResources(testDirName);
//		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);
//		Configuration config = newConfig(installDir, resourceFactory);
//
//		mockListeningPartnerAPIs(httpClient, testDirName, config, "test01", "id01", false);
//
//		new ImportHandler(config).execute();
//
//		// Read the output and verify content.
//		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
//				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);
//
//		String sfgPartnerKey = "test01-id01";
//		assertEquals(ProcessingStatus.UPLOADED_TO_PEM, partnerMap.get(sfgPartnerKey).getProcessingStatus());
//		assertNull(partnerMap.get(sfgPartnerKey).getMessage());
//
//
//		assertEquals(1, partnerMap.size());
//		
//	}

	@Test
	public void testImportSfgPartnersInitiatingSuccess() throws Exception {
		String testDirName = "testImportSfgPartnersInitiating/success01";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);

		Configuration config = newConfig(installDir, resourceFactory);

		mockInitiatingPartnerAPIs(httpClient, config, "test04", "id04", 5);
		mockInitiatingPartnerAPIs(httpClient, config, "test05", "id05", 5);

		new ImportHandler(config).execute();

		// Read the output and verify content.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);

		String sfgPartnerKey = "test04-id04";
		assertEquals(ProcessingStatus.UPLOADED_TO_PEM, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertNull(partnerMap.get(sfgPartnerKey).getMessage());

		sfgPartnerKey = "test05-id05";
		assertEquals(ProcessingStatus.UPLOADED_TO_PEM, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertNull(partnerMap.get(sfgPartnerKey).getMessage());

		assertEquals(2, partnerMap.size());
	}

	@Test
	public void testImportSfgPartnersInitiatingNoOutPullProfileError() throws Exception {
		String testDirName = "testImportSfgPartnersInitiating/noOutPullProfileError";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);
		Configuration config = newConfig(installDir, resourceFactory);

		mockInitiatingPartnerAPIs(httpClient, config, "test03", "id03", 1);

		new ImportHandler(config).execute();

		// Read the output and verify content.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);

		String sfgPartnerKey = "test03-id03";
		assertEquals(ProcessingStatus.ERROR, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertEquals("Outbound Pull Profile Configuration for SFTP_OUT_PULL not specified.",
				partnerMap.get(sfgPartnerKey).getMessage());

		assertEquals(1, partnerMap.size());
	}

	@Test
	public void testImportSfgPartnersInitiatingOutPullProfileIsPrError() throws Exception {
		String testDirName = "testImportSfgPartnersInitiating/outPullProfileIsPrError";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);
		Configuration config = newConfig(installDir, resourceFactory);

		String resourcePrefix = "test04";
		mockInitiatingPartnerAPIs(httpClient, config, resourcePrefix, "id04", 1);

		String outPullProfileConfigKey = "5947e63c-ee4c-48f9-bab9-2c2b0b94b45c";
		String sftpOutboundPullProfileConfigName = resourcePrefix + "sftpoutpull-pr";
		String sponsorKey = "2405680d-5aea-425d-9773-28affc005bc0";
		String pemPartnerKey = resourcePrefix + "-partnerKey";
		String partnerDivisionKey = "b4949281-0adc-4b3f-9417-078ddbdd561d";
		MockInvokeApi.pemProfileConfigPrGet(httpClient, config, outPullProfileConfigKey,
				sftpOutboundPullProfileConfigName, sponsorKey, SFTP, "SFTP_OUT_PULL", pemPartnerKey,
				SPONSOR_DIVISION1_KEY, partnerDivisionKey);

		new ImportHandler(config).execute();

		// Read the output and verify content.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);

		String sfgPartnerKey = "test04-id04";
		assertEquals(ProcessingStatus.ERROR, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertEquals("Profile Configuration for SFTP_OUT_PULL must be of type SG or DG",
				partnerMap.get(sfgPartnerKey).getMessage());

		assertEquals(1, partnerMap.size());
	}

	@Test
	public void testImportSfgPartnersInitiatingNoInbPushProfileError() throws Exception {
		String testDirName = "testImportSfgPartnersInitiating/noInbPushProfileError";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);
		Configuration config = newConfig(installDir, resourceFactory);

		mockInitiatingPartnerAPIs(httpClient, config, "test03", "id03", 2);

		new ImportHandler(config).execute();

		// Read the output and verify content.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);

		String sfgPartnerKey = "test03-id03";
		assertEquals(ProcessingStatus.ERROR, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertEquals("Inbound Push Profile Configuration for SFTP_INB_PUSH not specified.",
				partnerMap.get(sfgPartnerKey).getMessage());

		assertEquals(1, partnerMap.size());
	}

	@Test
	public void testImportSfgPartnersInitiatingInbPushProfileIsPrError() throws Exception {
		String testDirName = "testImportSfgPartnersInitiating/inbPushProfileIsPrError";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);
		Configuration config = newConfig(installDir, resourceFactory);

		String resourcePrefix = "test04";
		mockInitiatingPartnerAPIs(httpClient, config, resourcePrefix, "id04", 2);

		String inbPushProfileConfigKey = "5947e63c-ee4c-48f9-bab9-2c2b0b94b45c";
		String sftpInboundPushProfileConfigName = resourcePrefix + "sftpinbpush-pr";
		String sponsorKey = "2405680d-5aea-425d-9773-28affc005bc0";
		String pemPartnerKey = resourcePrefix + "-partnerKey";
		String partnerDivisionKey = "b4949281-0adc-4b3f-9417-078ddbdd561d";
		MockInvokeApi.pemProfileConfigPrGet(httpClient, config, inbPushProfileConfigKey,
				sftpInboundPushProfileConfigName, sponsorKey, SFTP, "SFTP_INB_PUSH", pemPartnerKey,
				SPONSOR_DIVISION1_KEY, partnerDivisionKey);

		new ImportHandler(config).execute();

		// Read the output and verify content.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);

		String sfgPartnerKey = "test04-id04";
		assertEquals(ProcessingStatus.ERROR, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertEquals("Profile Configuration for SFTP_INB_PUSH must be of type SG or DG",
				partnerMap.get(sfgPartnerKey).getMessage());

		assertEquals(1, partnerMap.size());
	}

	@Test
	public void testImportSfgPartnersInitiatingNoHostIdentityKeyProfileError() throws Exception {
		String testDirName = "testImportSfgPartnersInitiating/noHostIdentityKeyProfileError";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);
		Configuration config = newConfig(installDir, resourceFactory);

		mockInitiatingPartnerAPIs(httpClient, config, "test03", "id03", 3);

		new ImportHandler(config).execute();

		// Read the output and verify content.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);

		String sfgPartnerKey = "test03-id03";
		assertEquals(ProcessingStatus.ERROR, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertEquals("Profile Configuration for SSH_KEY is not available as part of tool",
				partnerMap.get(sfgPartnerKey).getMessage());

		assertEquals(1, partnerMap.size());
	}

	static void mockInitiatingPartnerAPIs(HttpClientUtil httpClient, Configuration config, String resourcePrefix,
			String uniqueId, int step) throws Exception {
		String pemPartnerName = resourcePrefix + "-partner";
		String pemPartnerKey = pemPartnerName + "Key";
		String sfgPartnerKey = resourcePrefix + "-" + uniqueId;
		String prodsfgUserAccountKey = resourcePrefix + "4590";
		String testsfgUserAccountKey = resourcePrefix + "3575";
		String sftpOutboundPullProfileConfigKey = "868cdedf-04b2-495c-826f-fbec21016a14";
		String sftpInboundPushProfileConfigKey = "64436795-aee0-4a5a-b69d-d97f3acede5e";
		String hostIdentityKeyProfileConfigKey = "27c1406d-02fb-4bef-a0bb-c4d2c098a6ee";
		String sponsorKey = "2405680d-5aea-425d-9773-28affc005bc0";

		MockInvokeApi.pemPartnerGet(httpClient, config, uniqueId, pemPartnerKey, pemPartnerName);
		MockInvokeApi.sfgInitiatingPartnerGet(httpClient, config, true, sfgPartnerKey, pemPartnerName, resourcePrefix,
				prodsfgUserAccountKey);
		MockInvokeApi.sfgInitiatingPartnerGet(httpClient, config, false, sfgPartnerKey, pemPartnerName, resourcePrefix,
				testsfgUserAccountKey);

		MockInvokeApi.pemSystemsGetBySfgPartnerExtension(httpClient, config, pemPartnerKey, sfgPartnerKey);

		// User Account Get
		MockInvokeApi.sfgUserAccountGet(httpClient, config, true, prodsfgUserAccountKey, sfgPartnerKey, pemPartnerName);
		MockInvokeApi.sfgUserAccountGet(httpClient, config, false, testsfgUserAccountKey, sfgPartnerKey,
				pemPartnerName);

		// User Credential import
		String pemUserCredProfileConfigName = resourcePrefix + "-" + uniqueId + "-UserCred";
		MockInvokeApi.pemProfileConfigsGet(httpClient, config, pemPartnerKey, USER_CRED, USER_CRED,
				pemUserCredProfileConfigName);
		String pemUserCredProfileConfigKey = MockInvokeApi.pemProfileConfigCreate(httpClient, config,
				pemUserCredProfileConfigName, pemPartnerKey, USER_CRED, USER_CRED, SPONSOR_DIVISION1_KEY);
		MockInvokeApi.pemUserCredentialsGet(httpClient, config, pemPartnerKey, pemUserCredProfileConfigKey);
		String pemUserCredKey = resourcePrefix + "-" + uniqueId + "-UserCredKey"; // "f2e14d69-473c-4a08-91b4-6ab9d56318da";
		MockInvokeApi.pemUserCredCreate(httpClient, config, pemUserCredProfileConfigKey, testsfgUserAccountKey,
				prodsfgUserAccountKey, pemUserCredKey);
		MockInvokeApi.pemUserCredMarkComplete(httpClient, config, pemUserCredKey);

		if (step >= 2) {
			// Get SFTP Outbound Pull DG Profile Config
			String sftpOutboundPullProfileConfigName = resourcePrefix + "sftpoutpull-dg";
			MockInvokeApi.pemProfileConfigDgGet(httpClient, config, sftpOutboundPullProfileConfigKey,
					sftpOutboundPullProfileConfigName, sponsorKey, SFTP, "SFTP_OUT_PULL", pemPartnerKey,
					SPONSOR_DIVISION1_KEY, SPONSOR_DIVISION1_KEY, "parentProfileConfigKey");
			String sftpOutboundPullKey = "7cd6351d-86da-47d1-a665-6fa7a445aab4";
			MockInvokeApi.pemSftpOutPullsDgGet(httpClient, config, sftpOutboundPullProfileConfigKey, sponsorKey,
					sftpOutboundPullKey, sftpOutboundPullProfileConfigName);
			MockInvokeApi.pemSftpOutPullMarkComplete(httpClient, config, sftpOutboundPullKey);
		}
		if (step >= 3) {
			// Get SFTP Inbound Push DG Profile Config
			String sftpInboundPushProfileConfigName = resourcePrefix + "sftpinbpush-dg";
			MockInvokeApi.pemProfileConfigDgGet(httpClient, config, sftpInboundPushProfileConfigKey,
					sftpInboundPushProfileConfigName, sponsorKey, SFTP, "SFTP_INB_PUSH", pemPartnerKey,
					SPONSOR_DIVISION1_KEY, SPONSOR_DIVISION1_KEY, "parentProfileConfigKey");
			String sftpInboundPushKey = "15f42b06-cbc1-4934-93fe-50b56721aac0";
			MockInvokeApi.pemSftpInbPushsDgGet(httpClient, config, sftpInboundPushProfileConfigKey, sponsorKey,
					sftpInboundPushKey, sftpInboundPushProfileConfigName);
			MockInvokeApi.pemSftpInbPushMarkComplete(httpClient, config, sftpInboundPushKey);
		}
		if (step >= 4) {
			// Get SSH Key DG Profile Config
			String hostIdentityKeyProfileConfigName = resourcePrefix + "sshKey-dg";
			String sshKeyKey = "d6f9bd43-cf1d-4ea1-b324-e1fa2beb0737";
			MockInvokeApi.pemSshKeysDgGet(httpClient, config, hostIdentityKeyProfileConfigKey, sponsorKey, sshKeyKey,
					hostIdentityKeyProfileConfigName);
			MockInvokeApi.pemSshKeyMarkComplete(httpClient, config, sshKeyKey);
		}
		if (step >= 5) {
			// System import
			String pemSystemProfileConfigName = resourcePrefix + "-" + uniqueId + "-" + SYSTEM;
			MockInvokeApi.pemProfileConfigsGet(httpClient, config, pemPartnerKey, SYSTEM, SYSTEM,
					pemSystemProfileConfigName);
			String pemSystemProfileConfigKey = MockInvokeApi.pemProfileConfigCreate(httpClient, config,
					pemSystemProfileConfigName, pemPartnerKey, SYSTEM, SYSTEM, SPONSOR_DIVISION1_KEY);
			MockInvokeApi.pemSystemsGet(httpClient, config, pemPartnerKey, pemSystemProfileConfigKey);
			String pemSystemKey = "59ed2283-293a-4c15-9a70-f158551c3d02";
			MockInvokeApi.pemSystemSftpInitiateCreate(httpClient, config, pemSystemProfileConfigKey, sfgPartnerKey,
					sftpInboundPushProfileConfigKey, sftpOutboundPullProfileConfigKey, hostIdentityKeyProfileConfigKey,
					pemUserCredProfileConfigKey, pemSystemKey);
			MockInvokeApi.pemSystemMarkComplete(httpClient, config, pemSystemKey);
		}
	}

	static void mockListeningPartnerAPIs(HttpClientUtil httpClient, Configuration config, String resourcePrefix,
			String uniqueId, boolean mockSuccessOn2ndAttemptScenario) throws Exception {
		String pemPartnerName = resourcePrefix + "-partner";
		String pemPartnerKey = pemPartnerName + "Key";
		String sfgPartnerKey = resourcePrefix + "-" + uniqueId;
		String remoteProfileKey = uniqueId + "ib" + resourcePrefix;
		String sshKnownHostKeyName = resourcePrefix + "-KnownHostKey";

		MockInvokeApi.pemPartnerGet(httpClient, config, uniqueId, pemPartnerKey, pemPartnerName);
		MockInvokeApi.sfgListeningPartnerGet(httpClient, config, true, sfgPartnerKey, pemPartnerName, resourcePrefix,
				remoteProfileKey);
		MockInvokeApi.sfgListeningPartnerGet(httpClient, config, false, sfgPartnerKey, pemPartnerName, resourcePrefix,
				remoteProfileKey);

		MockInvokeApi.pemSystemsGetBySfgPartnerExtension(httpClient, config, pemPartnerKey, sfgPartnerKey);

		// SSH Known Host Key import
		MockInvokeApi.sfgSshRemoteProfilesGet(httpClient, config, true, remoteProfileKey, sshKnownHostKeyName);
		MockInvokeApi.sfgSshRemoteProfilesGet(httpClient, config, false, remoteProfileKey, sshKnownHostKeyName);

		String pemSshKhkProfileConfigName = resourcePrefix + "-" + uniqueId + "-" + SSH_KEY;
		MockInvokeApi.pemProfileConfigsGet(httpClient, config, pemPartnerKey, SSH_KEY, SSH_KEY,
				pemSshKhkProfileConfigName);
		String pemSshKhkProfileConfigKey = MockInvokeApi.pemProfileConfigCreate(httpClient, config,
				pemSshKhkProfileConfigName, pemPartnerKey, SSH_KEY, SSH_KEY, SPONSOR_DIVISION1_KEY);
		MockInvokeApi.pemSshKeysGet(httpClient, config, pemSshKhkProfileConfigKey);

		MockInvokeApi.sfgSshKnownHostKeyGet(httpClient, config, true, sshKnownHostKeyName);
		MockInvokeApi.sfgSshKnownHostKeyGet(httpClient, config, false, sshKnownHostKeyName);

		String pemSshKhkKey = "07c3585a-f958-49a9-91b3-d42ed2eb4f78";
		MockInvokeApi.pemSshKeyCreate(httpClient, config, pemSshKhkProfileConfigKey, pemSshKhkKey);
		MockInvokeApi.pemSshKeyMarkComplete(httpClient, config, pemSshKhkKey);

		// User Credential import
		String pemUserCredProfileConfigName = resourcePrefix + "-" + uniqueId + "-UserCred";
		MockInvokeApi.pemProfileConfigsGet(httpClient, config, pemPartnerKey, USER_CRED, USER_CRED,
				pemUserCredProfileConfigName);
		String pemUserCredProfileConfigKey = MockInvokeApi.pemProfileConfigCreate(httpClient, config,
				pemUserCredProfileConfigName, pemPartnerKey, USER_CRED, USER_CRED, SPONSOR_DIVISION1_KEY);
		MockInvokeApi.pemUserCredentialsGet(httpClient, config, pemPartnerKey, pemUserCredProfileConfigKey);
		String pemUserCredKey = "f2e14d69-473c-4a08-91b4-6ab9d56318da";
		MockInvokeApi.pemUserCredCreate(httpClient, config, pemUserCredProfileConfigKey, "admin", "admin",
				pemUserCredKey);
		MockInvokeApi.pemUserCredMarkComplete(httpClient, config, pemUserCredKey);

		// SFTP Inbound Pull import
		String pemSftpInbPullProfileConfigName = resourcePrefix + "-" + uniqueId + "-SftpInbPull";
		MockInvokeApi.pemProfileConfigsGet(httpClient, config, pemPartnerKey, SFTP, "SFTP_INB_PULL",
				pemSftpInbPullProfileConfigName);
		String pemSftpInbPullProfileConfigKey = MockInvokeApi.pemProfileConfigCreate(httpClient, config,
				pemSftpInbPullProfileConfigName, pemPartnerKey, SFTP, "SFTP_INB_PULL", SPONSOR_DIVISION1_KEY);
		MockInvokeApi.pemSftpInbPullsGet(httpClient, config, pemPartnerKey, pemSftpInbPullProfileConfigKey);
		String pemSftpInbPullKey = "05dad188-abf1-462b-8bec-82abd5f9eb85";
		MockInvokeApi.pemSftpInbPullCreate(httpClient, config, pemSftpInbPullProfileConfigKey, remoteProfileKey,
				pemUserCredProfileConfigKey, pemSshKhkProfileConfigKey, pemSftpInbPullKey);
		MockInvokeApi.pemSftpInbPullMarkComplete(httpClient, config, pemSftpInbPullKey);

		// SFTP Outbound Push import
		String pemSftpOutPushProfileConfigName = resourcePrefix + "-" + uniqueId + "-SftpOutPush";
		MockInvokeApi.pemProfileConfigsGet(httpClient, config, pemPartnerKey, SFTP, "SFTP_OUT_PUSH",
				pemSftpOutPushProfileConfigName);
		String pemSftpOutPushProfileConfigKey = MockInvokeApi.pemProfileConfigCreate(httpClient, config,
				pemSftpOutPushProfileConfigName, pemPartnerKey, SFTP, "SFTP_OUT_PUSH", SPONSOR_DIVISION1_KEY);
		MockInvokeApi.pemSftpOutPushsGet(httpClient, config, pemPartnerKey, pemSftpOutPushProfileConfigKey);
		String pemSftpOutPushKey = "46e85e96-cff7-413a-9c0d-d596f93caac7";
		MockInvokeApi.pemSftpOutPushCreate(httpClient, config, pemSftpOutPushProfileConfigKey, remoteProfileKey,
				pemUserCredProfileConfigKey, pemSshKhkProfileConfigKey, pemSftpOutPushKey);
		MockInvokeApi.pemSftpOutPushMarkComplete(httpClient, config, pemSftpOutPushKey);

		// System import
		String pemSystemProfileConfigName = resourcePrefix + "-" + uniqueId + "-" + SYSTEM;
		MockInvokeApi.pemProfileConfigsGet(httpClient, config, pemPartnerKey, SYSTEM, SYSTEM,
				pemSystemProfileConfigName);
		String pemSystemProfileConfigKey = MockInvokeApi.pemProfileConfigCreate(httpClient, config,
				pemSystemProfileConfigName, pemPartnerKey, SYSTEM, SYSTEM, SPONSOR_DIVISION1_KEY);
		MockInvokeApi.pemSystemsGet(httpClient, config, pemPartnerKey, pemSystemProfileConfigKey);
		String pemSystemKey = "46e85e96-cff7-413a-9c0d-d596f93caac7";
		MockInvokeApi.pemSystemSftpListenCreate(httpClient, config, pemSystemProfileConfigKey, sfgPartnerKey,
				pemSftpInbPullProfileConfigKey, pemSftpOutPushProfileConfigKey, pemSshKhkProfileConfigKey,
				pemUserCredProfileConfigKey, pemSystemKey);
		if (!mockSuccessOn2ndAttemptScenario) {
			MockInvokeApi.pemSystemMarkComplete(httpClient, config, pemSystemKey);
		} else {
			ApiResponse apiOutputError = Mockito.mock(ApiResponse.class);
			when(apiOutputError.getStatusCode()).thenReturn("400");
			when(apiOutputError.getResponse()).thenReturn("<error code=\"400\" errorDescription=\"dummy error1\" />");
			MockInvokeApi.pemSystemMarkComplete(httpClient, config, pemSystemKey, apiOutputError);
		}
	}

}
