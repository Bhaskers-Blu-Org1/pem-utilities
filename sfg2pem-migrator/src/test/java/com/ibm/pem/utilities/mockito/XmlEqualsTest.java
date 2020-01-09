/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.mockito;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class XmlEqualsTest {

	@Test
	public void testMatchSuccess() {
		String wanted = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><create name=\"test01-id01-SSH_KEY\""
				+ " partner=\"partner01Key\" resourceType=\"SSH_KEY\" serverType=\"PR\" subResourceType=\"SSH_KEY\">\r\n"
				+ "	<owningDivisions>\r\n"
				+ "		<SubResourceDivisionRef sponsorDivisionKey=\"divkey1\"/>\r\n"
				+ "	</owningDivisions>\r\n</create>";
		String actual = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><create name=\"test01-id01-SSH_KEY\""
				+ " partner=\"partner01Key\" resourceType=\"SSH_KEY\" serverType=\"PR\" subResourceType=\"SSH_KEY\">\r\n"
				+ "    <owningDivisions>\r\n"
				+ "        <SubResourceDivisionRef sponsorDivisionKey=\"divkey1\"/>\r\n"
				+ "    </owningDivisions>\r\n</create>";
		boolean match = new XmlEquals(wanted).matches(actual);
		assertEquals(true, match);
	}

	@Test
	public void testMatchFail() {
		String wanted = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><create name=\"test02-id02-SSH_KEY\""
				+ " partner=\"partner02Key\" resourceType=\"SSH_KEY\" serverType=\"PR\" subResourceType=\"SSH_KEY\">\r\n"
				+ "	<owningDivisions>\r\n"
				+ "		<SubResourceDivisionRef sponsorDivisionKey=\"divkey1\"/>\r\n"
				+ "	</owningDivisions>\r\n</create>";
		String actual = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><create name=\"test02-id02-SSH_KEY\""
				+ " partner=\"partner02Key\" resourceType=\"SSH_KEY\" serverType=\"PR\" subResourceType=\"SSH_KEY\">\r\n"
				+ "    <owningDivisions>\r\n"
				+ "        <SubResourceDivisionRef sponsorDivisionKey=\"divkey2\"/>\r\n"
				+ "    </owningDivisions>\r\n</create>";
		new XmlEquals(wanted).matches(actual);
		boolean match = new XmlEquals(wanted).matches(actual);
		assertEquals(false, match);
	}

}
