/**
 * Alias wrapper for LoginFrame.
 * Preserved to allow wildcard compilation ('javac *.java') without duplicate public class conflicts.
 */
public class loginf extends LoginFrame {
    public static void main(String[] args) {
        new LoginFrame();
    }
}