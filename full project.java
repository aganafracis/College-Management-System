import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * Main application window for user login.
 * Preserves the original UI fields and layout intent while providing
 * role-aware secure authentication, Remember Me preferences, and registration
 * routing.
 */
public class LoginFrame extends JFrame {

    JLabel titleLabel, usernameLabel, passwordLabel;
    JTextField usernameText;
    JPasswordField passwordText;
    JCheckBox rememberMe;
    JButton loginButton, forgotPasswordButton, registerButton;
    JRadioButton student, faculty;
    ButtonGroup group;

    public LoginFrame() {

        setTitle("Login");
        setSize(450, 440);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(9, 1, 10, 8));

        // Title
        titleLabel = new JLabel("Login to continue to your account");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        add(titleLabel);

        // Student / Faculty
        JPanel rolePanel = new JPanel();

        student = new JRadioButton("Student");
        faculty = new JRadioButton("Faculty");

        group = new ButtonGroup();
        group.add(student);
        group.add(faculty);

        rolePanel.add(student);
        rolePanel.add(faculty);

        add(rolePanel);

        // Username
        JPanel usernamePanel = new JPanel();

        usernameLabel = new JLabel("Username / Register Number / Staff ID");
        usernameText = new JTextField(20);

        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameText);

        add(usernamePanel);

        // Password
        JPanel passwordPanel = new JPanel();

        passwordLabel = new JLabel("Password");
        passwordText = new JPasswordField(20);

        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordText);

        add(passwordPanel);

        // Remember Me
        JPanel rememberPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rememberMe = new JCheckBox("Remember Me");
        rememberPanel.add(rememberMe);
        add(rememberPanel);

        // Forgot Password
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        forgotPasswordButton = new JButton("Forgot Password?");
        forgotPanel.add(forgotPasswordButton);
        add(forgotPanel);

        // Login
        JPanel loginBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(140, 30));
        loginBtnPanel.add(loginButton);
        add(loginBtnPanel);

        // Register
        JPanel registerBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerButton = new JButton("Register here");
        registerButton.setPreferredSize(new Dimension(140, 30));
        registerBtnPanel.add(registerButton);
        add(registerBtnPanel);

        // Load Remember Me preferences if previously saved
        loadRememberPreference();

        // Wire event handlers
        setupEventHandlers();

        // Center the frame
        setLocationRelativeTo(null);

        setVisible(true);
    }

    private void loadRememberPreference() {
        if (PreferencesUtil.isRememberMeEnabled()) {
            String savedUser = PreferencesUtil.getRememberedUsername();
            String savedRole = PreferencesUtil.getRememberedRole();
            usernameText.setText(savedUser);
            if ("Faculty".equalsIgnoreCase(savedRole)) {
                faculty.setSelected(true);
            } else {
                student.setSelected(true);
            }
            rememberMe.setSelected(true);
        }
    }

    private void setupEventHandlers() {
        // Forgot Password Action
        forgotPasswordButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Self-service password recovery is currently not configured.\n\n"
                            + "Please contact your system administrator at admin@institution.edu\n"
                            + "with your registered institutional ID for account assistance.",
                    "Password Recovery",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // Register Choice Action
        registerButton.addActionListener(e -> {
            String[] options = { "Student Registration", "Faculty Registration", "Cancel" };
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Please select your registration type:",
                    "Register New Account",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == 0) {
                this.setVisible(false);
                new StudentRegistration(this);
            } else if (choice == 1) {
                this.setVisible(false);
                new FacultyRegistration(this);
            }
        });

        // Login Action
        loginButton.addActionListener(e -> performLogin());

        // Allow Enter key in password field to trigger login
        passwordText.addActionListener(e -> performLogin());
    }

    private void performLogin() {
        // 1. Role selection required check
        if (!student.isSelected() && !faculty.isSelected()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select your role (Student or Faculty) before logging in.",
                    "Role Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedRole = student.isSelected() ? "Student" : "Faculty";
        String usernameOrId = usernameText.getText().trim();
        char[] password = passwordText.getPassword();

        if (usernameOrId.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your username, register number, or faculty ID.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE);
            PasswordUtil.clearArray(password);
            return;
        }

        if (password.length == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your password.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE);
            PasswordUtil.clearArray(password);
            return;
        }

        // Disable interactive buttons during background JDBC authentication
        setFormBusy(true);

        // Copy password for background worker
        char[] pwdCopy = Arrays.copyOf(password, password.length);
        PasswordUtil.clearArray(password);

        SwingWorker<AuthService.AuthResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AuthService.AuthResult doInBackground() {
                return AuthService.login(usernameOrId, pwdCopy, selectedRole);
            }

            @Override
            protected void done() {
                try {
                    AuthService.AuthResult result = get();
                    setFormBusy(false);

                    if (result.isSuccess()) {
                        // Handle Remember Me preference safely
                        if (rememberMe.isSelected()) {
                            PreferencesUtil.saveRememberPreference(usernameOrId, selectedRole);
                        } else {
                            PreferencesUtil.clearRememberPreference();
                        }

                        passwordText.setText("");
                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "Welcome, " + result.getDisplayName() + "!\n"
                                        + "Successfully logged in as: " + result.getRole(),
                                "Login Successful",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        passwordText.setText("");
                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                result.getMessage(),
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    setFormBusy(false);
                    passwordText.setText("");
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "An unexpected error occurred during login. Please try again.",
                            "System Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void setFormBusy(boolean busy) {
        loginButton.setEnabled(!busy);
        registerButton.setEnabled(!busy);
        forgotPasswordButton.setEnabled(!busy);
        usernameText.setEnabled(!busy);
        passwordText.setEnabled(!busy);
        student.setEnabled(!busy);
        faculty.setEnabled(!busy);
        rememberMe.setEnabled(!busy);
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}
