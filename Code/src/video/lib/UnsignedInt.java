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
 * $Id: UnsignedInt.java 313 2005-12-03 17:35:04Z merlimat $
 * 
 * $URL: http://svn.berlios.de/svnroot/repos/rtspproxy/tags/3.0-ALPHA2/src/main/java/rtspproxy/lib/number/UnsignedInt.java $
 * 
 */
package video.lib;

/**
 * The UnsignedInt class wraps a value of an unsigned 32 bits number.
 * 
 * @author Matteo Merli
 */
public final class UnsignedInt extends UnsignedNumber {
  static final long serialVersionUID = 1L;

  private long value;

  public UnsignedInt(byte c) {
    value = c;
  }

  public UnsignedInt(short c) {
    value = c & 0xFFFFL;
  }

  public UnsignedInt(int c) {
    value = c & 0xFFFFFFFFL;
  }

  // FIXME
  public UnsignedInt(long c) {
    value = c & 0xFFFFFFFFL;
  }

  private UnsignedInt() {
    value = 0;
  }

  public static UnsignedInt fromBytes(byte[] c) {
    return fromBytes(c, 0);
  }

  public static UnsignedInt fromBytes(byte[] c, int idx) {
    UnsignedInt number = new UnsignedInt();
    if ((c.length - idx) < 4)
      throw new IllegalArgumentException("An UnsignedInt number is composed of 4 bytes.");

    number.value = (c[0] << 24 | c[1] << 16 | c[2] << 8 | c[3]);
    return number;
  }

  public static UnsignedInt fromString(String c) {
    return fromString(c, 10);
  }

  // FIXME: Need testing.
  public static UnsignedInt fromString(String c, int radix) {
    UnsignedInt number = new UnsignedInt();
    long v = Long.parseLong(c, radix);
    number.value = v & 0xFFFFFFFFL;
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
  public int intValue() {
    return (int) (value & 0xFFFFFFFFL);
  }

  @Override
  public long longValue() {
    return value & 0xFFFFFFFFL;
  }

  @Override
  public byte[] getBytes() {
    byte[] c = new byte[4];
    c[0] = (byte) ((value >> 24) & 0xFF);
    c[1] = (byte) ((value >> 16) & 0xFF);
    c[2] = (byte) ((value >> 8) & 0xFF);
    c[3] = (byte) ((value >> 0) & 0xFF);
    return c;
  }

  @Override
  public int compareTo(UnsignedNumber other) {
    long otherValue = other.longValue();
    if (value > otherValue) {
      return +1;
    } else if (value < otherValue) {
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
    return longValue() == ((Number) other).longValue();
  }

  @Override
  public String toString() {
    return Long.toString((long) value & 0xFFFFFFFFL);
  }

  @Override
  public int hashCode() {
    return (int) (value ^ (value >>> 32));
  }

  @Override
  public void shiftRight(int nBits) {
    if (Math.abs(nBits) > 32)
      throw new IllegalArgumentException("Cannot right shift " + nBits + " an UnsignedInt.");

    value >>>= nBits;
  }

  @Override
  public void shiftLeft(int nBits) {
    if (Math.abs(nBits) > 32)
      throw new IllegalArgumentException("Cannot left shift " + nBits + " an UnsignedInt.");

    value <<= nBits;
  }

}
