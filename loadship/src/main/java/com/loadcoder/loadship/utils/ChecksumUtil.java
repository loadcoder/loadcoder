/*******************************************************************************
 * Copyright (C) 2021 Team Loadcoder
 * 
 * This file is part of Loadcoder.
 * 
 * Loadcoder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Loadcoder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.loadcoder.loadship.utils;

import java.security.MessageDigest;

public class ChecksumUtil {

	public boolean checksumEquals(byte[] bytes, String checksumToCompare) {
		if (bytes == null || bytes.length == 0) {
			return false;
		}

		String bytesChecksum = md5(bytes);
		if (bytesChecksum.equals(checksumToCompare)) {
			return true;
		}
		return false;
	}

	public static String md5(byte[] bytes) {
		return rebase(md5Bytes(bytes));
	}

	public static byte[] md5Bytes(byte[] bytes) {
		try {
			byte[] md5Byte = MessageDigest.getInstance("MD5").digest(bytes);
			return md5Byte;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String rebase(byte[] bytes) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		// return complete hash
		return sb.toString();
	}
}
