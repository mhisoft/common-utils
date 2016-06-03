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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

/**
 * Description:  File realted Utils
 *
 * @author Tony Xue
 * @since Mar, 2016
 */
public class FileUtils {

	/**
	 * Read the  <bold>small</bold> file into a byte array.
	 * @param pathToFile the path to the file.
	 * @return content of the file in bytes.
	 * @throws IOException
	 */
	public static byte[] readFile(final String pathToFile) throws IOException {
		byte[] array = Files.readAllBytes(new File(pathToFile).toPath());
		return array;
	}


	public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}


	public static int byteArrayToInt(byte[] b) {
		final ByteBuffer bb = ByteBuffer.wrap(b);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}

	public static byte[] intToByteArray(int i) {
		final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(i);
		return bb.array();
	}

	/**
	 * Pad to total length with zeros at the end.
	 * @param totalLength
	 * @param sourceArray
	 * @return
	 */
	public static byte[] padByteArray(final  byte[] sourceArray, final int totalLength) {
		if (sourceArray.length>totalLength)
			throw new RuntimeException("total length is not enough for the target array" + totalLength);
		final ByteBuffer bb = ByteBuffer.allocate(totalLength);
		bb.put(sourceArray);
		return bb.array();
	}

	/**
	 * trim all the zeros in the array from the end.
	 * @param sourceArray
	 * @return
	 */
	public static byte[] trimByteArray( byte[] sourceArray) {

		int i;
		for ( i = sourceArray.length-1; i >= 0; i--) {
			if (sourceArray[i]==0) {
				 continue;
			}
			else
				break;
		}

		final ByteBuffer bb = ByteBuffer.allocate(i+1);
		bb.put(sourceArray, 0, i+1);
		return bb.array();
	}


	/**
	 * Read 4 bytes and convert to int from the fileInputStream
	 * @param fileInputStream
	 * @return
	 * @throws IOException
	 */
	public static int readInt(FileInputStream fileInputStream) throws IOException {
		byte[] bytesInt = new byte[4];
		int readBytes = fileInputStream.read(bytesInt);
		if (readBytes!=4)
			throw new RuntimeException("didn't read 4 bytes for a integer");

		return  FileUtils.byteArrayToInt(bytesInt);
	}

	private static final int BUFFER = 4096*16;
	//nioBufferCopy

	/**
	 *
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void copyFile(final File source, final File target  ) throws IOException {
		FileChannel in = null;
		FileChannel out = null;
	//	long totalFileSize = 0;

		try {
			in = new FileInputStream(source).getChannel();
			out = new FileOutputStream(target).getChannel();
			//totalFileSize = in.size();

			ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER);
			int readSize = in.read(buffer);
			long totalRead = 0;
			//int progress = 0;

			//long startTime, endTime  ;

			while (readSize != -1) {

				//startTime = System.currentTimeMillis();
				totalRead = totalRead + readSize;

				//progress = (int) (totalRead * 100 / totalFileSize);


				buffer.flip();

				while (buffer.hasRemaining()) {
					out.write(buffer);
					//System.out.printf(".");
					//showPercent(rdProUI, totalSize/size );
				}
				buffer.clear();
				readSize = in.read(buffer);


				//endTime = System.currentTimeMillis();
			}



		} finally {
			close(in);
			close(out);

		}
	}


	private static void close(Closeable closable) {
		if (closable != null) {
			try {
				closable.close();
			} catch (IOException e) {
				//
			}
		}
	}


}
