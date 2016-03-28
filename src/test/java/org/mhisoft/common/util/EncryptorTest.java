
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

package org.mhisoft.common.util;

import java.security.AlgorithmParameters;

import org.junit.Test;

import junit.framework.Assert;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Mar, 2016
 */
public class EncryptorTest {
	static int FIXED_RECORD_LENGTH=2000;
	@Test
	public void testPBEEncryption() {
		try {
			Encryptor encryptor = new Encryptor("testpassword2343");

			String s1 = "1.FooBar 23974034 &&^23 时尚 ~!)\\u";
			String s2 = "2.FooBar 2397 时尚 ~!)\\u";
			byte[] enc = encryptor.encrypt(StringUtils.getBytes(s1));

			System.out.println(StringUtils.toHexString(enc));

			byte[] byteItem = FileUtils.padByteArray(enc, FIXED_RECORD_LENGTH);
			byte[] enc1_2 = FileUtils.trimByteArray(byteItem);

			byte[] params = encryptor.getCipherParameters();


			//decrypt using a new instance of encryptor.
			AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(Encryptor.ALGORITHM);
			algorithmParameters.init(params);


			Encryptor encryptor2 = new Encryptor("testpassword2343");
			byte[]  dec = encryptor2.decrypt(enc1_2, algorithmParameters);
			System.out.println(StringUtils.bytesToString(dec));

			Assert.assertEquals(s1,  StringUtils.bytesToString(dec));


			//again, salt is changed.
			byte[] enc2 = encryptor.encrypt(StringUtils.getBytes(s2));

			//decrypt using a new instance of encryptor.
			byte[] params2  = encryptor.getCipherParameters();
			AlgorithmParameters algorithmParameters2 = AlgorithmParameters.getInstance(Encryptor.ALGORITHM);
			algorithmParameters2.init(params2);

			byte[]  dec2 = encryptor2.decrypt(enc2, algorithmParameters2);
			System.out.println(StringUtils.bytesToString(dec2));
			Assert.assertEquals(s2,  StringUtils.bytesToString(dec2));



		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
