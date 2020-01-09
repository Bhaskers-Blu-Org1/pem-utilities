/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ApiInvocationException;
import com.ibm.pem.utilities.sfg2pem.Constants;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.ProfileConfigurationXSLT;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SftpResourceHelper;
import com.ibm.pem.utilities.util.DOMUtils;

public abstract class PrConfigurationProcessor {

	protected static final String CREATE_PROFILECONFIGURATION_API_URL = "{PROTOCOL}://{PR.HOST_NAME}{:PR.PORT}/mdrws/sponsors/{CONTEXT_URI}/profileconfigurations/";

	public static class ConfigInfo {
		private String profileConfigKey;
		private String resourceKey;

		public String getProfileConfigKey() {
			return profileConfigKey;
		}

		public ConfigInfo setProfileConfigKey(String profileConfigKey) {
			this.profileConfigKey = profileConfigKey;
			return this;
		}

		public String getConfigResourceKey() {
			return resourceKey;
		}

		public ConfigInfo setResourceKey(String resourceKey) {
			this.resourceKey = resourceKey;
			return this;
		}
	}

	private Configuration config;
	private PartnerInfo partnerInfo;

	public PrConfigurationProcessor(PartnerInfo partnerInfo, Configuration config) {
		this.config = config;
		this.partnerInfo = partnerInfo;
	}

	protected Configuration getConfig() {
		return config;
	}

	protected PartnerInfo getPartnerInfo() {
		return partnerInfo;
	}

	public ConfigInfo execute() throws ImportException {
		String resourceType = getResourceType();
		String subResourceType = getSubResourceType();
		String resourceName = getResourceName();
		String profileConfigKey = null;
		try {
			if (getProfileConfigKey() != null) {
				profileConfigKey = getProfileConfigKey();
			} else if (getProfileConfigKey() == null) {
				profileConfigKey = getProfileConfiguration(resourceType, subResourceType, resourceName);
				if (profileConfigKey == null) {
					profileConfigKey = createProfileConfiguration(resourceType, subResourceType, resourceName);
				}
			}

			Document apiResponse;
			try {
				apiResponse = ImportHelper.buildDomDoc(getConfigResource(profileConfigKey));
			} catch (ParserConfigurationException | SAXException | IOException e) {
				throw new ImportException(e);
			}

			String resourceKey = DOMUtils.getAttributeValueByTagName(apiResponse, getResourceKeyTagName(),
					getResourceKeyAttributeName());
			
			if (!partnerInfo.isRemoteProfileExists() && getProfileConfigKey() != null && resourceKey == null) {
				throw new ImportException("No Profile Configuration could be found with key " +profileConfigKey+ " and for subResourceType " +subResourceType);
			} else {
				resourceKey = createConfigResourceAndmarkComplete(profileConfigKey, apiResponse, resourceKey);
			}
			
			return new ConfigInfo().setProfileConfigKey(profileConfigKey).setResourceKey(resourceKey);
		} catch (ApiInvocationException e) {
			throw new ImportException(e);
		}
	}

	private String createConfigResourceAndmarkComplete(String profileConfigKey, Document apiResponse, String resourceKey)
			throws ImportException, ApiInvocationException {
		boolean markAsComplete = false;
		if (resourceKey == null) {
			resourceKey = createConfigResource(profileConfigKey);
			markAsComplete = true;
		} else {
			String resourceStatus = DOMUtils.getAttributeValueByTagName(apiResponse, "status", "code");
			if (resourceStatus.equalsIgnoreCase("NEW")) {
				markAsComplete = true;
			} else if (resourceStatus.equalsIgnoreCase("PROD_CFG_PVRN_COMPLETE")) {
				// Do nothing
			} else {
				throw new ImportException("Config resource is not in valid state to do markComplete");
			}
		}

		if (markAsComplete) {
			SftpResourceHelper.markAsComplete(resourceKey, getConfig(), getConfigResourceUri());
		}
		return resourceKey;
	}

	protected abstract String getResourceType();

	protected abstract String getSubResourceType();

	protected abstract String getResourceNamePostfix();

	protected String getProfileConfigKey() throws ImportException {
		return null;
	}

	protected String getResourceName() {
		return DOMUtils.getAttributeValueByTagName(partnerInfo.getProdSfgPartnerDoc(), "TradingPartner",
				"partnerName") + "-" + getResourceNamePostfix();
	}

	protected String getProfileConfiguration(String resourceType, String subResourceType, String resourceName)
			throws ApiInvocationException, ImportException {
		String configurationId = null;
		String url = getProfileConfigurationURL(resourceType, subResourceType, resourceName);
		String apiResponse = SftpResourceHelper.getResourceFromPR(getConfig(), url).getResponse();
		try {
			configurationId = DOMUtils.getAttributeValueByTagName(apiResponse, "ProfileConfiguration",
					"profileConfigurationKey");
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ImportException(e);
		}
		return configurationId;
	}

	protected String getProfileConfigurationURL(String resourceType, String subResourceType, String resourceName) {
		return config.buildPRUrl(CREATE_PROFILECONFIGURATION_API_URL)
				+ String.format("?partner=%s&resourceType=%s&subResourceType=%s&name=%s",
						partnerInfo.getPemPartnerKey(), resourceType, subResourceType, resourceName);
	}

	protected String createProfileConfiguration(String resourceType, String subResourceType, String resourceName)
			throws ApiInvocationException, ImportException {
		String configurationId = null;
		try {
			String requestXML = DOMUtils.createXml(new ProfileConfigurationXSLT().createProfileConfiguration(
					config, resourceName, resourceType, subResourceType, Constants.SERVER_TYPE_PR,
					partnerInfo.getPemPartnerKey(), partnerInfo.getSponsorDivisionKey(), partnerInfo.getAuthUserKeySGProfileConfig()));
			String createProfileConfigRequest = requestXML;
			configurationId = SftpResourceHelper.callCreateApi(getConfig(), createProfileConfigRequest,
					config.buildPRUrl(CREATE_PROFILECONFIGURATION_API_URL), "profileConfigurationKey");
		} catch (TransformerException | IOException | ParserConfigurationException | SAXException e) {
			throw new ImportException(e);
		}
		return configurationId;
	}
	
	protected abstract String createConfigResource(String profileConfigKey) throws ImportException;

	protected abstract String getConfigResource(String profileConfigKey) throws ImportException, ApiInvocationException;

	protected abstract String getConfigResourceUri();

	protected abstract String getResourceKeyAttributeName();

	protected abstract String getResourceKeyTagName();

	public static void main(String[] args) {
		
	}
}
