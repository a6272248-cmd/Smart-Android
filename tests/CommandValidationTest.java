import com.smartmanager.nativeapp.RouterApi;
import java.io.*;
public class CommandValidationTest {
  public static void main(String[] args) throws Exception {
    if (!reject(() -> RouterApi.writeLength(new ByteArrayOutputStream(), -1))) throw new AssertionError("negative length accepted");
    System.out.println("PASS: invalid framing rejected before any network operation");
  }
  private static boolean reject(Throwing r){try{r.run();return false;}catch(Exception e){return true;}}
  interface Throwing { void run() throws Exception; }
}
