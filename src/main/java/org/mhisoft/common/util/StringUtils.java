package org.mhisoft.common.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.IllegalCharsetNameException;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Mar, 2016
 */
public class StringUtils {
	public static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * String to Byte array
	 * @param str
	 * @return
	 */
	public static byte[] getBytes(final String str) {
		byte[] result;
		if (str == null) {
			result = new byte[0];
		} else {
			try {
				result = str.getBytes(DEFAULT_ENCODING);
			} catch (final UnsupportedEncodingException e) {
				throw new IllegalCharsetNameException(DEFAULT_ENCODING);
			}
		}
		return result;
	}

	/**
	 * Convert byte array into hex string that is displable.
	 * @param b
	 * @return
	 */
	public  static String toHexString(final byte[] b) {
		final StringBuffer result = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			result.append(Integer.toHexString(b[i] >> 4 & 0xF));
			result.append(Integer.toHexString(b[i] & 0xF));
		}
		return result.toString();
	}

	/**
	 * Get string from bytes.
	 *
	 * @param bytes
	 * @return String
	 */
	public static String bytesToString(final byte[] bytes) {
		String result;
		if (bytes == null) {
			result = "";
		} else {
			try {
				result = new String(bytes, DEFAULT_ENCODING);
			} catch (final UnsupportedEncodingException e) {
				throw new IllegalCharsetNameException(DEFAULT_ENCODING);
			}
		}
		return result;
	}
}
