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
import com.ibm.pem.utilities.sfg2pem.SfgPartnerReportReader;

public class TestSfgPartnerDataProcessorTest extends Sfg2PemTest {

	@Test
	public void testGenerateSystemReferenceMap() throws Exception {
		String testDirName = "testGenerateSystemReferenceMap";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);
		copyConfigFile(testDirName, TEST_RESOURCES_SOURCE_DIR + "/config-import.properties");

		Configuration config = newConfig(installDir, null);

		TestSfgPartnerDataProcessor testSfgPartnerDataProcessor = new TestSfgPartnerDataProcessor(config);
		HashMap<String, String> testSfgPartnerMap = testSfgPartnerDataProcessor.execute();

		// Verify how many records have been successfully processed from test SFG env.
		assertEquals(3, testSfgPartnerMap.size());
		assertEquals("partner01", testSfgPartnerMap.get("systemRef01"));
		assertEquals("partner02", testSfgPartnerMap.get("systemRef02"));
		assertEquals("partner04", testSfgPartnerMap.get("systemRef04"));

		// Verify the number of partner records available for processing.
		HashMap<String, PartnerInfo> partnerMap = new SfgPartnerReportReader(config)
				.execute(installDir + REPORT_TEST_CSV_FILE_NAME);

		assertEquals("Number of partners processed is different than expected.", 3,
				partnerMap.size() - testSfgPartnerMap.size());
	}

}
