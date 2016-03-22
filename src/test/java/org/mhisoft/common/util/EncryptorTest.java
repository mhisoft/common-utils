
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

import org.junit.Test;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Mar, 2016
 */
public class EncryptorTest {
	@Test
	public void testPBEEncryption() {
		try {
			StandardPBEByteEncryptor encryptor = new StandardPBEByteEncryptor();
			encryptor.setAlgorithm("PBEWithHmacSHA512AndAES_256");
			encryptor.setPassword("testpassword");
			encryptor.setProviderName("SunJCE");


			encryptor.initialize();
			String s1 = "FooBar 23974034 &&^23 时尚 ~!)\\u";
			byte[] enc = encryptor.encrypt(StringUtils.getBytes(s1));
			System.out.println(StringUtils.toHexString(enc));

			byte[]  dec = encryptor.decrypt(enc);
			System.out.println(StringUtils.bytesToString(dec));


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
