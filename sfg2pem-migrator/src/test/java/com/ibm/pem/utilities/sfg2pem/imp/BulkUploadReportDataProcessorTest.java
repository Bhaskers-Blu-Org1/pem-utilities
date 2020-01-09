/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.Sfg2PemTest;

public class BulkUploadReportDataProcessorTest extends Sfg2PemTest {

	@Test
	public void testGeneratePartnerGroupIdMap() throws Exception {
		String testDirName = "testGeneratePartnerGroupIdMap/success01";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + "/config-import.properties");

		Configuration config = newConfigBuilder(installDir, null)
				.setProperty(Configuration.PEM_PARTNER_BULK_UPLOAD_REPORT_FILE, "PartnerBulkUpload_Report.csv").build();

		BulkUploadReportDataProcessor bulkUploadReportDataProcessor = new BulkUploadReportDataProcessor(config);
		HashMap<String, String> partnerGroupIdMap = bulkUploadReportDataProcessor.execute();

		// Verify how many records have been successfully processed.
		assertEquals(2, partnerGroupIdMap.size());
		assertEquals("test01-partnerKey", partnerGroupIdMap.get("#pgid-01"));
		assertEquals("test02-partnerKey", partnerGroupIdMap.get("#pgid-02"));
	}

	@Test
	public void testGeneratePartnerGroupIdMapError() throws Exception {
		String testDirName = "testGeneratePartnerGroupIdMap/validationErrors";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + "/config-import.properties");

		Configuration config = newConfigBuilder(installDir, null)
				.setProperty(Configuration.PEM_PARTNER_BULK_UPLOAD_REPORT_FILE, "PartnerBulkUpload_Report.csv").build();

		BulkUploadReportDataProcessor bulkUploadReportDataProcessor = new BulkUploadReportDataProcessor(config);
		HashMap<String, String> partnerGroupIdMap = bulkUploadReportDataProcessor.execute();

		// Verify how many records have been successfully processed.
		assertEquals(1, partnerGroupIdMap.size());
		assertEquals("test03-partnerKey", partnerGroupIdMap.get("#pgid-03"));
	}

}
