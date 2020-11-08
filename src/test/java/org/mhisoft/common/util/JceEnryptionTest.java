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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mhisoft.common.util.security.JceEncryption;
import org.mhisoft.common.util.security.KeySerializer;

import java.security.GeneralSecurityException;
import java.security.Key;

public class JceEnryptionTest {
	static JceEncryption engine;

	@BeforeAll
	public static void setup() {
			engine = JceEncryption.getDefaultEngine();
	}

	@Test
	public void testEncryptDecrypt() throws Exception {
		System.out.println(engine);
		Assertions.assertEquals("256", "" + engine.getKeySize());
		String text = "Test123!";
		String s = engine.encrypt(text);
		System.out.println("Encrypted value:" + s);
		Assertions.assertEquals(engine.decrypt(s), text);

		//test null text
		for (int i = 0; i <10; i++) {
			text = null;
			s = engine.encrypt(text);
			String de = engine.decrypt(s);
			Assertions.assertEquals(de, "");

			//test null text
			text = "a";
			s = engine.encrypt(text);
			Assertions.assertEquals(engine.decrypt(s), text);
		}

	}

	@Test
	public void testEncrypt2_reInitEngine() throws Exception {
		String text = "Test123!";
		String s = engine.encrypt(text);
		System.out.println(s);

		JceEncryption engine2 = JceEncryption.createEngine();
		String s2= engine2.encrypt(text);
		System.out.println(s2);
		Assertions.assertNotSame(s,s2);

	}


	//can't just decrypt, need the same IV used by encrypt for decrypt.
	@Test
	public void testDecrypt() throws Exception {
		String text = "Test123!";
		String enc1 = "Zbfi2CUsIHcv0VB3QMzDkg==";
		try {
			System.out.println("decrypted to:"+engine.decrypt(enc1));
			Assertions.fail("YOu should not be able to decrypt.");
		} catch (GeneralSecurityException e) {
			//
		}
	}

	@Test
	public void testDecryptWithWrongkey() throws Exception {
		String text = "Test123!";
		String encS = engine.encrypt(text);

		Key k = engine.generateKey();
		JceEncryption engine2 = new JceEncryption(KeySerializer.instance.serialize(k));
		try {
			engine2.decrypt(encS);
			Assertions.fail("YOu should not be able to decrypt using another key.");
		} catch (GeneralSecurityException e) {
			//javax.crypto.BadPaddingException: Given final block not properly padded
			//e.printStackTrace();
		}


	}



}
