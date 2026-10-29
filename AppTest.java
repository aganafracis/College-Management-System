import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Automated acceptance tests and unit verification suite.
 * Run with: java -cp ".;lib/mysql-connector-j-8.3.0.jar" AppTest
 */
public class AppTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("Running Team 3 Application Test Suite");
        System.out.println("=================================================");

        testPasswordHashing();
        testPasswordVerification();
        testStudentValidation();
        testFacultyValidation();
        testPasswordComplexityValidation();
        testDatabaseErrorMessageFormatting();
        testDatabaseOfflineHandling();
        testRememberMePreferences();
        testUIComponentsStructure();

        System.out.println("=================================================");
        System.out.printf("Test Results: %d / %d tests passed.%n", testsPassed, testsRun);
        System.out.println("=================================================");

        if (testsRun != testsPassed) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void testPasswordHashing() {
        System.out.println("\nTesting PasswordUtil PBKDF2 Hashing:");
        char[] pwd1 = "Secret123!".toCharArray();
        String hash1 = PasswordUtil.hashPassword(pwd1);

        assertTrue("Hash format starts with PBKDF2$65536$", hash1.startsWith("PBKDF2$65536$"));

        char[] pwd2 = "Secret123!".toCharArray();
        String hash2 = PasswordUtil.hashPassword(pwd2);

        assertTrue("Unique salt per hash: two identical passwords yield different hashes", !hash1.equals(hash2));
    }

    private static void testPasswordVerification() {
        System.out.println("\nTesting Password Verification:");
        char[] original = "AdminPassword456#".toCharArray();
        String hash = PasswordUtil.hashPassword(original);

        assertTrue("Correct password verifies successfully",
                PasswordUtil.verifyPassword("AdminPassword456#".toCharArray(), hash));

        assertTrue("Incorrect password fails verification",
                !PasswordUtil.verifyPassword("WrongPassword456#".toCharArray(), hash));

        assertTrue("Empty password fails verification",
                !PasswordUtil.verifyPassword("".toCharArray(), hash));

        assertTrue("Tampered hash fails verification",
                !PasswordUtil.verifyPassword("AdminPassword456#".toCharArray(), "PBKDF2$65536$invalid$hash"));
    }

    private static void testStudentValidation() {
        System.out.println("\nTesting Student Input Validation:");

        // Valid case
        String errValid = ValidationUtil.validateStudentInput(
                "Alice Smith", "REG12345", "alice@example.com", "9876543210",
                "2002-04-12", "alicesmith", "Pass1234!".toCharArray(), true
        );
        assertTrue("Valid student inputs pass validation", errValid == null);

        // Missing full name
        String errNoName = ValidationUtil.validateStudentInput(
                "", "REG12345", "alice@example.com", "9876543210",
                "2002-04-12", "alicesmith", "Pass1234!".toCharArray(), true
        );
        assertTrue("Blank student name rejected", errNoName != null && errNoName.contains("Full Name"));

        // Invalid email
        String errEmail = ValidationUtil.validateStudentInput(
                "Alice", "REG12345", "alice-not-an-email", "9876543210",
                "2002-04-12", "alicesmith", "Pass1234!".toCharArray(), true
        );
        assertTrue("Malformed student email rejected", errEmail != null && errEmail.contains("email"));

        // Invalid date format
        String errDob = ValidationUtil.validateStudentInput(
                "Alice", "REG12345", "alice@example.com", "9876543210",
                "12/04/2002", "alicesmith", "Pass1234!".toCharArray(), true
        );
        assertTrue("Non-ISO Date of Birth format rejected", errDob != null && errDob.contains("Date of Birth"));

        // Future date of birth
        String errFutureDob = ValidationUtil.validateStudentInput(
                "Alice", "REG12345", "alice@example.com", "9876543210",
                "2030-01-01", "alicesmith", "Pass1234!".toCharArray(), true
        );
        assertTrue("Future Date of Birth rejected", errFutureDob != null && errFutureDob.contains("future"));

        // Unaccepted terms
        String errTerms = ValidationUtil.validateStudentInput(
                "Alice", "REG12345", "alice@example.com", "9876543210",
                "2002-04-12", "alicesmith", "Pass1234!".toCharArray(), false
        );
        assertTrue("Unchecked terms and conditions rejected", errTerms != null && errTerms.contains("Terms"));
    }

    private static void testFacultyValidation() {
        System.out.println("\nTesting Faculty Input Validation:");

        // Valid case
        String errValid = ValidationUtil.validateFacultyInput(
                "Dr. John Doe", "FAC987", "jdoe@university.edu", "204B",
                "Machine Learning", "johndoe", "SecurePass88!".toCharArray(), true
        );
        assertTrue("Valid faculty inputs pass validation", errValid == null);

        // Missing faculty ID
        String errNoId = ValidationUtil.validateFacultyInput(
                "Dr. John", "", "jdoe@university.edu", "204B",
                "AI", "johndoe", "SecurePass88!".toCharArray(), true
        );
        assertTrue("Missing faculty ID rejected", errNoId != null && errNoId.contains("Faculty ID"));

        // Terms not accepted
        String errTerms = ValidationUtil.validateFacultyInput(
                "Dr. John", "FAC987", "jdoe@university.edu", "204B",
                "AI", "johndoe", "SecurePass88!".toCharArray(), false
        );
        assertTrue("Unchecked faculty Terms of Service rejected", errTerms != null && errTerms.contains("Terms"));
    }

    private static void testPasswordComplexityValidation() {
        System.out.println("\nTesting Password Complexity Rules:");

        assertTrue("Short password (< 8 chars) rejected",
                ValidationUtil.validatePassword("Pass1!".toCharArray()) != null);

        assertTrue("Password without numbers or symbols rejected",
                ValidationUtil.validatePassword("JustLettersHere".toCharArray()) != null);

        assertTrue("Password without letters rejected",
                ValidationUtil.validatePassword("1234567890".toCharArray()) != null);

        assertTrue("Strong password accepted",
                ValidationUtil.validatePassword("ValidPass123".toCharArray()) == null);
    }

    private static void testDatabaseErrorMessageFormatting() {
        System.out.println("\nTesting Database Exception Formatting (Security & Guidance):");

        java.sql.SQLException accessDenied = new java.sql.SQLException("Access denied for user 'root'@'localhost'", "28000", 1045);
        String msg1 = DatabaseConfig.formatSqlException(accessDenied);
        assertTrue("Access denied exception provides clear guidance", msg1.contains("Access denied") || msg1.contains("access denied"));

        java.sql.SQLException commsFail = new java.sql.SQLException("Communications link failure", "08S01");
        String msg2 = DatabaseConfig.formatSqlException(commsFail);
        assertTrue("Communications failure suggests checking MySQL service", msg2.contains("running"));
    }

    private static void testDatabaseOfflineHandling() {
        System.out.println("\nTesting Live/Offline Database Connection Handling:");
        String offlineStatus = DatabaseConfig.testConnection();
        // If MySQL is offline, testConnection returns guidance message without crashing
        if (offlineStatus != null) {
            assertTrue("Offline database returns actionable setup guidance without throwing unhandled exception",
                    offlineStatus.contains("running") || offlineStatus.contains("MySQL") || offlineStatus.contains("Database"));
        } else {
            System.out.println("  [INFO] Local MySQL service is currently online.");
        }
    }

    private static void testRememberMePreferences() {
        System.out.println("\nTesting Remember Me Preferences (Safe Storage):");

        PreferencesUtil.saveRememberPreference("student_alice", "Student");
        assertTrue("Remember Me enabled state persisted", PreferencesUtil.isRememberMeEnabled());
        assertTrue("Remembered username persisted", "student_alice".equals(PreferencesUtil.getRememberedUsername()));
        assertTrue("Remembered role persisted", "Student".equals(PreferencesUtil.getRememberedRole()));

        PreferencesUtil.clearRememberPreference();
        assertTrue("Remember Me state successfully cleared", !PreferencesUtil.isRememberMeEnabled());
    }

    private static void testUIComponentsStructure() {
        System.out.println("\nTesting UI Components Structure & Labels:");

        // Test running on Headless environment safely
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("  [INFO] Headless environment detected - skipping Swing layout rendering.");
            return;
        }

        try {
            LoginFrame loginFrame = new LoginFrame();
            assertTrue("LoginFrame has correct title label",
                    "Login to continue to your account".equals(loginFrame.titleLabel.getText()));
            assertTrue("LoginFrame has Username label",
                    loginFrame.usernameLabel.getText().contains("Register Number"));
            assertTrue("LoginFrame has Student radio button", loginFrame.student != null);
            assertTrue("LoginFrame has Faculty radio button", loginFrame.faculty != null);
            assertTrue("LoginFrame has Remember Me checkbox", loginFrame.rememberMe != null);
            assertTrue("LoginFrame has Login button", "Login".equals(loginFrame.loginButton.getText()));
            assertTrue("LoginFrame has Register button", "Register here".equals(loginFrame.registerButton.getText()));
            assertTrue("LoginFrame has Forgot Password button", "Forgot Password?".equals(loginFrame.forgotPasswordButton.getText()));
            loginFrame.dispose();

            StudentRegistration studentReg = new StudentRegistration();
            assertTrue("StudentRegistration department contains CSE",
                    studentReg.department.getItemAt(0).equals("CSE"));
            assertTrue("StudentRegistration semester contains S1",
                    studentReg.semester.getItemAt(0).equals("S1"));
            assertTrue("StudentRegistration course contains B.Tech CSE",
                    studentReg.course.getItemAt(0).equals("B.Tech CSE"));
            assertTrue("StudentRegistration has Back to Login button", studentReg.backToLogin != null);
            studentReg.dispose();

            FacultyRegistration facultyReg = new FacultyRegistration();
            assertTrue("FacultyRegistration department contains CSE",
                    facultyReg.department.getItemAt(0).equals("CSE"));
            assertTrue("FacultyRegistration designation contains Professor",
                    facultyReg.designation.getItemAt(0).equals("Professor"));
            assertTrue("FacultyRegistration courseLoad contains 1 Course",
                    facultyReg.courseLoad.getItemAt(0).equals("1 Course"));
            assertTrue("FacultyRegistration has Back to Login button", facultyReg.backToLogin != null);
            facultyReg.dispose();

        } catch (Exception e) {
            System.err.println("UI testing exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
