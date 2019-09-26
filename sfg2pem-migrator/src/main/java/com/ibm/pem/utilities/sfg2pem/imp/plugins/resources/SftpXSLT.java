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

public class SftpXSLT {

	public Document createSFTP(Configuration config, Document prodSftpData, Document testSftpData,
			String configurationId, String managerSshKeyProfileConfigKey, String userIdentityKeyProfileConfigKey,
			String userCredentialProfileConfigKey, String subResourceType)
			throws TransformerException, ParserConfigurationException, SAXException, IOException {
		StringWriter outPutXml = new StringWriter();

		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = null;

		File inputXSLT;
		if (subResourceType.equals(Constants.SFTP_INB_PULL)) {
			inputXSLT = new File(config.getInstallDirectory() + "/xslt/sftpInbPull.xsl").getAbsoluteFile();
			transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(inputXSLT));
		} else if (subResourceType.equals(Constants.SFTP_OUT_PUSH)) {
			inputXSLT = new File(config.getInstallDirectory() + "/xslt/sftpOutbPush.xsl").getAbsoluteFile();
			transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(inputXSLT));
		}
		transformer.setParameter("configurationId", configurationId);
		transformer.setParameter("prodType", Constants.PROD);
		transformer.setParameter("testType", Constants.TEST);
		if (userCredentialProfileConfigKey != null) {
			transformer.setParameter("userCredential", userCredentialProfileConfigKey);
		}
		
		//Stamping authorizedUserKey instead of userIdentityKey
		if (userIdentityKeyProfileConfigKey != null && (subResourceType.equals(Constants.SFTP_INB_PULL)
				|| subResourceType.equals(Constants.SFTP_OUT_PUSH))) {
			transformer.setParameter("authorizedUserKey", userIdentityKeyProfileConfigKey); 
		}
		
		

		if (managerSshKeyProfileConfigKey != null) {
			transformer.setParameter("keyType", "KNOWN_HOST_KEYS");
			transformer.setParameter("sshKey", managerSshKeyProfileConfigKey);
		}

		transformer.setParameter("subResourceType", subResourceType);
		transformer.setParameter("testInputXml", testSftpData.getDocumentElement());
		transformer.setParameter("prodInputXml", prodSftpData.getDocumentElement());

		transformer.transform(new javax.xml.transform.dom.DOMSource(prodSftpData.getDocumentElement()),
				new javax.xml.transform.stream.StreamResult(outPutXml));

		InputSource input = new InputSource();
		input.setCharacterStream(new StringReader(outPutXml.toString()));
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(input);
		return doc;
	}

}
