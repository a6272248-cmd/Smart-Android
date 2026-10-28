import java.util.*;
public class StateToggleTest {
  public static void main(String[] args) {
    if (!toggle(false) || toggle(true)) throw new AssertionError("toggle state");
    System.out.println("PASS: disabled state is inverted only after reading current state");
  }
  static boolean toggle(boolean disabled) { return !disabled; }
}
