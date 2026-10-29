import java.util.prefs.Preferences;

/**
 * Manages Remember Me convenience preferences safely.
 * Only non-sensitive preferences (username and role) are persisted.
 * Passwords and reusable credentials are NEVER stored.
 *
 * Persistence location: Standard OS user preferences backing store
 * (On Windows: HKEY_CURRENT_USER\Software\JavaSoft\Prefs via java.util.prefs).
 */
public class PreferencesUtil {

    private static final String PREF_USERNAME = "remembered_username";
    private static final String PREF_ROLE = "remembered_role";
    private static final String PREF_ENABLED = "remember_me_enabled";

    private static final Preferences prefs = Preferences.userNodeForPackage(PreferencesUtil.class);

    public static void saveRememberPreference(String username, String role) {
        if (username != null && !username.trim().isEmpty()) {
            prefs.put(PREF_USERNAME, username.trim());
            prefs.put(PREF_ROLE, role != null ? role.trim() : "Student");
            prefs.putBoolean(PREF_ENABLED, true);
        }
    }

    public static void clearRememberPreference() {
        prefs.remove(PREF_USERNAME);
        prefs.remove(PREF_ROLE);
        prefs.putBoolean(PREF_ENABLED, false);
    }

    public static boolean isRememberMeEnabled() {
        return prefs.getBoolean(PREF_ENABLED, false);
    }

    public static String getRememberedUsername() {
        return prefs.get(PREF_USERNAME, "");
    }

    public static String getRememberedRole() {
        return prefs.get(PREF_ROLE, "Student");
    }
}
