import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database configuration and provides JDBC connections.
 * Resolves settings from:
 * 1. Environment variables (DB_URL, DB_USER, DB_PASSWORD)
 * 2. Local 'db.properties' (in working directory or application root)
 * 3. Classpath 'db.properties'
 * 4. Default local development parameters
 */
public class DatabaseConfig {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/team3_auth_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static String configSource = "defaults";
    private static boolean initialized = false;

    static {
        loadConfiguration();
    }

    public static synchronized void loadConfiguration() {
        Properties props = new Properties();
        boolean loadedFromFile = false;

        // 1. Check current working directory
        File propFile = new File("db.properties");

        // 2. If not found in working directory, check the application code directory
        if (!propFile.exists()) {
            try {
                java.net.URL location = DatabaseConfig.class.getProtectionDomain().getCodeSource().getLocation();
                if (location != null) {
                    File codeDir = new File(location.toURI());
                    if (codeDir.isFile()) {
                        codeDir = codeDir.getParentFile();
                    }
                    File candidate = new File(codeDir, "db.properties");
                    if (candidate.exists()) {
                        propFile = candidate;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                props.load(fis);
                loadedFromFile = true;
                configSource = propFile.getAbsolutePath();
            } catch (IOException e) {
                System.err.println("[DatabaseConfig] Warning: Failed to read db.properties: " + e.getMessage());
            }
        } else {
            // 3. Check classpath
            try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                    loadedFromFile = true;
                    configSource = "classpath:db.properties";
                }
            } catch (IOException e) {
                System.err.println("[DatabaseConfig] Warning: Failed to read classpath db.properties: " + e.getMessage());
            }
        }

        // Environment variables have highest precedence
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        if (envUrl != null && !envUrl.trim().isEmpty()) {
            dbUrl = envUrl.trim();
            configSource = "environment variable (DB_URL)";
        } else {
            dbUrl = props.getProperty("db.url", DEFAULT_URL).trim();
        }

        if (envUser != null && !envUser.trim().isEmpty()) {
            dbUser = envUser.trim();
        } else {
            dbUser = props.getProperty("db.user", DEFAULT_USER).trim();
        }

        if (envPass != null) {
            dbPassword = envPass;
        } else {
            dbPassword = props.getProperty("db.password", DEFAULT_PASS);
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConfig] Warning: MySQL Connector/J driver class not found on classpath.");
        }

        System.out.println("[DatabaseConfig] Configuration loaded from " + configSource
                + " [URL: " + getSanitizedUrl() + ", User: " + dbUser + "]");

        initialized = true;
    }

    /**
     * Obtains a fresh database connection.
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadConfiguration();
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Returns the sanitized database URL for diagnostic messages (omits sensitive query info).
     */
    public static String getSanitizedUrl() {
        if (dbUrl == null) return "unknown";
        int qIdx = dbUrl.indexOf('?');
        return (qIdx > 0) ? dbUrl.substring(0, qIdx) : dbUrl;
    }

    /**
     * Extracts database name from JDBC URL.
     */
    public static String getDatabaseName() {
        if (dbUrl == null) return "team3_auth_db";
        try {
            String clean = getSanitizedUrl();
            int slashIdx = clean.lastIndexOf('/');
            if (slashIdx >= 0 && slashIdx < clean.length() - 1) {
                return clean.substring(slashIdx + 1);
            }
        } catch (Exception ignored) {
        }
        return "team3_auth_db";
    }

    /**
     * Tests connectivity to the database.
     * Returns null if successful, or a human-friendly error diagnosis if failed.
     */
    public static String testConnection() {
        try (Connection conn = getConnection()) {
            if (conn.isValid(3)) {
                return null;
            } else {
                return "Database connection test failed (timeout).";
            }
        } catch (SQLException e) {
            return formatSqlException(e);
        }
    }

    /**
     * Formats SQL exceptions into safe, user-friendly guidance while logging
     * the actual underlying exception and stack trace to the developer console.
     */
    public static String formatSqlException(SQLException e) {
        // Log the full underlying exception and stack trace to development console
        System.err.println("\n--- [DatabaseConfig Diagnostic Error] ---");
        System.err.println("Exception: " + e.getClass().getName() + ": " + e.getMessage());
        System.err.println("ErrorCode: " + e.getErrorCode() + ", SQLState: " + e.getSQLState());
        System.err.println("Target URL: " + getSanitizedUrl() + ", Config Source: " + configSource);
        e.printStackTrace(System.err);
        System.err.println("-----------------------------------------\n");

        String msg = e.getMessage() != null ? e.getMessage() : "";
        int errorCode = e.getErrorCode();

        if (msg.contains("Communications link failure") || msg.contains("Connection refused") || "08S01".equals(e.getSQLState())) {
            return "Cannot reach MySQL server at " + getSanitizedUrl() + ".\n\n"
                    + "Please ensure:\n"
                    + "1. Your MySQL service is running.\n"
                    + "2. MySQL is listening on the configured host and port (default: 3306).\n"
                    + "3. The settings in db.properties match your MySQL configuration.\n\n"
                    + "(Check console output for detailed JDBC stack trace)";
        } else if (errorCode == 1045 || msg.contains("Access denied") || "28000".equals(e.getSQLState())) {
            return "Database access denied for user '" + dbUser + "'.\n\n"
                    + "Please verify your username and password in db.properties.";
        } else if (errorCode == 1049 || msg.contains("Unknown database") || "42000".equals(e.getSQLState())) {
            return "Database '" + getDatabaseName() + "' does not exist on the server.\n\n"
                    + "Please run 'schema.sql' in your MySQL client to initialize the database.";
        } else if (errorCode == 1146 || msg.contains("doesn't exist") || "42S02".equals(e.getSQLState())) {
            return "One or more required database tables do not exist.\n\n"
                    + "Please execute 'schema.sql' in your MySQL client to create the required tables.";
        } else {
            return "Database error: " + (msg.length() > 140 ? msg.substring(0, 140) + "..." : msg)
                    + "\n\n(See developer console for details)";
        }
    }
}
