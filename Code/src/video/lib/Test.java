/**
 * 
 */
package video.lib;

/**
 * @author yuezhu
 *
 */
public class Test {

  /**
   * @param args
   */
  public static void main(String[] args) {
    UnsignedInt unsignedInt1 = UnsignedInt.fromString("A453CBF5", 16);
    System.err.println("1 hashcode=" + unsignedInt1.hashCode());
    UnsignedInt unsignedInt2 = new UnsignedInt(0xA453CBF5L);
    System.err.println("direct longValue=" + unsignedInt2.longValue());
    System.err.println("2 hashcode=" + unsignedInt2.hashCode());

  }

}
