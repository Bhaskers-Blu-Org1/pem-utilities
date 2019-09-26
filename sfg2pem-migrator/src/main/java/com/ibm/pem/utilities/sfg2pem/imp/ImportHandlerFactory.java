/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.Constants;
import com.ibm.pem.utilities.sfg2pem.ValidationException;

public class ImportHandlerFactory {

	private static final Logger LOG = LoggerFactory.getLogger(ImportHandlerFactory.class);

	private Configuration config;
	private List<String> configHandlers;

	private ImportHandlerFactory(Configuration config, List<String> configHandlers) {
		this.config = config;
		this.configHandlers = configHandlers;
	}

	public static ImportHandlerFactory buildInstance(Configuration config) throws ValidationException {
		List<String> configHandlers = new ArrayList<>();
		HashMap<Integer, String> map = new HashMap<>();

		if (LOG.isInfoEnabled()) {
			LOG.info("Fetching import handlers.");
		}
		Pattern pattern = Pattern.compile(Constants.MODE_IMPORT_SFG_PARTNER_DATA_TO_PEM + ".handler.[0-9]+.class");
		Properties props = config.getProps();
		Iterator<Object> iter = props.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (pattern.matcher(key).matches()) {
				String handlerClassName = props.getProperty(key);
				try {
					@SuppressWarnings("unchecked")
					Class<PrConfigurationImportHandler> theClass = (Class<PrConfigurationImportHandler>) Class
							.forName(handlerClassName);
					theClass.getConstructor(Configuration.class).newInstance(config);
					// successfully instantiated the plugin class.
					map.put(Integer
							.parseInt(key.replaceFirst(Constants.MODE_IMPORT_SFG_PARTNER_DATA_TO_PEM + ".handler.", "")
									.replaceFirst(".class", "")),
							handlerClassName);
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException | NoSuchMethodException
						| SecurityException e) {
					throw new ValidationException(e);
				}
			}
		}
		Integer[] handlerIdsInSequence = map.keySet().toArray(new Integer[map.keySet().size()]);
		Arrays.sort(handlerIdsInSequence);
		for (int i : handlerIdsInSequence) {
			configHandlers.add(map.get(i));
		}

		if (configHandlers.size() == 0) {
			throw new ValidationException("No import handlers found.");
		}

		return new ImportHandlerFactory(config, configHandlers);
	}

	public PrConfigurationImportHandler getConfigurationHandler(Document prodSfgPartner, Document testSfgPartner) throws ValidationException {
		// Iterate through all plugins and let them decide if they want to process the
		// data or not.
		for (String handlerClassName : configHandlers) {
			try {
				@SuppressWarnings("unchecked")
				Class<PrConfigurationImportHandler> theClass = (Class<PrConfigurationImportHandler>) Class
						.forName(handlerClassName);
				PrConfigurationImportHandler handler = theClass.getConstructor(Configuration.class).newInstance(config);
				if (handler.accept(prodSfgPartner, testSfgPartner)) {
					return handler;
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new ValidationException(e);
			}
		}
		return null;
	}

}
