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
 * $Id: SDESInfo.java 214 2005-09-20 19:49:55Z merlimat $
 * 
 * $URL: http://svn.berlios.de/svnroot/repos/rtspproxy/tags/3.0-ALPHA2/src/main/java/rtspproxy/rtp/rtcp/SDESInfo.java $
 * 
 */

package video.lib;

//import java.nio.ByteBuffer;


import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author mat
 * 
 */
public class SDESInfo implements RtcpInfo {
  private static final Logger LOGGER = LoggerFactory.getLogger(SDESInfo.class);

  public enum Type {
    END(0), CNAME(1), NAME(2), EMAIL(3), PHONE(4), LOC(5), TOOL(6), NOTE(7), PRIV(8);

    public final byte value;

    public static Type fromByte(byte value) {
      for (Type t : Type.values())
        if (t.value == value)
          return t;
      return END;
    }

    private Type(int value) {
      this.value = (byte) value;
    }
  }

  private class Chunk {

    @SuppressWarnings("unused")
    public int ssrc;
    public Type type;
    @SuppressWarnings("unused")
    public byte[] value;
  }

  private Chunk[] chunkList;

  public SDESInfo(RtcpPacket packet, IoBuffer buffer) {
    // int totalBytesToRead = packet.length * 4;
    byte sourceCount = packet.count;

    chunkList = new Chunk[sourceCount];

    for (byte i = 0; i < sourceCount; i++) {
      chunkList[i] = new Chunk();
      Chunk c = chunkList[i];

      c.ssrc = buffer.getInt();
      c.type = Type.fromByte(buffer.get());

      switch (c.type) {
      case PRIV:
        LOGGER.debug("Chunk private...");
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see rtspproxy.rtp.rtcp.RtcpInfo#toBuffer()
   */
  public IoBuffer toBuffer() {
    // TODO Auto-generated method stub
    return null;
  }

}
