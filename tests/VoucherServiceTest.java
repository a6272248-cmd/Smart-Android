import com.smartmanager.nativeapp.VoucherService;
import java.util.*;

public class VoucherServiceTest {
  public static void main(String[] args) {
    List<Map<String,String>> users = new ArrayList<>();
    users.add(Collections.singletonMap("name", "SM0000004"));
    users.add(Collections.singletonMap("name", "SM0000012"));
    users.add(Collections.singletonMap("name", "SM-other"));
    if (VoucherService.nextSerial(users, "SM") != 13) throw new AssertionError("serial reconciliation");
    String p = VoucherService.password();
    if (!p.matches("[0-9]{6}")) throw new AssertionError("password format");
    System.out.println("PASS: router-aware serial allocation and voucher password generation");
  }
}
