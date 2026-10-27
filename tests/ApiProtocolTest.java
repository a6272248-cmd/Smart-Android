import com.smartmanager.nativeapp.RouterApi;
import java.io.*;
import java.util.*;
public class ApiProtocolTest {
 public static void main(String[] a) throws Exception {
  int[] lengths={0,1,127,128,16383,16384,2097151,2097152,4194304};
  for(int x:lengths){ByteArrayOutputStream b=new ByteArrayOutputStream();RouterApi.writeLength(b,x);
   int read=RouterApi.readLength(new ByteArrayInputStream(b.toByteArray()));if(read!=x)throw new AssertionError(x+" vs "+read);}
  byte[][] failures={{(byte)0xF1},{(byte)0x80},{(byte)0xF0,1,2}};
  for(byte[] f:failures){try{RouterApi.readLength(new ByteArrayInputStream(f));throw new AssertionError("Accepted invalid prefix");}catch(IOException expected){}}
  System.out.println("PASS: RouterOS length framing, boundary values and truncated inputs");
 }
}
