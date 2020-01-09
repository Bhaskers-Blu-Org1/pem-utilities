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
import org.mockito.junit.MockitoJUnitRunner;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.Sfg2PemTest;
import com.ibm.pem.utilities.sfg2pem.SfgPartnerReportReader;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo.ProcessingStatus;
import com.ibm.pem.utilities.util.HttpClientUtil;
import com.ibm.pem.utilities.util.ResourceFactory;

@RunWith(MockitoJUnitRunner.class)
public class SfgPartnerSftpImportWithBulkUploadReportTest extends Sfg2PemTest {

	private static final String CONFIG_IMPORT_PROPERTIES_FILE_NAME = "/config-import.properties";

	@Mock
	private ResourceFactory resourceFactory;

	@Mock
	private HttpClientUtil httpClient;

	@Before
	public void setUp() {
		when(resourceFactory.createHttpClientInstance()).thenReturn(httpClient);
	}

	@Test
	public void testGeneratePartnerBulkUploadInputFile() throws Exception {
		String testDirName = "testImportSfgPartnersWithBulkUploadReport/generatePartnerBulkUploadInputFile";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);
		Configuration config = newConfig(installDir, resourceFactory);

		new ImportHandler(config).execute();

		// Read the output and verify content.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_PROD_CSV_FILE_NAME);

		String sfgPartnerKey = "test01-id01";
		assertEquals(ProcessingStatus.PARTNER_IMPORT_PENDING, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertEquals("Partner needs to be imported to PEM.", partnerMap.get(sfgPartnerKey).getMessage());

		sfgPartnerKey = "test02-id02";
		assertEquals(ProcessingStatus.PARTNER_IMPORT_PENDING, partnerMap.get(sfgPartnerKey).getProcessingStatus());
		assertEquals("Partner needs to be imported to PEM.", partnerMap.get(sfgPartnerKey).getMessage());

		assertEquals(2, partnerMap.size());
	}

	@Test
	public void testImportWithPartnerBulkUploadReportSuccess() throws Exception {
		String testDirName = "testImportSfgPartnersWithBulkUploadReport/success01";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + CONFIG_IMPORT_PROPERTIES_FILE_NAME);
		Configuration config = newConfigBuilder(installDir, resourceFactory)
				.setProperty(Configuration.PEM_PARTNER_BULK_UPLOAD_REPORT_FILE, "PartnerBulkUpload_Report.csv").build();

		SfgPartnerSftpImportTest.mockListeningPartnerAPIs(httpClient, config, "test01", "id01", false);
		SfgPartnerSftpImportTest.mockListeningPartnerAPIs(httpClient, config, "test02", "id02", false);

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

}
