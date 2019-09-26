/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.exp;

import java.util.Properties;

import com.ibm.pem.utilities.sfg2pem.HeaderInfo;

public class SFGPartnerExportHeaderInfo extends HeaderInfo {

	private static String[] columnHeaders = new String[] { //
			"type", //
			"partnerName", //
			"sfgPartnerKey", //
			"pemPartnerKey", //
			"sponsorDivisionKey", //
			"prSystemRef", //
			"userName", //
			"givenName", //
			"lastName", //
			"emailAddress", //
			"phone", //
			"city", //
			"community", //
			"isListeningProducer",//
			"isListeningConsumer",//
			"isInitiatingProducer",//
			"isInitiatingConsumer",//
			"inbPushProfileConfig",//
			"obPullProfileConfig",//
			"hostIdentityKeyProfileConfig",//
			"authUserKeySGProfileConfig", // header to capture the SG Profile Config Key of Authroized User Key (shared with partner)
			"processingStatus" //
	};

	public SFGPartnerExportHeaderInfo(Properties properties) {
		super();
		for (String headerCode : columnHeaders) {
			String headerDisplayName = properties.getProperty(HeaderInfo.HEADER_KEY_PREFIX + headerCode);
			addHeader(headerCode, headerDisplayName);
		}
	}

}
