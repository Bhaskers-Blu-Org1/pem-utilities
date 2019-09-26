/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import java.util.HashMap;

public class PemPartnerInfo {
	
	private HashMap<String, String> data;
	
	public static enum PEMPartnerInfoField {
		userId("userId"),
		nameOfCompany("nameOfCompany"),
		website("website"),
		firstName("firstName"),
		lastName("lastName"),
		streetAddress("streetAddress"),
		city("city"), 
		country("country"),
		state("state"),
		email("email"),
		zipCode("zipCode"),
		headOfficePhone("headOfficePhone"),
		businessRole("businessRole"),
		officePhone("officePhone"),
		mobilePhone("mobilePhone"),
		furtherContacts("furtherContacts"),
		question("question"),
		answer("answer"),
		comments("comments"),
		attrTypeName("attrTypeName"),
		attrValue("attrValue"),
		name("name"),
		value("value"),
		statusCode("statusCode"),
		description("description"),
		divisionStatusCode("divisionStatusCode"),
		divisionDescription("divisionDescription"),
		divisionName("divisionName"),
		doNotInvite("doNotInvite"),
		partnerKey("partnerKey"),
		partnerGroupId("partnerGroupId"); 

		private String code;

		PEMPartnerInfoField(String code) {
			this.code = code;
		}

		public String getCode() {
			return this.code;
		}

		public static PEMPartnerInfoField parse(String code) {
			for (PEMPartnerInfoField v : values()) {
				if (v.code.equals(code)) {
					return v;
				}
			}
			return null;
		}

	}
	
	public PemPartnerInfo(PartnerInfo sfgPartnerInfo) {
		init(sfgPartnerInfo);
	}
	
	public PemPartnerInfo() {
		data = new HashMap<>();
	}

	private void init(PartnerInfo sfgPartnerInfo) {
		data = new HashMap<>();
		data.put(PEMPartnerInfoField.partnerGroupId.getCode(), sfgPartnerInfo.getPemPartnerKey());
		data.put(PEMPartnerInfoField.city.getCode(), sfgPartnerInfo.getData(PartnerInfo.PartnerInfoField.city.getCode()));
		data.put(PEMPartnerInfoField.firstName.getCode(), sfgPartnerInfo.getData(PartnerInfo.PartnerInfoField.givenName.getCode()));
		data.put(PEMPartnerInfoField.userId.getCode(), sfgPartnerInfo.getData(PartnerInfo.PartnerInfoField.emailAddress.getCode()));
		data.put(PEMPartnerInfoField.officePhone.getCode(), sfgPartnerInfo.getData(PartnerInfo.PartnerInfoField.phone.getCode()));
		data.put(PEMPartnerInfoField.lastName.getCode(), sfgPartnerInfo.getData(PartnerInfo.PartnerInfoField.lastName.getCode()));
		data.put(PEMPartnerInfoField.nameOfCompany.getCode(), sfgPartnerInfo.getData(PartnerInfo.PartnerInfoField.partnerName.getCode()));
	}
	
	public String getData(String field) {
		return data.get(field);
	}
	
	public void setData(String field, String value) {
		data.put(field, value);
	}

	public String getPartnerKey() {
		return data.get(PEMPartnerInfoField.partnerKey.getCode());
	}

	public String getPartnerGroupId() {
		return data.get(PEMPartnerInfoField.partnerGroupId.getCode());
	}

}
