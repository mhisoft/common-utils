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

import java.io.IOException;
import java.security.AlgorithmParameters;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.salt.RandomSaltGenerator;

/**
 * Description: Encryptor does encryption and decryption
 *
 * @author Tony Xue
 * @since Mar, 2016
 */
public class Encryptor {

	String password;
	private StandardPBEByteEncryptor encryptor;
	//private static Encryptor instance ;
	public static final String ALGORITHM = "PBEWithHmacSHA512AndAES_256";

//	public static Encryptor createInstance(final String password) {
//        instance = new Encryptor(password);
//		return instance;
//	}
//
//	public static Encryptor getInstance() {
//		return instance;
//	}


	//share a  resuable salt generator.
	static RandomSaltGenerator saltGenerator = new RandomSaltGenerator() ;

	public Encryptor() {

	}

	public Encryptor(String password) {
		init(password);
	}

	//need password to init.
	protected void init(String pass) {
		this.password = pass;
		encryptor = new StandardPBEByteEncryptor();
		encryptor.setSaltGenerator(saltGenerator);
		encryptor.setAlgorithm(ALGORITHM);
		encryptor.setPassword(this.password);
		encryptor.setProviderName("SunJCE");
		encryptor.initialize();

		//initialize and generate a salt
		Thread t = new Thread( new SaltInit() );
		t.setDaemon(true);
		t.start();


	}

	/**
	 * Get and save the params after the encryption. It has random salt and IvParameterSpec
	 * @return
	 * @throws IOException
	 */
	public byte[] getCipherParameters() throws IOException {
		return encryptor.getCipherParameters();
	}


	public byte[] encrypt(byte[] input) throws EncryptionOperationNotPossibleException {
		byte[] enc = encryptor.encrypt(input);
		return enc;
	}

	public byte[] decrypt(byte[] input, AlgorithmParameters algorithmParameters) throws EncryptionOperationNotPossibleException {
		byte[] dec = encryptor.decrypt(input, algorithmParameters);
		return dec;
	}

	class SaltInit implements  Runnable {
		@Override
		public void run() {
			//call this once to prepare the salt generator.
			//which take time.
			long t1 = System.currentTimeMillis();
			encryptor.generateSalt();
			long t2 = System.currentTimeMillis();
			System.out.println("encryptor initialized, took " +(t2-t1));

		}
	}


}
