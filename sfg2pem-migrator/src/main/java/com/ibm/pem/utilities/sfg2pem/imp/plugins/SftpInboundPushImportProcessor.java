/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp.plugins;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.ApiInvocationException;
import com.ibm.pem.utilities.sfg2pem.ImportException;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.resources.SftpResourceHelper;

public class SftpInboundPushImportProcessor extends SftpConfigImportProcessor {

	private PartnerInfo partnerInfo;
	
	public SftpInboundPushImportProcessor(PartnerInfo partnerInfo, Configuration config,
			SftpConfigInfo sftpConfigInfo) {
		super(partnerInfo, config, sftpConfigInfo);
		this.partnerInfo = partnerInfo;
	}

	@Override
	protected String getSubResourceType() {
		return "SFTP_INB_PUSH";
	}

	@Override
	protected String getResourceNamePostfix() {
		return "SftpInbPush";
	}

	@Override
	protected String getConfigResource(String profileConfigKey) throws ApiInvocationException, ImportException {
		String url = getConfig().buildPRUrl(getConfigResourceURL())
				+ String.format("?configurationId=%s", profileConfigKey);
		return SftpResourceHelper.getResourceFromPR(getConfig(), url).getResponse();
	}

	@Override
	protected String getConfigResourceURL() {
		return "{PROTOCOL}://{PR.HOST_NAME}{:PR.PORT}/mdrws/sponsors/{CONTEXT_URI}/sftpinboundpushs/";
	}

	@Override
	protected String getConfigResourceUri() {
		return "sftpinboundpushs/";
	}

	@Override
	public String getResourceKeyAttributeName() {
		return "sftpInboundPushKey";
	}

	@Override
	protected String getResourceKeyTagName() {
		return "SftpInboundPush";
	}
	
	@Override
	protected String getProfileConfigKey() throws ImportException {
		if (!partnerInfo.isRemoteProfileExists()) {
			String message = null;
			if (partnerInfo.getInbPushProfileConfig() != null
					&& !partnerInfo.getInbPushProfileConfig().trim().isEmpty()) {
				if (SftpResourceHelper.isServerTypeSGorDG(partnerInfo.getInbPushProfileConfig(), getConfig())) {
					return partnerInfo.getInbPushProfileConfig();
				} else {
					message = "Profile Configuration for " + getSubResourceType() + " must be of type SG or DG";
				}
			} else {
				message = "Inbound Push Profile Configuration for " + getSubResourceType() + " not specified.";
			}
			throw new ImportException(message);
		}
		return null;
	}
}
