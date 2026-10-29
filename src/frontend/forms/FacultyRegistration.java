import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

/**
 * Faculty Registration Window.
 * Preserves all original fields, choices, and labels while integrating
 * input validation, atomic database persistence, and navigation back to Login.
 */
public class FacultyRegistration extends JFrame {

    JTextField name, facultyId, email, extension, specialization;
    JTextField username;
    JPasswordField password;
    JComboBox<String> department, designation, courseLoad;
    JCheckBox terms;
    JButton register, backToLogin;

    private final JFrame parentLoginFrame;

    public FacultyRegistration() {
        this(null);
    }

    public FacultyRegistration(JFrame parentLoginFrame) {
        this.parentLoginFrame = parentLoginFrame;

        setTitle("Faculty Registration");
        setSize(520, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setLayout(new GridLayout(0, 2, 10, 10));

        // 1. Full Name
        panel.add(new JLabel("Full Name:"));
        name = new JTextField();
        panel.add(name);

        // 2. Faculty ID
        panel.add(new JLabel("Faculty ID:"));
        facultyId = new JTextField();
        panel.add(facultyId);

        // 3. Official Email
        panel.add(new JLabel("Official Email Address:"));
        email = new JTextField();
        panel.add(email);

        // 4. Office Extension
        panel.add(new JLabel("Office Extension:"));
        extension = new JTextField();
        panel.add(extension);

        // 5. Department
        panel.add(new JLabel("Department / Faculty:"));
        department = new JComboBox<>(new String[]{
            "CSE", "ECE", "EEE", "ME", "CE"
        });
        panel.add(department);

        // 6. Designation
        panel.add(new JLabel("Designation / Rank:"));
        designation = new JComboBox<>(new String[]{
            "Professor", "Associate Professor",
            "Assistant Professor", "Lecturer"
        });
        panel.add(designation);

        // 7. Specialization
        panel.add(new JLabel("Specialization:"));
        specialization = new JTextField();
        panel.add(specialization);

        // 8. Current Course Load
        panel.add(new JLabel("Current Course Load:"));
        courseLoad = new JComboBox<>(new String[]{
            "1 Course", "2 Courses", "3 Courses",
            "4 Courses", "5+ Courses"
        });
        panel.add(courseLoad);

        // 9. Username
        panel.add(new JLabel("Username:"));
        username = new JTextField();
        panel.add(username);

        // 10. Password
        panel.add(new JLabel("Password:"));
        password = new JPasswordField();
        panel.add(password);

        // Terms
        panel.add(new JLabel(""));
        terms = new JCheckBox("I agree to the Terms of Service");
        panel.add(terms);

        // Navigation & Register Buttons
        backToLogin = new JButton("Back to Login");
        register = new JButton("Register");
        panel.add(backToLogin);
        panel.add(register);

        // Navigation: Back to Login button
        backToLogin.addActionListener(e -> navigateBackToLogin());

        // Handle window close cleanly
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                navigateBackToLogin();
            }
        });

        // Register Action
        register.addActionListener(e -> performRegistration());

        add(panel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void performRegistration() {
        // Collect inputs
        String fullName = name.getText().trim();
        String facId = facultyId.getText().trim();
        String emailAddr = email.getText().trim();
        String officeExt = extension.getText().trim();
        String dept = (String) department.getSelectedItem();
        String desig = (String) designation.getSelectedItem();
        String spec = specialization.getText().trim();
        String cLoad = (String) courseLoad.getSelectedItem();
        String uname = username.getText().trim();
        char[] pwd = password.getPassword();
        boolean termsAccepted = terms.isSelected();

        // Client-side validation
        String validationError = ValidationUtil.validateFacultyInput(
                fullName, facId, emailAddr, officeExt, spec, uname, pwd, termsAccepted
        );

        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError, "Validation Error", JOptionPane.WARNING_MESSAGE);
            PasswordUtil.clearArray(pwd);
            return;
        }

        // Disable buttons during submission
        setFormBusy(true);

        char[] pwdCopy = Arrays.copyOf(pwd, pwd.length);
        PasswordUtil.clearArray(pwd);

        AuthService.FacultyData data = new AuthService.FacultyData(
                fullName, facId, emailAddr, officeExt, dept, desig, spec, cLoad, uname, pwdCopy
        );

        SwingWorker<AuthService.RegistrationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AuthService.RegistrationResult doInBackground() {
                return AuthService.registerFaculty(data);
            }

            @Override
            protected void done() {
                try {
                    AuthService.RegistrationResult result = get();
                    setFormBusy(false);

                    if (result.isSuccess()) {
                        JOptionPane.showMessageDialog(
                                FacultyRegistration.this,
                                result.getMessage(),
                                "Registration Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        navigateBackToLogin();
                    } else {
                        JOptionPane.showMessageDialog(
                                FacultyRegistration.this,
                                result.getMessage(),
                                "Registration Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    setFormBusy(false);
                    JOptionPane.showMessageDialog(
                            FacultyRegistration.this,
                            "An unexpected error occurred during registration. Please try again.",
                            "System Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void setFormBusy(boolean busy) {
        register.setEnabled(!busy);
        backToLogin.setEnabled(!busy);
        name.setEnabled(!busy);
        facultyId.setEnabled(!busy);
        email.setEnabled(!busy);
        extension.setEnabled(!busy);
        department.setEnabled(!busy);
        designation.setEnabled(!busy);
        specialization.setEnabled(!busy);
        courseLoad.setEnabled(!busy);
        username.setEnabled(!busy);
        password.setEnabled(!busy);
        terms.setEnabled(!busy);
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void navigateBackToLogin() {
        dispose();
        if (parentLoginFrame != null) {
            parentLoginFrame.setVisible(true);
        } else {
            new LoginFrame();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FacultyRegistration());
    }
}
