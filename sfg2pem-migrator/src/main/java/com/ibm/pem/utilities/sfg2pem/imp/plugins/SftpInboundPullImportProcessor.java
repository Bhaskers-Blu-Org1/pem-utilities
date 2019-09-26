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

public class SftpInboundPullImportProcessor extends SftpConfigImportProcessor {

	public SftpInboundPullImportProcessor(PartnerInfo partnerInfo, Configuration config,
			SftpConfigInfo sftpConfigInfo) {
		super(partnerInfo, config, sftpConfigInfo);
	}

	@Override
	protected String getSubResourceType() {
		return "SFTP_INB_PULL";
	}

	@Override
	protected String getResourceNamePostfix() {
		return "SftpInbPull";
	}

	@Override
	protected String getConfigResource(String profileConfigKey) throws ApiInvocationException, ImportException {
		String url = getConfig().buildPRUrl(getConfigResourceURL()) + String.format("?partner=%s&configurationId=%s",
				getPartnerInfo().getPemPartnerKey(), profileConfigKey);
		return SftpResourceHelper.getResourceFromPR(getConfig(), url).getResponse();
	}

	@Override
	protected String getConfigResourceURL() {
		return "{PROTOCOL}://{PR.HOST_NAME}{:PR.PORT}/mdrws/sponsors/{CONTEXT_URI}/sftpinboundpulls/";
	}

	@Override
	protected String getConfigResourceUri() {
		return "sftpinboundpulls/";
	}

	@Override
	public String getResourceKeyAttributeName() {
		return "sftpInboundPullKey";
	}

	@Override
	protected String getResourceKeyTagName() {
		return "SftpInboundPull";
	}
}
