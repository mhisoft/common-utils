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

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
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
	 *
	 * @param pathToFile the path to the file.
	 * @return content of the file in bytes.
	 * @throws IOException
	 */
	public static byte[] readFile(final String pathToFile) throws IOException {
		byte[] array = Files.readAllBytes(new File(pathToFile).toPath());
		return array;
	}


	/**
	 * Read the  <bold>small</bold> file into a byte array.
	 *
	 * @param file the  file.
	 * @return content of the file in bytes.
	 * @throws IOException
	 */
	public static byte[] readFile(final File file) throws IOException {
		byte[] array = Files.readAllBytes(file.toPath());
		return array;
	}


	/**
	 * Read the inputstream to a byte array.
	 *
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static byte[] readFile(InputStream is) throws IOException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		return buffer.toByteArray();
	}


	public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}


	/**
	 * Pad to total length with zeros at the end.
	 *
	 * @param totalLength
	 * @param sourceArray
	 * @return
	 */
	public static byte[] padByteArray(final byte[] sourceArray, final int totalLength) {
		if (sourceArray.length > totalLength)
			throw new RuntimeException("total length is not enough for the target array" + totalLength);
		final ByteBuffer bb = ByteBuffer.allocate(totalLength);
		bb.put(sourceArray);
		return bb.array();
	}

	/**
	 * trim all the zeros in the array from the end.
	 *
	 * @param sourceArray
	 * @return
	 */
	public static byte[] trimByteArray(byte[] sourceArray) {

		int i;
		for (i = sourceArray.length - 1; i >= 0; i--) {
			if (sourceArray[i] == 0) {
				continue;
			} else
				break;
		}

		final ByteBuffer bb = ByteBuffer.allocate(i + 1);
		bb.put(sourceArray, 0, i + 1);
		return bb.array();
	}


	/**
	 * Read 4 bytes and convert to int from the fileInputStream
	 *
	 * @param fileInputStream
	 * @return
	 * @throws IOException
	 */

	//use the DataOutputStream to read/wirte int.
	public static int readInt(FileInputStream fileInputStream) throws IOException {
		byte[] bytesInt = new byte[4];
		int readBytes = fileInputStream.read(bytesInt);
		if (readBytes != 4)
			throw new RuntimeException("didn't read 4 bytes for a integer");

		return ByteArrayHelper.bytesToInt(bytesInt);
	}

	public static int readInt(RandomAccessFile fileInputStream) throws IOException {
		byte[] bytesInt = new byte[4];
		int readBytes = fileInputStream.read(bytesInt);
		if (readBytes != 4)
			throw new RuntimeException("didn't read 4 bytes for a integer");

		return ByteArrayHelper.bytesToInt(bytesInt);
	}

	private static final int BUFFER = 4096 * 16;
	//nioBufferCopy

	/**
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void copyFile(final File source, final File target) throws IOException {
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


	/**
	 * Splut the file with full patch into three tokens. 1. dir, 2.filename, 3. extension
	 * no slash at the end and no dots on the file ext.
	 *
	 * @param fileWithPath
	 * @return
	 */
	public static String[] splitFileParts(final String fileWithPath) {

		String[] ret = new String[3];
		int k = fileWithPath.lastIndexOf(File.separator);
		String dir = "";
		String fileName = "";
		String fileExt = "";
		if (k > -1) {
			dir = fileWithPath.substring(0, k);                         // no slash at the end
			fileName = fileWithPath.substring(k + 1, fileWithPath.length());
		} else
			fileName = fileWithPath;


		String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
		fileName = tokens[0];
		fileExt = tokens[1];


		ret[0] = dir;
		ret[1] = fileName;
		ret[2] = fileExt;
		return ret;
	}


	public static void launchAppOpenFile(String pathToFile) {
		if (Desktop.isDesktopSupported()) {
			try {
				File myFile = new File(pathToFile);
				Desktop.getDesktop().open(myFile);
			} catch (IOException ex) {
				// no application registered for PDFs
				ex.printStackTrace();
			}
		}
	}

	public static boolean fileExists(final String fname) {
		File f = new File(fname);
		return (f.isFile() && f.exists());
	}


	/**
	 * Write the string to file, return the total number of bytes occupied.
	 *
	 * @param out
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static int writeString(DataOutputStream out, String str) throws IOException {
		if (str == null)
			throw new RuntimeException("input str is null");

		byte[] content = StringUtils.getBytes(str);
		//write size
		byte[] size = ByteArrayHelper.intToBytes(content.length);
		out.write(size);
		out.write(content);
		return size.length + content.length;

	}


	public static String readString(FileInputStream fileInputStream) throws IOException {
		int numBytes = FileUtils.readInt(fileInputStream);
		byte[] _byte = new byte[numBytes];
		int readBytes = fileInputStream.read(_byte);
		if (readBytes != numBytes)
			throw new RuntimeException("readString() failed, " + "read " + readBytes + " bytes only, expected to read:" + numBytes);

		return StringUtils.bytesToString(_byte);

	}

	public static String readString(RandomAccessFile raFile) throws IOException {
		int numBytes = FileUtils.readInt(raFile);
		byte[] _byte = new byte[numBytes];
		int readBytes = raFile.read(_byte);
		if (readBytes != numBytes)
			throw new RuntimeException("readString() failed, " + "read " + readBytes + " bytes only, expected to read:" + numBytes);

		return StringUtils.bytesToString(_byte);

	}


	/**
	 * Launch the URL using the default browser.
	 *
	 * @param url
	 */
	public static void launchURL(String url) {

		try {
			if (Desktop.isDesktopSupported()) {
				// Windows
				Desktop.getDesktop().browse(new URI(url));
			} else {
				// Ubuntu
				Runtime runtime = Runtime.getRuntime();
				runtime.exec("/usr/bin/firefox -new-window " + url);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
