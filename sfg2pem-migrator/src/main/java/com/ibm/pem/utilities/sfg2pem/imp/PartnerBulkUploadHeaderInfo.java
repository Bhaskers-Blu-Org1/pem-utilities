/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import java.util.Properties;

import com.ibm.pem.utilities.sfg2pem.HeaderInfo;

public class PartnerBulkUploadHeaderInfo extends HeaderInfo {

	private static String[] columnHeaders = new String[] { //
			"userId", //
			"nameOfCompany", //
			"website", //
			"firstName", //
			"lastName", //
			"streetAddress", //
			"city", //
			"country", //
			"state", //
			"email", //
			"zipCode", //
			"headOfficePhone", //
			"businessRole", //
			"officePhone",//
			"mobilePhone",//
			"furtherContacts",//
			"question",//
			"answer",//
			"comments",//
			"attrTypeName",//
			"attrValue", // 
			"attrTypeName",//
			"attrValue", // 
			"customField.name", //
			"customField.value", //
			"customField.name", //
			"customField.value", //
			"divisionName", //
			"doNotInvite", //
			"partnerGroupId" //
	};

	public PartnerBulkUploadHeaderInfo(Properties properties) {
		super();
		for (String headerCode : columnHeaders) {
			String headerDisplayName = properties.getProperty(HeaderInfo.HEADER_KEY_PREFIX + headerCode);
			addHeader(headerCode, headerDisplayName);
		}
	}

}
