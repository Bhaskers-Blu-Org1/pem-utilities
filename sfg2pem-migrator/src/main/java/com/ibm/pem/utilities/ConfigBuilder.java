/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.ibm.pem.utilities.sfg2pem.ValidationException;
import com.ibm.pem.utilities.util.ResourceFactory;

public class ConfigBuilder {

	private Properties props;

	private ResourceFactory resourceFactory;

	public ConfigBuilder() {
		props = new Properties();
	}

	public ConfigBuilder(String configFile) throws IOException {
		this();
		load(configFile);
	}

	private void load(String configFile) throws IOException {
		FileInputStream is = null;
		try {
			File file = new File(configFile);
			is = new FileInputStream(file);
			props.load(is);
			String installDirectory = new File(file.getAbsolutePath()).getParent();
			if (installDirectory == null) {
				installDirectory = ".";
			}
			props.setProperty("installDirectory", installDirectory);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public ConfigBuilder setResourceFactory(ResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
		return this;
	}

	public ConfigBuilder setProperty(String key, String value) {
		props.setProperty(key, value);
		return this;
	}

	public Configuration build() throws ValidationException, IOException {
		if (resourceFactory != null) {
			return new Configuration(resourceFactory).load(props);
		} else {
			return new Configuration().load(props);
		}
	}

}
