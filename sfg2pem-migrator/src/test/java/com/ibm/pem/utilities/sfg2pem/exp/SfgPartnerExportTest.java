/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.exp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.Constants;
import com.ibm.pem.utilities.sfg2pem.Sfg2PemTest;
import com.ibm.pem.utilities.sfg2pem.SfgPartnerReportReader;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo;
import com.ibm.pem.utilities.util.ApiResponse;
import com.ibm.pem.utilities.util.HttpClientUtil;
import com.ibm.pem.utilities.util.ResourceFactory;

@RunWith(MockitoJUnitRunner.class)
public class SfgPartnerExportTest extends Sfg2PemTest {

	@Mock
	private ResourceFactory resourceFactory;

	@Mock
	private HttpClientUtil httpClient;

	@Before
	public void setup() {
		Mockito.when(resourceFactory.createHttpClientInstance()).thenReturn(httpClient);
	}

	@Test
	public void testSfgPartnerExport() throws Exception {
		String testDirName = "testExport01";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);

		Configuration config = newConfig(installDir, resourceFactory);

		mockSfgGetPartnersApi(config, false, testDirName);
		mockSfgGetPartnersApi(config, true, testDirName);

		new SFGPartnerExportHandler(config).execute();

		assertTrue("Test output file not created.", config.getTestOutPutFile().exists());
		assertTrue("Prod output file not created.", config.getProdOutPutFile().exists());

		// Read the output and verify content.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);
		assertEquals("Number of prod SFG partners exported is different than expected.", 3, partnerMap.size());
		assertTrue("Partner test01-id01 not found.", partnerMap.containsKey("test01-id01"));
		assertTrue("Partner test02-id02 not found.", partnerMap.containsKey("test02-id02"));

		partnerMap = new SfgPartnerReportReader(config).execute(installDir + REPORT_TEST_CSV_FILE_NAME);
		assertEquals("Number of test SFG partners exported is different than expected.", 3, partnerMap.size());
		assertTrue("Partner test01-id01 not found.", partnerMap.containsKey("test01-id01"));
		assertTrue("Partner test02-id02 not found.", partnerMap.containsKey("test02-id02"));

	}

	private void mockSfgGetPartnersApi(Configuration config, boolean getFromProductionSfg, String testDirName)
			throws Exception {
		String url = getFromProductionSfg ? config.getSfgProdRestURL() : config.getSfgTestRestURL();
		url += Constants.SFG_PARTNER_REST_URI;
		String userName = getFromProductionSfg ? config.getSfgProdUserName() : config.getSfgTestUserName();
		char[] password = getFromProductionSfg ? config.getSfgProdPassword() : config.getSfgTestPassword();
		String host = getFromProductionSfg ? config.getSfgProdHost() : config.getSfgTestHost();
		ApiResponse apiOutput = new ApiResponse();
		apiOutput.setStatusCode("200");
		String testResponseFile = getFromProductionSfg ? "/sfg-prod-export.json" : "/sfg-test-export.json";
		apiOutput.setResponse(readFileAsString(getTestInstallDir(testDirName) + testResponseFile));

		Mockito.when(httpClient.doGet(eq(url), anyMap(), eq(userName), eq(password), eq(config), eq(host)))
				.thenReturn(apiOutput);
	}

}
