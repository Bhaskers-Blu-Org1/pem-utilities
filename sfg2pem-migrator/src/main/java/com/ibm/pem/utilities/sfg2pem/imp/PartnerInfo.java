/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.sfg2pem.imp;

import java.util.HashMap;

import org.w3c.dom.Document;

public class PartnerInfo {
	
	public PartnerInfo() {
		data = new HashMap<>();
	}

	public static enum ProcessingStatus {
		NOT_PROCESSED("Not Processed"), //
		ERROR("Error"), //
		PARTNER_IMPORT_PENDING("Partner Import Pending"),
		UPLOADED_TO_PEM("Uploaded to PEM"),
		IGNORED("Ignored");

		private String code;

		ProcessingStatus(String code) {
			this.code = code;
		}

		public String getCode() {
			return this.code;
		}

		public static ProcessingStatus parse(String code) {
			for (ProcessingStatus vps : values()) {
				if (vps.code.equals(code)) {
					return vps;
				}
			}
			return null;
		}

	}

	public static enum PartnerInfoField {
		sfgPartnerKey("sfgPartnerKey"), //
		pemPartnerKey("pemPartnerKey"), //
		sponsorDivisionKey("sponsorDivisionKey"), //
		inbPushProfileConfig("inbPushProfileConfig"), //
		obPullProfileConfig("obPullProfileConfig"), //
		hostIdentityKeyProfileConfig("hostIdentityKeyProfileConfig"), //
		authUserKeySGProfileConfig("authUserKeySGProfileConfig"), // header to capture the SG Profile Config Key of Authroized User Key (shared with partner)
		prSystemRef("prSystemRef"), //
		partnerName("partnerName"), //
		userName("userName"), //
		givenName("givenName"), //
		lastName("lastName"), //
		emailAddress("emailAddress"), //
		phone("phone"), //
		city("city"), //
		community("community"), //
		type("type"), //
		processingStatus("processingStatus"), //
		message("message");

		private String code;

		PartnerInfoField(String code) {
			this.code = code;
		}

		public String getCode() {
			return this.code;
		}

		public static PartnerInfoField parse(String code) {
			for (PartnerInfoField v : values()) {
				if (v.code.equals(code)) {
					return v;
				}
			}
			return null;
		}

	}

	private HashMap<String, String> data;

	private Document prodSfgPartnerDoc;

	private Document testSfgPartnerDoc;

	private String testSfgPartnerKey;
	
	private boolean isRemoteProfileExists = true;
	
	private String sshAuthorizedUserkeyProfileConfigKey;
	
	private String testSfgUserName;
	
	private String prodSfgUserName;
	
	public String getTestSfgUserName() {
		return testSfgUserName;
	}

	public void setTestSfgUserName(String testSfgUserName) {
		this.testSfgUserName = testSfgUserName;
	}

	public String getProdSfgUserName() {
		return prodSfgUserName;
	}

	public void setProdSfgUserName(String prodSfgUserName) {
		this.prodSfgUserName = prodSfgUserName;
	}
	
	public String getSshAuthorizedUserkeyProfileConfigKey() {
		return sshAuthorizedUserkeyProfileConfigKey;
	}

	public String setSshAuthorizedUserkeyProfileConfigKey(String sshAuthorizedUserkeyProfileConfigKey) {
		return this.sshAuthorizedUserkeyProfileConfigKey = sshAuthorizedUserkeyProfileConfigKey;
	}

	public boolean isRemoteProfileExists() {
		return isRemoteProfileExists;
	}

	public void setRemoteProfileExists(boolean isRemoteProfileExists) {
		this.isRemoteProfileExists = isRemoteProfileExists;
	}

	public ProcessingStatus getProcessingStatus() {
		String value = data.get(PartnerInfoField.processingStatus.getCode());
		return value == null ? null : ProcessingStatus.parse(value);
	}

	public void setProcessingStatus(ProcessingStatus processingStatus) {
		data.put(PartnerInfoField.processingStatus.getCode(),
				processingStatus == null ? null : processingStatus.getCode());
	}
	
	public String getInbPushProfileConfig() {
		return data.get(PartnerInfoField.inbPushProfileConfig.getCode());
	}

	public String getHostIdentityKeyProfileConfig() {
		return data.get(PartnerInfoField.hostIdentityKeyProfileConfig.getCode());
	}
	
	public String getObPullProfileConfig() {
		return data.get(PartnerInfoField.obPullProfileConfig.getCode());
	}
	
	public String getAuthUserKeySGProfileConfig() {
		return data.get(PartnerInfoField.authUserKeySGProfileConfig.getCode());
	}
	
	public String getPemPartnerKey() {
		return data.get(PartnerInfoField.pemPartnerKey.getCode());
	}

	public String getSfgPartnerKey() {
		return data.get(PartnerInfoField.sfgPartnerKey.getCode());
	}

	public String getSponsorDivisionKey() {
		return data.get(PartnerInfoField.sponsorDivisionKey.getCode());
	}

	public String getPrSystemRef() {
		return data.get(PartnerInfoField.prSystemRef.getCode());
	}

	public String getMessage() {
		return data.get(PartnerInfoField.message.getCode());
	}

	public void setMessage(String message) {
		data.put(PartnerInfoField.message.getCode(), message);
	}

	public String getData(String field) {
		return data.get(field);
	}

	public void setData(String field, String value) {
		data.put(field, value);
	}

	public Document getProdSfgPartnerDoc() {
		return prodSfgPartnerDoc;
	}

	public void setProdSfgPartnerDoc(Document prodSfgPartnerDoc) {
		this.prodSfgPartnerDoc = prodSfgPartnerDoc;
	}

	public Document getTestSfgPartnerDoc() {
		return testSfgPartnerDoc;
	}

	public void setTestSfgPartnerDoc(Document testSfgPartnerDoc) {
		this.testSfgPartnerDoc = testSfgPartnerDoc;
	}

	public String getTestSfgPartnerKey() {
		return testSfgPartnerKey;
	}

	public void setTestSfgPartnerKey(String testSfgPartnerKey) {
		this.testSfgPartnerKey = testSfgPartnerKey;
	}
}
