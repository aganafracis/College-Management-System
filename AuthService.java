import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Service and Data Access layer for User Authentication and Registration.
 * Executes parameterized queries and ensures atomic multi-table registration transactions.
 */
public class AuthService {

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final String username;
        private final String displayName;
        private final String role;

        private AuthResult(boolean success, String message, String username, String displayName, String role) {
            this.success = success;
            this.message = message;
            this.username = username;
            this.displayName = displayName;
            this.role = role;
        }

        public static AuthResult success(String username, String displayName, String role) {
            return new AuthResult(true, "Authentication successful.", username, displayName, role);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, message, null, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getUsername() {
            return username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getRole() {
            return role;
        }
    }

    public static class RegistrationResult {
        private final boolean success;
        private final String message;

        public RegistrationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static RegistrationResult success(String message) {
            return new RegistrationResult(true, message);
        }

        public static RegistrationResult failure(String message) {
            return new RegistrationResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class StudentData {
        public String name;
        public String regNo;
        public String email;
        public String phone;
        public String dob; // YYYY-MM-DD
        public String department;
        public String semester;
        public String course;
        public String username;
        public char[] password;

        public StudentData(String name, String regNo, String email, String phone, String dob,
                           String department, String semester, String course, String username,
                           char[] password) {
            this.name = name != null ? name.trim() : "";
            this.regNo = regNo != null ? regNo.trim() : "";
            this.email = email != null ? email.trim() : "";
            this.phone = phone != null ? phone.trim() : "";
            this.dob = dob != null ? dob.trim() : "";
            this.department = department;
            this.semester = semester;
            this.course = course;
            this.username = username != null ? username.trim() : "";
            this.password = password;
        }
    }

    public static class FacultyData {
        public String name;
        public String facultyId;
        public String email;
        public String extension;
        public String department;
        public String designation;
        public String specialization;
        public String courseLoad;
        public String username;
        public char[] password;

        public FacultyData(String name, String facultyId, String email, String extension,
                           String department, String designation, String specialization,
                           String courseLoad, String username, char[] password) {
            this.name = name != null ? name.trim() : "";
            this.facultyId = facultyId != null ? facultyId.trim() : "";
            this.email = email != null ? email.trim() : "";
            this.extension = extension != null ? extension.trim() : "";
            this.department = department;
            this.designation = designation;
            this.specialization = specialization != null ? specialization.trim() : "";
            this.courseLoad = courseLoad;
            this.username = username != null ? username.trim() : "";
            this.password = password;
        }
    }

    /**
     * Authenticates a user against the selected role.
     * Uses parameterized queries and verifies the PBKDF2 password hash.
     * For Student: accepts username or register number.
     * For Faculty: accepts username or faculty ID.
     */
    public static AuthResult login(String identifier, char[] password, String role) {
        if (identifier == null || identifier.trim().isEmpty() || password == null || password.length == 0) {
            return AuthResult.failure("Please enter both username/ID and password.");
        }
        if (role == null || (!role.equalsIgnoreCase("Student") && !role.equalsIgnoreCase("Faculty"))) {
            return AuthResult.failure("Role selection (Student or Faculty) is required for login.");
        }

        String normalizedRole = role.toUpperCase();
        String sql;
        if ("STUDENT".equals(normalizedRole)) {
            sql = "SELECT a.id, a.username, a.password_hash, a.role, s.full_name "
                + "FROM accounts a "
                + "JOIN student_profiles s ON a.id = s.account_id "
                + "WHERE a.role = 'STUDENT' AND (a.username = ? OR s.register_number = ?)";
        } else {
            sql = "SELECT a.id, a.username, a.password_hash, a.role, f.full_name "
                + "FROM accounts a "
                + "JOIN faculty_profiles f ON a.id = f.account_id "
                + "WHERE a.role = 'FACULTY' AND (a.username = ? OR f.faculty_id = ?)";
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String idTrimmed = identifier.trim();
            ps.setString(1, idTrimmed);
            ps.setString(2, idTrimmed);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    boolean passwordVerified = PasswordUtil.verifyPassword(password, storedHash);
                    if (passwordVerified) {
                        return AuthResult.success(
                                rs.getString("username"),
                                rs.getString("full_name"),
                                rs.getString("role")
                        );
                    }
                }
            }
            // Return identical generic message regardless of whether identifier or password was invalid
            return AuthResult.failure("Invalid username/ID or password for the selected role.");

        } catch (SQLException e) {
            System.err.println("[AuthService] Login DB error: " + e.getMessage());
            return AuthResult.failure(DatabaseConfig.formatSqlException(e));
        } finally {
            PasswordUtil.clearArray(password);
        }
    }

    /**
     * Atomically registers a student in a single database transaction.
     * Inserts into accounts and student_profiles. Rolls back on any failure.
     */
    public static RegistrationResult registerStudent(StudentData data) {
        String validationError = ValidationUtil.validateStudentInput(
                data.name, data.regNo, data.email, data.phone, data.dob, data.username, data.password, true
        );
        if (validationError != null) {
            PasswordUtil.clearArray(data.password);
            return RegistrationResult.failure(validationError);
        }

        String passwordHash = PasswordUtil.hashPassword(data.password);
        PasswordUtil.clearArray(data.password);

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            int accountId;
            String insertAccountSql = "INSERT INTO accounts (username, password_hash, role) VALUES (?, ?, 'STUDENT')";
            try (PreparedStatement psAccount = conn.prepareStatement(insertAccountSql, Statement.RETURN_GENERATED_KEYS)) {
                psAccount.setString(1, data.username);
                psAccount.setString(2, passwordHash);
                psAccount.executeUpdate();

                try (ResultSet rs = psAccount.getGeneratedKeys()) {
                    if (rs.next()) {
                        accountId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated account ID.");
                    }
                }
            }

            String insertProfileSql = "INSERT INTO student_profiles "
                    + "(account_id, full_name, register_number, email, phone, date_of_birth, department, semester, course_program, terms_accepted, terms_accepted_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE, NOW())";
            try (PreparedStatement psProfile = conn.prepareStatement(insertProfileSql)) {
                psProfile.setInt(1, accountId);
                psProfile.setString(2, data.name);
                psProfile.setString(3, data.regNo);
                psProfile.setString(4, data.email);
                psProfile.setString(5, data.phone);
                psProfile.setDate(6, Date.valueOf(data.dob));
                psProfile.setString(7, data.department);
                psProfile.setString(8, data.semester);
                psProfile.setString(9, data.course);
                psProfile.executeUpdate();
            }

            conn.commit();
            return RegistrationResult.success("Student registration successful! You can now log in.");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    System.err.println("[AuthService] Rollback failed: " + rbEx.getMessage());
                }
            }
            return RegistrationResult.failure(handleDuplicateOrDbError(e, data.username, data.regNo, data.email));
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // Ignore close exception
                }
            }
        }
    }

    /**
     * Atomically registers a faculty member in a single database transaction.
     * Inserts into accounts and faculty_profiles. Rolls back on any failure.
     */
    public static RegistrationResult registerFaculty(FacultyData data) {
        String validationError = ValidationUtil.validateFacultyInput(
                data.name, data.facultyId, data.email, data.extension, data.specialization,
                data.username, data.password, true
        );
        if (validationError != null) {
            PasswordUtil.clearArray(data.password);
            return RegistrationResult.failure(validationError);
        }

        String passwordHash = PasswordUtil.hashPassword(data.password);
        PasswordUtil.clearArray(data.password);

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            int accountId;
            String insertAccountSql = "INSERT INTO accounts (username, password_hash, role) VALUES (?, ?, 'FACULTY')";
            try (PreparedStatement psAccount = conn.prepareStatement(insertAccountSql, Statement.RETURN_GENERATED_KEYS)) {
                psAccount.setString(1, data.username);
                psAccount.setString(2, passwordHash);
                psAccount.executeUpdate();

                try (ResultSet rs = psAccount.getGeneratedKeys()) {
                    if (rs.next()) {
                        accountId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated account ID.");
                    }
                }
            }

            String insertProfileSql = "INSERT INTO faculty_profiles "
                    + "(account_id, full_name, faculty_id, email, office_extension, department, designation, specialization, current_course_load, terms_accepted, terms_accepted_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE, NOW())";
            try (PreparedStatement psProfile = conn.prepareStatement(insertProfileSql)) {
                psProfile.setInt(1, accountId);
                psProfile.setString(2, data.name);
                psProfile.setString(3, data.facultyId);
                psProfile.setString(4, data.email);
                psProfile.setString(5, data.extension.isEmpty() ? null : data.extension);
                psProfile.setString(6, data.department);
                psProfile.setString(7, data.designation);
                psProfile.setString(8, data.specialization.isEmpty() ? null : data.specialization);
                psProfile.setString(9, data.courseLoad);
                psProfile.executeUpdate();
            }

            conn.commit();
            return RegistrationResult.success("Faculty registration successful! You can now log in.");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    System.err.println("[AuthService] Rollback failed: " + rbEx.getMessage());
                }
            }
            return RegistrationResult.failure(handleDuplicateOrDbError(e, data.username, data.facultyId, data.email));
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // Ignore close exception
                }
            }
        }
    }

    private static String handleDuplicateOrDbError(SQLException e, String username, String idNumber, String email) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        int errorCode = e.getErrorCode();

        // MySQL duplicate key error code is 1062 (SQLState 23000)
        if (errorCode == 1062 || "23000".equals(e.getSQLState())) {
            String lower = msg.toLowerCase();
            if (lower.contains("username") || lower.contains("idx_accounts_username")) {
                return "The username '" + username + "' is already taken. Please choose another username.";
            } else if (lower.contains("register_number") || lower.contains("idx_student_reg_no")) {
                return "The register number '" + idNumber + "' is already registered.";
            } else if (lower.contains("faculty_id") || lower.contains("idx_faculty_id")) {
                return "The faculty ID '" + idNumber + "' is already registered.";
            } else if (lower.contains("email")) {
                return "The email address '" + email + "' is already registered.";
            }
            return "A record with one of these unique identifiers already exists in the system.";
        }

        return DatabaseConfig.formatSqlException(e);
    }
}
