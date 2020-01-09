/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ibm.pem.utilities.ConfigBuilder;
import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.util.ResourceFactory;

public class ConfigurationTest extends Sfg2PemTest {

	@Test
	public void testConfigLoadFromFile() throws Exception {
		String testDirName = "testConfig01";
		String installDir = getTestInstallDir(testDirName);
		copyTestResources(testDirName);

		Configuration config = newConfig(installDir, new ResourceFactory());

		assertEquals("Mode is ", "ExtractSFGPartnerData", config.getMode());
	}

	@Test
	public void testConfigBuild() throws Exception {
		String testDirName = "testConfig02";
		String installDir = getTestInstallDir(testDirName);

		ConfigBuilder cb = new ConfigBuilder();
		cb.setProperty(Configuration.OUTPUT_FILE_DIRECTORY, installDir);
		cb.setProperty("installDirectory", installDir);
		cb.setProperty("MODE", "ExtractSFGPartnerData");
		cb.setProperty("PROTOCOL", "https");
		cb.setProperty("CONTEXT_URI", "b2b");
		cb.setProperty("PR.HOST_NAME", "localhost");
		cb.setProperty("SFG.PROD.PROTOCOL", "https");
		cb.setProperty("SFG.PROD.HOST_NAME", "localhost");
		cb.setProperty("SFG.PROD.CONTEXT_URI", "B2BAPIs");
		cb.setProperty("SFG.TEST.PROTOCOL", "https");
		cb.setProperty("SFG.TEST.HOST_NAME", "localhost");
		cb.setProperty("SFG.TEST.CONTEXT_URI", "B2BAPIs");

		cb.setProperty("disableConsole", "true");
		cb.setProperty("USERNAME", "admin");
		cb.setProperty("PASSWORD", "password");
		cb.setProperty("SFG.PROD.USERNAME", "admin");
		cb.setProperty("SFG.PROD.PASSWORD", "password");
		cb.setProperty("SFG.TEST.USERNAME", "admin");
		cb.setProperty("SFG.TEST.PASSWORD", "password");

		cb.setProperty("ProxyAuthentication", "false");
		cb.setProperty("DELIMITER", "~");

		cb.setProperty("SFGToPEMMigration.SFGPartnerDataFileName.Prod", "report-prod.csv");
		cb.setProperty("SFGToPEMMigration.SFGPartnerDataFileName.Test", "report-test.csv");

		Configuration config = cb.build();
		assertNotNull(config);
	}

	@Test
	public void testConfigLoadMissingInstallDirError() throws Exception {
		ConfigBuilder cb = new ConfigBuilder();
		try {
			Configuration config = cb.build();
			assertNotNull(config);
		} catch (ValidationException e) {
			assertEquals("Provide a valid non-empty value for property: installDirectory", e.getMessage());
		}
	}

	@Test
	public void testConfigLoadMissingModeError() throws Exception {
		String testDirName = "testConfig01/errorMissingMode";
		String installDir = getTestInstallDir(testDirName);

		ConfigBuilder cb = new ConfigBuilder();
		cb.setProperty(Configuration.OUTPUT_FILE_DIRECTORY, installDir);
		cb.setProperty("installDirectory", installDir);
		try {
			Configuration config = cb.build();
			assertNotNull(config);
		} catch (ValidationException e) {
			assertEquals("Provide a valid non-empty value for property: MODE", e.getMessage());
		}
	}

	@Test
	public void testConfigLoadUnrecognizedModeError() throws Exception {
		String testDirName = "testConfig01/errorMissingMode";
		String installDir = getTestInstallDir(testDirName);

		ConfigBuilder cb = new ConfigBuilder();
		cb.setProperty(Configuration.OUTPUT_FILE_DIRECTORY, installDir);
		cb.setProperty("installDirectory", installDir);
		cb.setProperty("MODE", "somemode");
		try {
			Configuration config = cb.build();
			assertNotNull(config);
		} catch (ValidationException e) {
			assertEquals("Please provide a valid value for property: MODE", e.getMessage());
		}
	}

}
