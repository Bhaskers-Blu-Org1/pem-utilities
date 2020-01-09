/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ValidationException;

public class HttpClientUtil {

	public static final int HTTP_OK = 200;
	public static final int TIMEOUT = 60000;
	public static final int BUFFER_SIZE = 1024;
	public static final int MAX_RESPONSE_SIZE = 10240;

	public ApiResponse doPost(String url, Map<?, ?> headers, String payload, String userName, char[] password,
			Configuration config, String host)
			throws URISyntaxException, HttpException, IOException, KeyManagementException, NoSuchAlgorithmException,
			CertificateException, KeyStoreException, ValidationException {
		HttpPost httpPost = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000)
				.setConnectTimeout(config.getConnectTimeout()).build();

		httpPost.setConfig(requestConfig);
		StringEntity entity = null;
		if (payload != null) {
			entity = new StringEntity(payload, "UTF-8");
			httpPost.setEntity(entity);
		}
		if (headers != null) {
			Set<?> headerNameSet = headers.keySet();
			String headerName;
			for (Iterator<?> iterator = headerNameSet.iterator(); iterator.hasNext(); httpPost.setHeader(headerName,
					(String) headers.get(headerName))) {
				headerName = (String) iterator.next();
				if (headerName.equalsIgnoreCase("Content-Type") && entity != null) {
					entity.setContentType((String) headers.get(headerName));
				}
			}

		}
		HttpResponse response = null;
		if (userName != null && password != null) {
			response = createHttpClient(config).execute(httpPost,
					createHttpClientContext(userName, new String(password), config, host));
		} else {
			response = createHttpClient(config).execute(httpPost);
		}
		return buildApiResponse(response);
	}

	public ApiResponse doGet(final String url, final Map<String, String> headers, String userName, char[] password,
			Configuration config, String host)
			throws HttpException, IOException, URISyntaxException, KeyManagementException, NoSuchAlgorithmException,
			CertificateException, KeyStoreException, ValidationException {
		HttpGet httpget = new HttpGet(url);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000)
				.setConnectTimeout(config.getConnectTimeout()).build();
		httpget.setConfig(requestConfig);

		// Set headers
		if (headers != null) {
			Set<?> headerNameSet = headers.keySet();
			String headerName;
			for (Iterator<?> iterator = headerNameSet.iterator(); iterator.hasNext(); httpget.setHeader(headerName,
					(String) headers.get(headerName))) {
				headerName = (String) iterator.next();
			}

		}

		HttpResponse response = null;
		if (userName != null && password != null) {
			response = createHttpClient(config).execute(httpget,
					createHttpClientContext(userName, new String(password), config, host));
		} else {
			response = createHttpClient(config).execute(httpget);
		}
		return buildApiResponse(response);
	}

	private static ApiResponse buildApiResponse(HttpResponse response) throws IOException {
		ApiResponse apiOutput = new ApiResponse();
		InputStream instream = null;
		if (response.getEntity() != null) {
			instream = response.getEntity().getContent();
			apiOutput.setResponse(read(instream));
		}
		apiOutput.setStatusCode(String.valueOf(getResponseStatusCode(response)));
		apiOutput.setStatusLine(getResponseStatusLine(response));
		return apiOutput;
	}

	private static String read(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in), 1024);
		char cbuf[] = new char[1024];
		for (int numRead = 0; (numRead = r.read(cbuf)) != -1;) {
			String line = String.valueOf(cbuf, 0, numRead);
			sb.append(line);
		}
		in.close();
		return sb.toString();
	}

	private static HttpClient createHttpClient(Configuration config) throws KeyManagementException,
			NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
		SSLContext sslctx = SSLContexts.custom().useTLS().build();
		sslctx.init(null, null, null);
		// The following code should allow TLS 1.0,1.1,1.2 connections
		SSLConnectionSocketFactory httpssf = new SSLConnectionSocketFactory(sslctx,
				new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" }, null, new AllowAllHostnameVerifier());
		PlainConnectionSocketFactory httpsf = PlainConnectionSocketFactory.getSocketFactory();
		Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create().register("http", httpsf)
				.register("https", httpssf).build();

		org.apache.http.conn.HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);

		HttpClientBuilder httpClient = HttpClientBuilder.create();
		httpClient.addInterceptorLast(new HttpRequestInterceptor() {

			@Override
			public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
				AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);

				// If no auth scheme available yet, try to initialize it preemptively
				if (authState.getAuthScheme() == null) {
					AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
					CredentialsProvider credsProvider = (CredentialsProvider) context
							.getAttribute(HttpClientContext.CREDS_PROVIDER);
					HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
					Credentials creds = credsProvider
							.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
					if (creds == null) {
						throw new HttpException("No credentials for preemptive authentication");
					}
					authState.update(authScheme, creds);
				}
			}
		});

		// Set proxy host, if available
		HttpHost proxy = getProxyHost(config);
		if (proxy != null) {
			httpClient.setProxy(proxy);
		}

		httpClient.setConnectionManager(cm);
		return httpClient.build();
	}

	private static HttpClientContext createHttpClientContext(String userName, String password, Configuration config,
			String host) {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(host, AuthScope.ANY_PORT),
				new UsernamePasswordCredentials(userName, password));
		char[] proxyPassword = config.getProxyPassword();
		if (config.getProxyUsername() != null && proxyPassword != null) {
			credsProvider.setCredentials(new AuthScope(config.getProxyHost(), Integer.parseInt(config.getProxyPort())),
					new UsernamePasswordCredentials(config.getProxyUsername(), new String(proxyPassword)));
		}
		HttpClientContext httpClientContext = HttpClientContext.create();
		httpClientContext.setCredentialsProvider(credsProvider);
		BasicScheme basicAuth = new BasicScheme();
		httpClientContext.setAttribute("preemptive-auth", basicAuth);
		return httpClientContext;
	}

	private static String getResponseStatusLine(HttpResponse response) {
		if (response != null) {
			return response.getStatusLine().toString();
		}
		return null;
	}

	private static int getResponseStatusCode(HttpResponse response) {
		if (response != null) {
			return response.getStatusLine().getStatusCode();
		} else {
			return -1;
		}
	}

	private static HttpHost getProxyHost(Configuration config) {
		String proxyHost = config.getProxyHost();
		String proxyPort = config.getProxyPort();

		if (proxyPort != null && proxyHost != null && !(proxyHost.isEmpty() || proxyHost.equals(""))
				|| !(proxyPort.isEmpty() || proxyPort.equals(""))) {
			return new HttpHost(proxyHost, Integer.valueOf(proxyPort), "http");
		}
		return null;
	}
}
