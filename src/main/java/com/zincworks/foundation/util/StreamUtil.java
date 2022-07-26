package com.zincworks.foundation.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class StreamUtil {

	private StreamUtil() {

	}

	// 
	/**
	 * convert InputStream to String
	 * @param is InputStream
	 * @return String 
	 * @throws UtilException
	 */
	public static String getStringFromInputStream(InputStream is)
			throws UtilException {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line = null;
		
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append( '\n' );
			}

		} catch (IOException e) {
			
			throw new UtilException(e);
			
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				// ignore throw new UtilException(e);
			}
		}
		return sb.toString();
	}
}