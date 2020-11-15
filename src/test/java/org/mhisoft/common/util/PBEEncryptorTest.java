
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

import org.junit.jupiter.api.*;
import org.mhisoft.common.util.security.PBEEncryptor;

import java.io.File;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;


/**
 * Description:
 *
 * @author Tony Xue
 * @since Mar, 2016
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PBEEncryptorTest {
    static int FIXED_RECORD_LENGTH = 2000;
    static String separator = File.separator.equals("\\") ? "\\\\" : File.separator;

    @Test
    public void testPBEEncryption() {
        try {
            PBEEncryptor encryptor = new PBEEncryptor("testpassword2343");

            String s1 = "1.FooBar 23974034 &&^23 时尚 ~!)\\u";
            String s2 = "2.FooBar 2397 时尚 ~!)\\u";
            PBEEncryptor.EncryptionResult ret = encryptor.encrypt(StringUtils.getBytes(s1));
            byte[] enc = ret.getEncryptedData();

            System.out.println(StringUtils.toHexString(enc));

            byte[] byteItem = FileUtils.padByteArray(enc, FIXED_RECORD_LENGTH);
            byte[] enc1_2 = FileUtils.trimByteArray(byteItem);

            byte[] params = ret.getCipherParameters();


            //decrypt using a new instance of encryptor.
            AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(PBEEncryptor.ALGORITHM);
            algorithmParameters.init(params);


            PBEEncryptor encryptor2 = new PBEEncryptor("testpassword2343");
            byte[] dec = encryptor2.decrypt(enc1_2, algorithmParameters);
            System.out.println(StringUtils.bytesToString(dec));

            Assertions.assertEquals(s1, StringUtils.bytesToString(dec));


            //again, salt is changed.
            ret = encryptor.encrypt(StringUtils.getBytes(s2));
            byte[] enc2 = ret.getEncryptedData();

            //decrypt using a new instance of encryptor.
            byte[] params2 = encryptor.getCipherParameters();
            AlgorithmParameters algorithmParameters2 = AlgorithmParameters.getInstance(PBEEncryptor.ALGORITHM);
            algorithmParameters2.init(params2);

            byte[] dec2 = encryptor2.decrypt(enc2, algorithmParameters2);
            System.out.println(StringUtils.bytesToString(dec2));
            Assertions.assertEquals(s2, StringUtils.bytesToString(dec2));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testIterate() {
        String passBase = "test-23*(&r";
        String s2 = "2.FooBar 2397 时尚 ~!)\\u";

        try {
            for (int i = 0; i < 10; i++) {

                long t1 = System.currentTimeMillis();


                PBEEncryptor encryptor = new PBEEncryptor(passBase + i);
                PBEEncryptor.EncryptionResult ret = encryptor.encrypt(StringUtils.getBytes(s2 + Integer.valueOf(i)));
                byte[] encText = ret.getEncryptedData();
                System.out.println(StringUtils.toHexString(encText));

                byte[] params = encryptor.getCipherParameters();
                System.out.println("params:" + StringUtils.toHexString(params));

                //decrypt using a new instance of encryptor.
                AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(PBEEncryptor.ALGORITHM);
                algorithmParameters.init(params);
                PBEEncryptor decryptor = new PBEEncryptor(passBase + i);
                byte[] dec = decryptor.decrypt(encText, algorithmParameters);
                System.out.println(StringUtils.bytesToString(dec));

                //you code to be timed here
                long t2 = System.currentTimeMillis();
                System.out.println("iteration, took " + (t2 - t1));

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    static PBEEncryptor encryptor;
    static String original = "2.FooBar 2397 时尚 ~!)\\u";
    static String dataFilePath = "./target/testEnc_withJDK8_descwithJDK11.dat";
    static String paramFilePath = "./target/testEnc_withJDK8_descwithJDK11_param.dat";
    static byte[] encedOriginal;
    static byte[] params;

    @BeforeAll
    public static void setup() {
        System.out.println(System.getProperty("java.runtime.version"));

        dataFilePath = dataFilePath.replaceAll("/", separator);
        paramFilePath = paramFilePath.replaceAll("/", separator);
        encryptor = new PBEEncryptor("testpassword2343");
        PBEEncryptor.EncryptionResult ret = encryptor.encrypt(StringUtils.getBytes(original));
        encedOriginal = ret.getEncryptedData();
        System.out.println(StringUtils.toHexString(encedOriginal));

        params = ret.getCipherParameters();

    }

    @Test
    //run this with JDK 8 to save the dataFile
    @Order(1)
    public void testEnc_withJDK8_descwithJDK11_write() {
        try {
            FileUtils.writeFile(encedOriginal, dataFilePath);
            FileUtils.writeFile(params, paramFilePath);
            File dataFile = new File(dataFilePath);
            Assertions.assertTrue(dataFile.exists());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //read with JDK 11
    @Test
    @Order(2)
    public void testEnc_withJDK8_descwithJDK11_read() {
        try {
            //read the file
            byte[] enc2 = FileUtils.readFile(dataFilePath);
            byte[] params = FileUtils.readFile(paramFilePath);
            AlgorithmParameters algorithmParameters;
            try {
                algorithmParameters = AlgorithmParameters.getInstance(PBEEncryptor.ALGORITHM);
                algorithmParameters.init(params);
                byte[] redAndDecrypted = encryptor.decrypt(enc2, algorithmParameters);
                Assertions.assertEquals(original, StringUtils.bytesToString(redAndDecrypted));

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (IOException  e) {
            e.printStackTrace();
        }
    }

}
