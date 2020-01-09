/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.util;

import java.io.IOException;
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DOMUtils {

	private DOMUtils() {
	}

	public static Document toDocument(String data) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(data)));
	}

	public static String createXml(Document doc) throws TransformerException {
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer aTransformer = tranFactory.newTransformer();
		aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
		aTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
		aTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		aTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		StringWriter writer = new StringWriter();
		aTransformer.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.toString();
	}

	public static String formatXML(String data)
			throws TransformerException, IOException, ParserConfigurationException, SAXException {
		return createXml(toDocument(data));
	}

	public static String getAttributeValueByTagName(String xmldata, String tagName, String attributeName)
			throws ParserConfigurationException, SAXException, IOException {
		Document doc = toDocument(xmldata);
		return getAttributeValueByTagName(doc, tagName, attributeName);
	}

	public static String getAttributeValueByTagName(Document doc, String tagName, String attributeName) {
		String value = null;
		NodeList elementsByTagName = doc.getElementsByTagName(tagName);
		for (int i = 0; i < elementsByTagName.getLength(); i++) {
			Node node = elementsByTagName.item(i);
			if (node.hasAttributes()) {
				Attr attr = (Attr) node.getAttributes().getNamedItem(attributeName);
				if (attr != null) {
					value = attr.getValue();
					break;
				}
			}
		}
		return value;
	}

}
