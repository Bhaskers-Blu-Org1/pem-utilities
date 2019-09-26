/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp.plugins.resources;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.pem.utilities.Configuration;
import com.ibm.pem.utilities.sfg2pem.Constants;
import com.ibm.pem.utilities.sfg2pem.imp.PartnerInfo;
import com.ibm.pem.utilities.sfg2pem.imp.plugins.SystemImportProcessor.SystemConfigInfo;

public class SystemXSLT {

	private static final String CODE_EXTERNAL = "External";
	private static final String CODE_SSH_KEY = "SSH_KEY";

	public String getCommunityName(Document sfgPartnerDoc)
			throws ParserConfigurationException, SAXException, IOException {
		return SftpResourceHelper.getAttributeValueByTagName(sfgPartnerDoc, "TradingPartner", "community");
	}

	public String getSfgPartnerName(Document sfgPartnerDoc)
			throws ParserConfigurationException, SAXException, IOException {
		return SftpResourceHelper.getAttributeValueByTagName(sfgPartnerDoc, "TradingPartner", "partnerName");
	}

	public String getAuthenticationType(Document sfgPartnerDoc)
			throws ParserConfigurationException, SAXException, IOException {
		return SftpResourceHelper.getAttributeValueByTagName(sfgPartnerDoc, "authenticationType", "code");
	}

	public Document createSystem(Configuration config, SystemConfigInfo configInfo, PartnerInfo partnerInfo,
			String configurationId)
			throws TransformerException, ParserConfigurationException, SAXException, IOException {
		StringReader dummyInput = new StringReader("<a>pem</a>");
		StringWriter outPutXml = new StringWriter();
		TransformerFactory tFactory = TransformerFactory.newInstance();
		File inputXSLT = new File(config.getInstallDirectory() + "/xslt/system.xsl").getAbsoluteFile();
		Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(inputXSLT));
		transformer.setParameter("configurationId", configurationId);
		transformer.setParameter("testType", Constants.TEST);
		transformer.setParameter("prodType", Constants.PROD);
		transformer.setParameter("inbound", "y");
		transformer.setParameter("outbound", "y");
		transformer.setParameter("AuthenticationHost", "AuthenticationHost");

		if (getAuthenticationType(partnerInfo.getProdSfgPartnerDoc()).equalsIgnoreCase(CODE_EXTERNAL)) {
			transformer.setParameter("AuthenticationHostProdVal", CODE_EXTERNAL);
		} else {
			transformer.setParameter("AuthenticationHostProdVal", "");
		}

		if (getAuthenticationType(partnerInfo.getTestSfgPartnerDoc()).equalsIgnoreCase(CODE_EXTERNAL)) {
			transformer.setParameter("AuthenticationHostTestVal", CODE_EXTERNAL);
		} else {
			transformer.setParameter("AuthenticationHostTestVal", "");
		}

		transformer.setParameter("SFGCommunityName", "SFGCommunityName");
		transformer.setParameter("PartnerPrefix", "PartnerPrefix");
		transformer.setParameter("SFGCommunityNameProdVal", getCommunityName(partnerInfo.getProdSfgPartnerDoc()));
		transformer.setParameter("SFGCommunityNameTestVal", getCommunityName(partnerInfo.getTestSfgPartnerDoc()));

		transformer.setParameter("ProdPartnerPrefixVal", partnerInfo.getSfgPartnerKey());
		transformer.setParameter("TestPartnerPrefixVal", partnerInfo.getTestSfgPartnerKey());

		transformer.setParameter("SFGPartnerKey", "SFGPartnerKey");
		transformer.setParameter("ProdSFGPartnerKey", partnerInfo.getSfgPartnerKey());
		transformer.setParameter("TestSFGPartnerKey", partnerInfo.getTestSfgPartnerKey());

