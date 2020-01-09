/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.mockito;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mockito.ArgumentMatcher;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.matchers.text.ValuePrinter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlEquals implements ArgumentMatcher<String>, Serializable {

	private static final long serialVersionUID = -2886705564472157517L;

	private final String wanted;

	public XmlEquals(String wanted) {
		this.wanted = wanted;
	}

	@Override
	public boolean matches(String actual) {
		try {
			return formatXml(wanted).equals(formatXml(actual));
		} catch (TransformerException | ParserConfigurationException | SAXException | IOException e) {
			throw new MockitoException("", e);
		}
	}

	public String toString() {
		return describe(wanted);
	}

	private String describe(Object object) {
		return ValuePrinter.print(object);
	}

	private static String formatXml(String data)
			throws TransformerException, ParserConfigurationException, SAXException, IOException {
		Document doc = toDocument(data);
		trimWhitespace(doc);
		return createXml(doc);
	}

	private static Document toDocument(String data) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(data)));
	}

	private static String createXml(Document doc) throws TransformerException {
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer aTransformer = tranFactory.newTransformer();
		aTransformer.setOutputProperty(OutputKeys.INDENT, "no");
		aTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
		aTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		StringWriter writer = new StringWriter();
		aTransformer.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.toString();
	}

	private static void trimWhitespace(Node node) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				child.setTextContent(child.getTextContent().trim());
			}
			trimWhitespace(child);
		}
	}

}
