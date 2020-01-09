/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import com.ibm.pem.utilities.ConfigBuilder;
import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.util.ResourceFactory;

public abstract class Sfg2PemTest {

	protected static final String REPORT_TEST_CSV_FILE_NAME = "report_TEST.csv";
	protected static final String REPORT_PROD_CSV_FILE_NAME = "report_PROD.csv";
	protected static final String CONFIG_FILE_NAME = "config.properties";
	protected static final String TEST_RESOURCES_SOURCE_DIR = "src/test/resources/";
	protected static final String TEST_RESOURCES_TARGET_DIR = "build/test-install/";

	protected static ConfigBuilder newConfigBuilder(String installDir, ResourceFactory resourceFactory)
			throws IOException {
		ConfigBuilder cb = new ConfigBuilder(installDir + CONFIG_FILE_NAME);
		cb.setResourceFactory(resourceFactory);
		cb.setProperty(Configuration.OUTPUT_FILE_DIRECTORY, installDir);
		cb.setProperty(Configuration.XSLT_DIR, "resource/xslt");
		return cb;
	}

	protected static Configuration newConfig(String installDir, ResourceFactory resourceFactory)
			throws IOException, ValidationException {
		return newConfigBuilder(installDir, resourceFactory).build();
	}

	protected static String readFileAsString(String filePath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
	}

	/**
	 * Copies all test resources from the specified directory to the target.
	 */
	protected static void copyTestResources(String testDirName) throws IOException {
		File sourceDir = new File(TEST_RESOURCES_SOURCE_DIR + testDirName);
		File destDir = new File(TEST_RESOURCES_TARGET_DIR + testDirName);
		if (destDir.exists()) {
			destDir.delete();
		}
		FileUtils.copyDirectory(sourceDir, destDir);
	}

	/**
	 * Copies the specified config.properties file to the target.
	 */
	protected static void copyConfigFile(String testDirName, String sourceConfigFilePath) throws IOException {
		File sourceFile = new File(sourceConfigFilePath);
		File destFile = new File(TEST_RESOURCES_TARGET_DIR + testDirName + "/" + CONFIG_FILE_NAME);
		FileUtils.copyFile(sourceFile, destFile);
	}

	protected static String getTestInstallDir(String testDirName) {
		return TEST_RESOURCES_TARGET_DIR + testDirName + "/";
	}

	protected static String getCommonDataDir() {
		return TEST_RESOURCES_SOURCE_DIR + "data/";
	}

}
