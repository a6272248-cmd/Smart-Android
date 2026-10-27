import com.smartmanager.nativeapp.UserManagerService;
public class UserManagerServiceTest {
  public static void main(String[] args) {
    if (!"/tool/user-manager".equals(new UserManagerService(null).commandRoot("6.49.10"))) throw new AssertionError("v6 root");
    if (!"/user-manager".equals(new UserManagerService(null).commandRoot("7.14"))) throw new AssertionError("v7 root");
    System.out.println("PASS: User Manager v6/v7 command roots");
  }
}
