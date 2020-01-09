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

public class AssociateAuthUserToUserCredXSLT {

	public static Document associateAuthUserToUserCred(Configuration config, String sshAuthUserKeyProfileConfigKey)
			throws TransformerException, ParserConfigurationException, SAXException, IOException {
		StringReader dummyInput = new StringReader("<a>pem</a>");
		StringWriter outPutXml = new StringWriter();
		TransformerFactory tFactory = TransformerFactory.newInstance();
		File inputXSLT = new File(config.getXsltDirectory() + "/associateAuthUser.xsl").getAbsoluteFile();
		Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(inputXSLT));
		transformer.setParameter("authorizedUserProfileConfigKey", sshAuthUserKeyProfileConfigKey);
		transformer.transform(new javax.xml.transform.stream.StreamSource(dummyInput),
				new javax.xml.transform.stream.StreamResult(outPutXml));

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource input = new InputSource();
		input.setCharacterStream(new StringReader(outPutXml.toString()));
		Document doc = db.parse(input);
		return doc;
	}
}
