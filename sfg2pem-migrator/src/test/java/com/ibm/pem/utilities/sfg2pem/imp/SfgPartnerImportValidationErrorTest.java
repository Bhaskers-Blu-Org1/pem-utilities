/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;

import org.apache.http.HttpException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.Sfg2PemTest;
import com.ibm.pem.utilities.sfg2pem.SfgPartnerReportReader;
import com.ibm.pem.utilities.sfg2pem.ValidationException;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo.ProcessingStatus;
import com.ibm.pem.utilities.util.ApiResponse;
import com.ibm.pem.utilities.util.HttpClientUtil;
import com.ibm.pem.utilities.util.ResourceFactory;

@RunWith(MockitoJUnitRunner.class)
public class SfgPartnerImportValidationErrorTest extends Sfg2PemTest {

	@Mock
	private ResourceFactory resourceFactory;

	@Mock
	private HttpClientUtil httpClient;

	@Before
	public void setUp() {
		Mockito.when(resourceFactory.createHttpClientInstance()).thenReturn(httpClient);
	}

	@Test
	public void testImportValidationError01() throws Exception {
		String testDirName = "testImportValidationError01";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + "/config-import.properties");

		Configuration config = newConfig(installDir, resourceFactory);

		mockPemGetPartnerApi(config, "pemPartner04");
		mockPemGetPartnerApi(config, "pemPartner05");
		mockPemGetPartnerApi(config, "pemPartner09");
		mockPemGetPartnerApi(config, "pemPartner10");

		new ImportHandler(config).execute();

		// Read the output and verify content.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);

		assertEquals(ProcessingStatus.ERROR, partnerMap.get("partner01").getProcessingStatus());
		assertEquals("PEM Partner Key not provided.", partnerMap.get("partner01").getMessage());

		assertEquals(ProcessingStatus.ERROR, partnerMap.get("partner02").getProcessingStatus());
		assertEquals("Sponsor Division Key not provided.", partnerMap.get("partner02").getMessage());

		assertEquals(ProcessingStatus.ERROR, partnerMap.get("partner03").getProcessingStatus());
		assertEquals("PR System Ref not provided.", partnerMap.get("partner03").getMessage());

		assertEquals(ProcessingStatus.ERROR, partnerMap.get("partner04").getProcessingStatus());
		assertEquals("No partner reference specified from SFG-Test env.", partnerMap.get("partner04").getMessage());

		assertEquals(ProcessingStatus.ERROR, partnerMap.get("partner05").getProcessingStatus());
		assertEquals("code = 404, message = Not found", partnerMap.get("partner05").getMessage());

		assertEquals(ProcessingStatus.ERROR, partnerMap.get("partner06").getProcessingStatus());
		assertEquals("Processing Status not provided or is not supported.", partnerMap.get("partner06").getMessage());

		// Verify scenario where SFG Partner Key is not available.
		assertEquals(ProcessingStatus.ERROR, partnerMap.get("").getProcessingStatus());
		assertEquals("SFG Partner Key not provided.", partnerMap.get("").getMessage());

		// Verify partner with status as Ignored
		assertEquals(ProcessingStatus.IGNORED, partnerMap.get("partner08").getProcessingStatus());
		assertNull(partnerMap.get("partner08").getMessage());

		assertEquals(8, partnerMap.size());
	}

	private void mockPemGetPartnerApi(Configuration config, String partnerKey)
			throws Exception, HttpException, IOException, URISyntaxException, KeyManagementException,
			NoSuchAlgorithmException, CertificateException, KeyStoreException, ValidationException {
		String url = config.getPrRestURL() + "partners/" + partnerKey + "/";
		String userName = config.getUserName();
		char[] password = config.getPassword();
		String host = config.getPrHostName();
		ApiResponse apiOutput = new ApiResponse();
		if (partnerKey.contentEquals("pemPartner05")) {
			apiOutput.setStatusCode("404");
			apiOutput.setStatusLine("Not found");
		} else {
			apiOutput.setStatusCode("200");
		}
		apiOutput.setResponse("");

		Mockito.when(httpClient.doGet(eq(url), anyMap(), eq(userName), eq(password), eq(config), eq(host)))
				.thenReturn(apiOutput);
	}

}
