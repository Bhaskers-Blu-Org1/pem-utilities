/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.exp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.http.Header;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.Constants;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.Sfg2PemTest;
import com.ibm.pem.utilities.sfg2pem.imp.ImportHelper;
import com.ibm.pem.utilities.util.ApiResponse;
import com.ibm.pem.utilities.util.HttpClientUtil;
import com.ibm.pem.utilities.util.ResourceFactory;

@RunWith(MockitoJUnitRunner.class)
public class GetSfgPartnerApiPaginationTest extends Sfg2PemTest {

	private Configuration config;
	private HttpClientUtil httpClient;
	private String url;
	private char[] password;

	@Before
	public void setUp() {
		config = mock(Configuration.class);
		ResourceFactory resourceFactory = mock(ResourceFactory.class);
		httpClient = mock(HttpClientUtil.class);

		when(config.getResourceFactory()).thenReturn(resourceFactory);
		when(resourceFactory.createHttpClientInstance()).thenReturn(httpClient);

		String host = "sfgprod.com";
		url = "http://" + host + "/";
		when(config.getSfgProdRestURL()).thenReturn(url);

		when(config.getSfgProdUserName()).thenReturn("admin");
		password = "password".toCharArray();
		when(config.getSfgProdPassword()).thenReturn(password);
		when(config.getSfgProdHost()).thenReturn(host);

		url += Constants.SFG_PARTNER_REST_URI;
	}

	@Test
	public void testGetSfgApiSucessWith3Iterations() throws Exception {
		ApiResponse apiOutput1 = mockApiResponse(1);
		when(httpClient.doGet(eq(url), anyMap(), anyString(), eq(password), eq(config), anyString()))
				.thenReturn(apiOutput1);
		ApiResponse apiOutput2 = mockApiResponse(2);
		when(httpClient.doGet(eq(url + "?_range=1000-1999"), anyMap(), anyString(), eq(password), eq(config),
				anyString())).thenReturn(apiOutput2);
		ApiResponse apiOutput3 = mockApiResponse(3);
		when(httpClient.doGet(eq(url + "?_range=2000-2999"), anyMap(), anyString(), eq(password), eq(config),
				anyString())).thenReturn(apiOutput3);

		List<ApiResponse> response = ImportHelper.getResourceListFromSFG(true, config, Constants.SFG_PARTNER_REST_URI);

		assertEquals(3, response.size());
		assertEquals("<dummyresponse1 />", response.get(0).getResponse());
		assertEquals("<dummyresponse2 />", response.get(1).getResponse());
		assertEquals("<dummyresponse3 />", response.get(2).getResponse());
	}

	@Test
	public void testGetSfgApiError() throws Exception {
		ApiResponse apiOutput = mock(ApiResponse.class);
		when(apiOutput.getStatusCode()).thenReturn("400");
		when(apiOutput.getResponse()).thenReturn("<error code=\"400\" errorDescription=\"dummy error1\" />");

		when(httpClient.doGet(eq(url), anyMap(), anyString(), eq(password), eq(config), anyString()))
				.thenReturn(apiOutput);

		try {
			ImportHelper.getResourceListFromSFG(true, config, Constants.SFG_PARTNER_REST_URI);
			fail("Method call successful but was expected to fail!");
		} catch (ImportException e) {
			assertEquals("dummy error1", e.getMessage());
		}
	}

	@Test
	public void testGetSfgApiError2() throws Exception {
		ApiResponse apiOutput = mock(ApiResponse.class);
		when(apiOutput.getStatusCode()).thenReturn("500");
		String errorMessage = "some error message.";
		when(apiOutput.getResponse()).thenReturn(errorMessage);

		when(httpClient.doGet(eq(url), anyMap(), anyString(), eq(password), eq(config), anyString()))
				.thenReturn(apiOutput);

		try {
			ImportHelper.getResourceListFromSFG(true, config, Constants.SFG_PARTNER_REST_URI);
			fail("Method call successful but was expected to fail!");
		} catch (ImportException e) {
			assertEquals(errorMessage, e.getMessage());
		}
	}

	@Test
	public void testGetSfgApiWithErrorOn2ndIteration() throws Exception {
		ApiResponse apiOutput1 = mockApiResponse(1);
		when(httpClient.doGet(eq(url), anyMap(), anyString(), eq(password), eq(config), anyString()))
				.thenReturn(apiOutput1);

		ApiResponse apiOutput2 = mock(ApiResponse.class);
		when(apiOutput2.getStatusCode()).thenReturn("400");
		when(apiOutput2.getResponse()).thenReturn("<error code=\"400\" errorDescription=\"dummy error1\" />");
		when(httpClient.doGet(eq(url + "?_range=1000-1999"), anyMap(), anyString(), eq(password), eq(config),
				anyString())).thenReturn(apiOutput2);

		try {
			ImportHelper.getResourceListFromSFG(true, config, Constants.SFG_PARTNER_REST_URI);
			fail("Method call successful but was expected to fail!");
		} catch (ImportException e) {
			assertEquals("dummy error1", e.getMessage());
		}
	}

	private ApiResponse mockApiResponse(int count) {
		ApiResponse apiOutput = mock(ApiResponse.class);
		when(apiOutput.getStatusCode()).thenReturn("200");
		when(apiOutput.getResponse()).thenReturn(String.format("<dummyresponse%s />", count));
		Header header = mock(Header.class);
		Header[] headers = new Header[] { header };
		when(apiOutput.getResponseHeaders()).thenReturn(headers);
		when(header.getName()).thenReturn("content-range");
		if (count == 1) {
			when(header.getValue()).thenReturn("items 0-999/2001");
		}
		return apiOutput;
	}

}
