/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   Copyright (C) 2005 - Matteo Merli - matteo.merli@gmail.com            *
 *                                                                         *
 ***************************************************************************/

/*
 * $Id: UnsignedShort.java 316 2005-12-04 14:48:53Z merlimat $
 * 
 * $URL: http://svn.berlios.de/svnroot/repos/rtspproxy/tags/3.0-ALPHA2/src/main/java/rtspproxy/lib/number/UnsignedShort.java $
 * 
 */
package video.lib;

/**
 * The UnsignedByte class wraps a value of an unsigned 16 bits number.
 * 
 * @author Matteo Merli
 */
public final class UnsignedShort extends UnsignedNumber {
	static final long serialVersionUID = 1L;

	private int value;

	public UnsignedShort(byte c) {
		value = c;
	}

	public UnsignedShort(short c) {
		value = c & 0xFFFF;
	}

	public UnsignedShort(int c) {
		value = c & 0xFFFF;
	}

	public UnsignedShort(long c) {
		value = (int) (c & 0xFFFFL);
	}

	private UnsignedShort() {
		value = 0;
	}

	public static UnsignedShort fromBytes(byte[] c) {
		return fromBytes(c, 0);
	}

	public static UnsignedShort fromBytes(byte[] c, int idx) {
		UnsignedShort number = new UnsignedShort();
		if ((c.length - idx) < 2)
			throw new IllegalArgumentException(
					"An UnsignedShort number is composed of 2 bytes.");

		number.value = (c[0] << 8 | c[1]) & 0xFFFF;
		return number;
	}

	public static UnsignedShort fromString(String c) {
		return fromString(c, 10);
	}

	public static UnsignedShort fromString(String c, int radix) {
		UnsignedShort number = new UnsignedShort();
		long v = Integer.parseInt(c, radix);
		number.value = (int) (v & 0xFFFF);
		return number;
	}

	@Override
	public double doubleValue() {
		return (double) value;
	}

	@Override
	public float floatValue() {
		return (float) value;
	}

	@Override
	public short shortValue() {
		return (short) (value & 0xFFFF);
	}

	@Override
	public int intValue() {
		return value & 0xFFFF;
	}

	@Override
	public long longValue() {
		return value & 0xFFFFL;
	}

	@Override
	public byte[] getBytes() {
		byte[] c = new byte[2];
		c[0] = (byte) ((value >> 8) & 0xFF);
		c[1] = (byte) ((value >> 0) & 0xFF);
		return c;
	}

	@Override
	public int compareTo(UnsignedNumber other) {
		int otherValue = other.intValue();
		if (value > otherValue) {
			return +1;
		}
		else if (value < otherValue) {
			return -1;
		} else {
		  return 0;
		}
	}
	
	// FIXME: Need testing!
	// Fixed by yuezhu
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Number)) {
			return false;
		}
		return this.intValue() == ((Number) other).intValue();
	}
	
	@Override
	public int hashCode() {
		return (int)value;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

	@Override
	public void shiftRight(int nBits) {
		if (Math.abs(nBits) > 16)
			throw new IllegalArgumentException("Cannot right shift " + nBits
					+ " an UnsignedShort.");

		value >>>= nBits;
	}

	@Override
	public void shiftLeft(int nBits) {
		if (Math.abs(nBits) > 16)
			throw new IllegalArgumentException("Cannot left shift " + nBits
					+ " an UnsignedShort.");

		value <<= nBits;
	}

}
