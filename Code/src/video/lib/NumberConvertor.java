/**
 * 
 */
package video.lib;

/**
 * @author yuezhu
 *
 */
public class NumberConvertor {

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.err.println("long.size=" + Long.SIZE);
    short value = -54;
    byte[] bytes = toByteArray(value);
    System.err.println(toShortValue(bytes));
  }
  
  public static byte[] toByteArray(Number data) {  
    int length;  
    long value;

    if (data instanceof Byte) {
//      System.err.println("Byte");
      length = Byte.SIZE / Byte.SIZE;  
      value = (Byte) data;  
    } else if (data instanceof Short) {
//      System.err.println("Short");
      length = Short.SIZE / Byte.SIZE;  
      value = (Short) data;  
    } else if (data instanceof Integer) {
//      System.err.println("Integer");
      length = Integer.SIZE / Byte.SIZE;  
      value = (Integer) data;  
    } else if (data instanceof Long) {
//      System.err.println("Long");
      length = Long.SIZE / Byte.SIZE;  
      value = (Long) data;  
    } else {
      return null;
    }
      
    byte[] byteArray = new byte[length];  
    for (int i = 0; i < length; i++) {  
      byteArray[i] = (byte) ((value >> (8 * (length - i - 1))) & 0xff);  
    }  
    return byteArray;  
  }
  
  public static short toShortValue(byte[] data) {
    if (data == null || data.length != 2)
      return 0x0;
    short value = (short)(
        (short)(0xff & data[0]) << 8 |
        (short)(0xff & data[1]) << 0);
    return value;
        
  }
  
  public static int toIntValue(byte[] data) {
    if (data == null || data.length != 4)
      return 0x0;

    return (int)(
        (int)(0xff & data[0]) << 24 |
        (int)(0xff & data[1]) << 16 |
        (int)(0xff & data[2]) << 8 |
        (int)(0xff & data[3]) << 0);
  }

  public static long toLongValue(byte[] data) {
    if (data == null || data.length != 8)
      return 0x0;
    
    long value = (long)(
        (long)(0xff & data[0]) << 56 |
        (long)(0xff & data[1]) << 48 |
        (long)(0xff & data[2]) << 40 |
        (long)(0xff & data[3]) << 32 |
        (long)(0xff & data[4]) << 24 |
        (long)(0xff & data[5]) << 16 |
        (long)(0xff & data[6]) << 8 |
        (long)(0xff & data[7]) << 0
        ); 

    return value;
  }


}
