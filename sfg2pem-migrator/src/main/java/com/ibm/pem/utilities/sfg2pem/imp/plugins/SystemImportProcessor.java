/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp.plugins;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ApiInvocationException;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo;
import com.ibm.pem.utilities.sfg2pem.imp.PrConfigurationProcessor;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SftpResourceHelper;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SystemXSLT;
import com.ibm.pem.utilities.util.DOMUtils;

public class SystemImportProcessor extends PrConfigurationProcessor {

	private static final String CREATE_SYSTEM_API_URL = "{PROTOCOL}://{PR.HOST_NAME}{:PR.PORT}/mdrws/sponsors/{CONTEXT_URI}/systems/";

	public static class SystemConfigInfo {

		private String managedSshKeyProfileConfigKey;
		private String userIdentityKeyProfileConfigKey;
		private String userCredentialProfileConfigKey;
		private String sftpInbPullProfileConfigKey;
		private String sftpOutPushProfileConfigKey;
		private String sftpInbPushProfileConfigKey;
		private String sftpOutPullProfileConfigKey;
		private String sshAuthUserKeyProfileConfigKey;
		private String hostIdentityKeyProfileConfig;

		public String getSshAuthUserKeyProfileConfigKey() {
			return sshAuthUserKeyProfileConfigKey;
		}

		public SystemConfigInfo setSshAuthUserKeyProfileConfigKey(String sshAuthUserKeyProfileConfigKey) {
			this.sshAuthUserKeyProfileConfigKey = sshAuthUserKeyProfileConfigKey;
			return this;
		}

		public String getSftpInbPushProfileConfigKey() {
			return sftpInbPushProfileConfigKey;
		}

		public SystemConfigInfo setSftpInbPushProfileConfigKey(String sftpInbPushProfileConfigKey) {
			this.sftpInbPushProfileConfigKey = sftpInbPushProfileConfigKey;
			return this;
		}

		public String getSftpOutPullProfileConfigKey() {
			return sftpOutPullProfileConfigKey;
		}

		public SystemConfigInfo setSftpOutPullProfileConfigKey(String sftpOutPullProfileConfigKey) {
			this.sftpOutPullProfileConfigKey = sftpOutPullProfileConfigKey;
			return this;
		}

		public String getManagedSshKeyProfileConfigKey() {
			return managedSshKeyProfileConfigKey;
		}

		public SystemConfigInfo setManagedSshKeyProfileConfigKey(String managedSshKeyProfileConfigKey) {
			this.managedSshKeyProfileConfigKey = managedSshKeyProfileConfigKey;
			return this;
		}

		public String getUserIdentityKeyProfileConfigKey() {
			return userIdentityKeyProfileConfigKey;
		}

		public SystemConfigInfo setUserIdentityKeyProfileConfigKey(String userIdentityKeyProfileConfigKey) {
			this.userIdentityKeyProfileConfigKey = userIdentityKeyProfileConfigKey;
			return this;
		}

		public String getUserCredentialProfileConfigKey() {
			return userCredentialProfileConfigKey;
		}

		public SystemConfigInfo setUserCredentialProfileConfigKey(String userCredentialProfileConfigKey) {
			this.userCredentialProfileConfigKey = userCredentialProfileConfigKey;
			return this;
		}

		public String getSftpInbPullProfileConfigKey() {
			return sftpInbPullProfileConfigKey;
		}

		public SystemConfigInfo setSftpInbPullProfileConfigKey(String sftpInbPullProfileConfigKey) {
			this.sftpInbPullProfileConfigKey = sftpInbPullProfileConfigKey;
			return this;
		}

		public String getSftpOutPushProfileConfigKey() {
			return sftpOutPushProfileConfigKey;
		}

		public SystemConfigInfo setSftpOutPushProfileConfigKey(String sftpOutPushProfileConfigKey) {
			this.sftpOutPushProfileConfigKey = sftpOutPushProfileConfigKey;
			return this;
		}

		public String getHostIdentityKeyProfileConfig() {
			return hostIdentityKeyProfileConfig;
		}

		public SystemConfigInfo setHostIdentityKeyProfileConfig(String hostIdentityKeyProfileConfig) {
			this.hostIdentityKeyProfileConfig = hostIdentityKeyProfileConfig;
			return this;
		};

	}

	private SystemConfigInfo configInfo;

	public SystemImportProcessor(PartnerInfo partnerInfo, Configuration config, SystemConfigInfo configInfo) {
		super(partnerInfo, config);
		this.configInfo = configInfo;
	}

	@Override
	protected String getResourceType() {
		return "SYSTEM";
	}

	@Override
	protected String getSubResourceType() {
		return "SYSTEM";
	}

	@Override
	protected String getResourceNamePostfix() {
		return "SYSTEM";
	}

	@Override
	protected String getConfigResource(String profileConfigKey) throws ImportException, ApiInvocationException {
		String url = getConfig().buildPRUrl(CREATE_SYSTEM_API_URL) + String.format("?partner=%s&configurationId=%s",
				getPartnerInfo().getPemPartnerKey(), profileConfigKey);
		return SftpResourceHelper.getResourceFromPR(getConfig(), url).getResponse();
	}

	@Override
	protected String createConfigResource(String profileConfigKey) throws ImportException {
		try {
			String requestXml = DOMUtils.createXml(
					new SystemXSLT().createSystem(getConfig(), configInfo, getPartnerInfo(), profileConfigKey));

			return SftpResourceHelper.callCreateApi(getConfig(), requestXml,
					getConfig().buildPRUrl(CREATE_SYSTEM_API_URL), "systemKey");
		} catch (TransformerException | IOException | ParserConfigurationException | SAXException
				| ApiInvocationException e) {
			throw new ImportException(e);
		}
	}

	protected String getConfigResourceUri() {
		return "systems/";
	}

	@Override
	protected String getResourceKeyAttributeName() {
		return "systemKey";
	}

	@Override
	protected String getResourceKeyTagName() {
		return "System";
	}
}
