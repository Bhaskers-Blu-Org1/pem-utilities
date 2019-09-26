/**
 * Copyright 2019 Syncsort Inc. All Rights Reserved.
 * 
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.pem.utilities.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.ibm.pem.utilities.sfg2pem.Constants;

public class CreateFileUtil {

	/**
	 * Generates a timestamp in yyyyMMddhhmmss to append to the output report File name.
	 */
	public static String generateTimeStamp() {
		Calendar c = Calendar.getInstance();
		java.util.Date date = c.getTime();
		return new SimpleDateFormat("yyyyMMddhhmmss").format(date);
	}

	/**
	 * Creates a new empty file.
	 */
	public static File createFile(String filename, String outputPath, String mode) throws IOException {
		File file = null;
		if (mode.equals(Constants.MODE_EXTRACT_SFG_PARTNER_DATA)) {
			file = new File(outputPath + "/" + filename);
			if (!file.exists()) {
				FileUtils.forceMkdirParent(file);
				file.createNewFile();
			}
		} else if (mode.equals(Constants.MODE_IMPORT_SFG_PARTNER_DATA_TO_PEM)) {
			if (FilenameUtils.getExtension(filename).equals("csv")) {
				filename = FilenameUtils.removeExtension(filename);
			}
			file = new File(outputPath + "/" + filename + "_" + generateTimeStamp() + ".csv");
			if (!file.exists()) {
				FileUtils.forceMkdirParent(file);
				file.createNewFile();
			}
		}
		return file;
	}

}
