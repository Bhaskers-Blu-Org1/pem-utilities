/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Test;
import org.w3c.dom.Document;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ApiInvocationException;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.Sfg2PemTest;
import com.ibm.pem.utilities.sfg2pem.ValidationException;

public class ImportHandlerFactoryTest extends Sfg2PemTest {

	private static final String PROP_IMPORT_HANDLER_1_CLASS = "ImportSFGPartnerDataToPEM.handler.1.class";
	private static final String PROP_IMPORT_HANDLER_2_CLASS = "ImportSFGPartnerDataToPEM.handler.2.class";

	@Test
	public void testFirstHandlerMatch() throws Exception {
		Configuration config = mock(Configuration.class);
		Properties props = new Properties();
		props.put(PROP_IMPORT_HANDLER_1_CLASS, AlwaysAcceptMatchImportHandler.class.getName());
		props.put(PROP_IMPORT_HANDLER_2_CLASS, AlwaysRejectMatchImportHandler.class.getName());
		when(config.getProps()).thenReturn(props);
		PrConfigurationImportHandler handler = ImportHandlerFactory.buildInstance(config)
				.getConfigurationHandler(mock(Document.class), mock(Document.class));
		assertNotNull("No matching handler found.", handler);
		assertEquals(AlwaysAcceptMatchImportHandler.class, handler.getClass());
	}

	@Test
	public void testSecondHandlerMatch() throws Exception {
		Configuration config = mock(Configuration.class);
		Properties props = new Properties();
		props.put(PROP_IMPORT_HANDLER_1_CLASS, AlwaysRejectMatchImportHandler.class.getName());
		props.put(PROP_IMPORT_HANDLER_2_CLASS, AlwaysAcceptMatchImportHandler.class.getName());
		when(config.getProps()).thenReturn(props);
		PrConfigurationImportHandler handler = ImportHandlerFactory.buildInstance(config)
				.getConfigurationHandler(mock(Document.class), mock(Document.class));
		assertNotNull("No matching handler found.", handler);
		assertEquals(AlwaysAcceptMatchImportHandler.class, handler.getClass());
	}

	@Test
	public void testNoHandlerMatch() throws Exception {
		Configuration config = mock(Configuration.class);
		Properties props = new Properties();
		props.put(PROP_IMPORT_HANDLER_1_CLASS, AlwaysRejectMatchImportHandler.class.getName());
		props.put(PROP_IMPORT_HANDLER_2_CLASS, AlwaysRejectMatchImportHandler.class.getName());
		when(config.getProps()).thenReturn(props);
		PrConfigurationImportHandler handler = ImportHandlerFactory.buildInstance(config)
				.getConfigurationHandler(mock(Document.class), mock(Document.class));
		assertNull("Found a matching handler when expected not to.", handler);
	}

	@Test
	public void testUnrecognizedHandler() throws Exception {
		Configuration config = mock(Configuration.class);
		Properties props = new Properties();
		props.put(PROP_IMPORT_HANDLER_1_CLASS, "com.ibm.nonExistentHandlerClass");
		when(config.getProps()).thenReturn(props);
		try {
			ImportHandlerFactory.buildInstance(config);
			fail("Method call successful but was expected to fail!");
		} catch (ValidationException e) {
			assertEquals(ClassNotFoundException.class, e.getCause().getClass());
		}
	}

	@Test
	public void testNoHandlerAvailable() throws Exception {
		Configuration config = mock(Configuration.class);
		Properties props = new Properties();
		when(config.getProps()).thenReturn(props);
		try {
			ImportHandlerFactory.buildInstance(config).getConfigurationHandler(mock(Document.class),
					mock(Document.class));
			fail("Method call successful but was expected to fail!");
		} catch (ValidationException e) {
			assertEquals("No import handlers found.", e.getMessage());
		}
	}

	private static class AlwaysRejectMatchImportHandler extends PrConfigurationImportHandler {

		public AlwaysRejectMatchImportHandler(Configuration config) {
			super(config);
		}

		@Override
		public boolean accept(Document partnerInfo, Document testSfgPartner) {
			return false;
		}

		@Override
		protected void doExecute(PartnerInfo partnerInfo)
				throws ImportException, ApiInvocationException, ValidationException {
		}

	}

	private static class AlwaysAcceptMatchImportHandler extends PrConfigurationImportHandler {

		public AlwaysAcceptMatchImportHandler(Configuration config) {
			super(config);
		}

		@Override
		public boolean accept(Document partnerInfo, Document testSfgPartner) {
			return true;
		}

		@Override
		protected void doExecute(PartnerInfo partnerInfo)
				throws ImportException, ApiInvocationException, ValidationException {
		}

	}

}