		if (configInfo.getSftpInbPullProfileConfigKey() != null) {
			transformer.setParameter("sftpProfileRefName", "SFTPIBPullProfileConfig");
			transformer.setParameter("sftpProfileRefType", Constants.SFTP_INB_PULL);
			transformer.setParameter("sftpInbPullProfileConfigKey", configInfo.getSftpInbPullProfileConfigKey());
		}

		if (configInfo.getSftpOutPushProfileConfigKey() != null) {
			transformer.setParameter("sftpOutProfileRefName", "SFTPOBPushProfileConfig");
			transformer.setParameter("sftpOutProfileRefType", Constants.SFTP_OUT_PUSH);
			transformer.setParameter("sftpOutbPushProfileConfigKey", configInfo.getSftpOutPushProfileConfigKey());
		}

		if (configInfo.getManagedSshKeyProfileConfigKey() != null) {
			transformer.setParameter("managedSshKeyProfileConfigKey", configInfo.getManagedSshKeyProfileConfigKey());
			transformer.setParameter("managedSshKeyProfileRefName", "KnownHostKeyProfileConfig");
			transformer.setParameter("managedSshKeyProfileRefType", CODE_SSH_KEY);
		}

		if (configInfo.getUserIdentityKeyProfileConfigKey() != null) {
			transformer.setParameter("userIdentityKeyProfileConfigKey",
					configInfo.getUserIdentityKeyProfileConfigKey());
			transformer.setParameter("userIdentityKeyProfileRefName", "UserIdentityKeyProfileConfig");
			transformer.setParameter("userIdentityKeyProfileRefType", CODE_SSH_KEY);
		}
		if (configInfo.getUserCredentialProfileConfigKey() != null) {
			transformer.setParameter("userCredentialProfileConfigKey", configInfo.getUserCredentialProfileConfigKey());
			transformer.setParameter("userCredentialProfileRefName", "UserCredProfileConfig");
			transformer.setParameter("userCredentialProfileRefType", "USER_CRED");
		}

		if (configInfo.getSshAuthUserKeyProfileConfigKey() != null) {
			transformer.setParameter("sshAuthorizedUserKeyProfileConfigKey",
					configInfo.getSshAuthUserKeyProfileConfigKey());
			transformer.setParameter("managedSshKeyProfileRefName", "AuthUserKeyProfileConfig");
			transformer.setParameter("managedSshKeyProfileRefType", CODE_SSH_KEY);
		}

		if (configInfo.getHostIdentityKeyProfileConfig() != null) {
			transformer.setParameter("hostIdentityKeyyProfileConfigKey", configInfo.getHostIdentityKeyProfileConfig());
			transformer.setParameter("hostIdentityKeyProfileRefName", "HostIdentityKeyProfileConfig");
			transformer.setParameter("hostIdentityKeyProfileRefType", CODE_SSH_KEY);
		}

		if (configInfo.getSftpInbPushProfileConfigKey() != null) {
			transformer.setParameter("sftpProfileRefName", "SFTPIBPushProfileConfig");
			transformer.setParameter("sftpProfileRefType", Constants.SFTP_INB_PUSH);
			transformer.setParameter("sftpInbPushProfileConfigKey", configInfo.getSftpInbPushProfileConfigKey());
		}

		if (configInfo.getSftpOutPullProfileConfigKey() != null) {
			transformer.setParameter("sftpOutProfileRefName", "SFTPOBPullProfileConfig");
			transformer.setParameter("sftpOutProfileRefType", Constants.SFTP_OUT_PULL);
			transformer.setParameter("sftpOutbPullProfileConfigKey", configInfo.getSftpOutPullProfileConfigKey());
		}

		transformer.transform(new javax.xml.transform.stream.StreamSource(dummyInput),
				new javax.xml.transform.stream.StreamResult(outPutXml));

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource input = new InputSource();
		input.setCharacterStream(new StringReader(outPutXml.toString()));
		Document doc = db.parse(input);
		return doc;
	}

}
