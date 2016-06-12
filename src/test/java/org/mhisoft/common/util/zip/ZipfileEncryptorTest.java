/*
 *
 *  * Copyright (c) 2014- MHISoft LLC and/or its affiliates. All rights reserved.
 *  * Licensed to MHISoft LLC under one or more contributor
 *  * license agreements. See the NOTICE file distributed with
 *  * this work for additional information regarding copyright
 *  * ownership. MHISoft LLC licenses this file to you under
 *  * the Apache License, Version 2.0 (the "License"); you may
 *  * not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.mhisoft.common.util.zip;

import java.util.List;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.mhisoft.common.util.FileUtils;
import org.mhisoft.common.zip.api.AesZipFileDecrypter;
import org.mhisoft.common.zip.api.AesZipFileEncrypter;
import org.mhisoft.common.zip.impl.AESDecrypter;
import org.mhisoft.common.zip.impl.AESDecrypterJCA;
import org.mhisoft.common.zip.impl.AESEncrypter;
import org.mhisoft.common.zip.impl.AESEncrypterJCA;
import org.mhisoft.common.zip.impl.ExtZipEntry;

/**
 * Description:  ZipfileEncryptorTest
 *
 * @author Tony Xue
 * @since Jun, 2016
 */
public class ZipfileEncryptorTest {
	private static final Logger LOG = Logger.getLogger(ZipfileEncryptorTest.class.getName());

	@Test
	public void aesEncrypterJCAZipAndUnZipTest() {

		     /*encrypt*/

			try {
				AESEncrypter encrypter = new AESEncrypterJCA();
				String password ="testTest1244";

				InputStream in = this.getClass().getClassLoader().getResourceAsStream("ZipfileEncryptorTest.docx");
				InputStream file2 = this.getClass().getClassLoader().getResourceAsStream("test1.txt");
				File zipFile = new File("ZipfileEncryptorTest.zip");

				AesZipFileEncrypter enc = new AesZipFileEncrypter(zipFile , encrypter);
				try {
					enc.add("ZipfileEncryptorTest.docx", in, password);
					enc.add("test1.txt", file2, password);
				} finally {
					enc.close();
				}




				/*decrypt*/

				AESDecrypter decrypter = new AESDecrypterJCA();
				AesZipFileDecrypter aesZipFileDecrypter = new AesZipFileDecrypter(zipFile, decrypter);
				List<ExtZipEntry> entries = aesZipFileDecrypter.getEntryList();
				for (ExtZipEntry entry : entries) {
					LOG.info( "entry name:" + entry.getName());
					String[] tokens = FileUtils.splitFileParts(entry.getName());
					aesZipFileDecrypter.extractEntry(entry, new File (tokens[0]+tokens[1]+"-unzipped."+tokens[2]), password);
				}

			} catch (IOException | DataFormatException e) {
				e.printStackTrace();
			}


	}

}
