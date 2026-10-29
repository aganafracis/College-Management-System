import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validates form input fields for student and faculty registrations.
 * Provides clear, specific, actionable feedback for each field.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9\\-\\s()]{7,20}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_.-]{3,30}$");

    public static String validateStudentInput(String name, String regNo, String email, String phone,
                                              String dob, String username, char[] password, boolean termsAccepted) {
        if (name == null || name.trim().isEmpty()) {
            return "Full Name is required.";
        }
        if (regNo == null || regNo.trim().isEmpty()) {
            return "Register Number is required.";
        }
        if (email == null || email.trim().isEmpty()) {
            return "Email Address is required.";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Please enter a valid email address (e.g., student@university.edu).";
        }
        if (phone == null || phone.trim().isEmpty()) {
            return "Phone Number is required.";
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return "Please enter a valid phone number (at least 7 digits, e.g., +1-555-0199).";
        }
        if (dob == null || dob.trim().isEmpty()) {
            return "Date of Birth is required (format: YYYY-MM-DD).";
        }
        try {
            LocalDate parsedDate = LocalDate.parse(dob.trim());
            if (parsedDate.isAfter(LocalDate.now())) {
                return "Date of Birth cannot be in the future.";
            }
            if (parsedDate.isAfter(LocalDate.now().minusYears(12))) {
                return "Student must be at least 12 years old.";
            }
        } catch (DateTimeParseException e) {
            return "Invalid Date of Birth format. Please use ISO format: YYYY-MM-DD (e.g., 2003-05-15).";
        }
        if (username == null || username.trim().isEmpty()) {
            return "Username is required.";
        }
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            return "Username must be 3-30 characters long and contain only letters, numbers, dots, or underscores.";
        }
        String pwdErr = validatePassword(password);
        if (pwdErr != null) {
            return pwdErr;
        }
        if (!termsAccepted) {
            return "You must agree to the Terms and Conditions to register.";
        }
        return null;
    }

    public static String validateFacultyInput(String name, String facultyId, String email, String extension,
                                              String specialization, String username, char[] password,
                                              boolean termsAccepted) {
        if (name == null || name.trim().isEmpty()) {
            return "Full Name is required.";
        }
        if (facultyId == null || facultyId.trim().isEmpty()) {
            return "Faculty ID is required.";
        }
        if (email == null || email.trim().isEmpty()) {
            return "Official Email Address is required.";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Please enter a valid official email address (e.g., faculty@university.edu).";
        }
        if (extension != null && !extension.trim().isEmpty()) {
            if (!extension.trim().matches("^[0-9A-Za-z\\-\\s]{2,15}$")) {
                return "Office Extension must be 2-15 alphanumeric characters.";
            }
        }
        if (specialization == null || specialization.trim().isEmpty()) {
            return "Specialization is required.";
        }
        if (username == null || username.trim().isEmpty()) {
            return "Username is required.";
        }
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            return "Username must be 3-30 characters long and contain only letters, numbers, dots, or underscores.";
        }
        String pwdErr = validatePassword(password);
        if (pwdErr != null) {
            return pwdErr;
        }
        if (!termsAccepted) {
            return "You must agree to the Terms of Service to register.";
        }
        return null;
    }

    public static String validatePassword(char[] password) {
        if (password == null || password.length == 0) {
            return "Password is required.";
        }
        if (password.length < 8) {
            return "Password must be at least 8 characters long.";
        }
        boolean hasLetter = false;
        boolean hasDigitOrSymbol = false;
        for (char c : password) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else {
                hasDigitOrSymbol = true;
            }
        }
        if (!hasLetter || !hasDigitOrSymbol) {
            return "Password must contain at least one letter and at least one number or special character.";
        }
        return null;
    }
}
