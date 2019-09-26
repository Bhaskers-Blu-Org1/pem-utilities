/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem;

import org.junit.Test;

import com.ibm.pem.utilities.Configuration;

public class ConfigurationTest {

	@Test
	public void testConfigurationLoad() throws Exception {
		Configuration config = new Configuration();
		config.load("src/test/resources/Config-test01.properties");
	}

}
