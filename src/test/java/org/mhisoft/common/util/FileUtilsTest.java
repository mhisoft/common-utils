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
 *  *    http:/www.apache.org/licenses/LICENSE-2.0
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

import org.junit.Assert;
import org.junit.Test;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Jul, 2017
 */
public class FileUtilsTest {


	@Test
	public void getFileNameWithoutPath() {
		String s = "S:/projects/mhisoft/evault-app/dist/test.docx";
		Assert.assertEquals("test.docx", FileUtils.getFileNameWithoutPath(s));

		String s2 = "S:/projects/mhisoft/evault-app/dist";
		Assert.assertEquals(null, FileUtils.getFileNameWithoutPath(s2));

		String s3 = "S:/projects/mhisoft/evault-app/";
		Assert.assertNull(FileUtils.getFileNameWithoutPath(s3));
	}

	@Test
	public void getFileDir() {
		String s = "S:/projects/mhisoft/evault-app/dist/test.docx";
		Assert.assertEquals("S:/projects/mhisoft/evault-app/dist", FileUtils.gerFileDir(s));

		String s2 = "S:/projects/mhisoft/evault-app/dist";
		Assert.assertEquals("S:/projects/mhisoft/evault-app/dist", FileUtils.gerFileDir(s2));

		String s3 = "S:/projects/mhisoft/evault-app/";
		Assert.assertEquals("S:/projects/mhisoft/evault-app", FileUtils.gerFileDir(s3));
	}
}
