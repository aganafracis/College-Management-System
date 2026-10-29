import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

/**
 * Student Registration Window.
 * Preserves all original fields, choices, and labels while integrating
 * input validation, atomic database persistence, and navigation back to Login.
 */
public class StudentRegistration extends JFrame {

    JTextField name, regNo, email, phone, dob, username;
    JPasswordField password;
    JComboBox<String> department, semester, course;
    JCheckBox terms;
    JButton register, backToLogin;

    private final JFrame parentLoginFrame;

    public StudentRegistration() {
        this(null);
    }

    public StudentRegistration(JFrame parentLoginFrame) {
        this.parentLoginFrame = parentLoginFrame;

        setTitle("Student Registration");
        setSize(520, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setLayout(new GridLayout(12, 2, 10, 10));

        // 1. Full Name
        panel.add(new JLabel("Full Name:"));
        name = new JTextField();
        panel.add(name);

        // 2. Register Number
        panel.add(new JLabel("Register Number:"));
        regNo = new JTextField();
        panel.add(regNo);

        // 3. Email
        panel.add(new JLabel("Email Address:"));
        email = new JTextField();
        panel.add(email);

        // 4. Phone Number
        panel.add(new JLabel("Phone Number:"));
        phone = new JTextField();
        panel.add(phone);

        // 5. Date of Birth (ISO YYYY-MM-DD)
        panel.add(new JLabel("Date of Birth (YYYY-MM-DD):"));
        dob = new JTextField();
        dob.setToolTipText("Enter format: YYYY-MM-DD (e.g. 2003-05-15)");
        panel.add(dob);

        // 6. Department
        panel.add(new JLabel("Department:"));
        department = new JComboBox<>(
                new String[]{"CSE", "ECE", "EEE", "ME", "CE"});
        panel.add(department);

        // 7. Semester
        panel.add(new JLabel("Semester:"));
        semester = new JComboBox<>(
                new String[]{"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"});
        panel.add(semester);

        // 8. Course / Program
        panel.add(new JLabel("Course / Program:"));
        course = new JComboBox<>(
                new String[]{"B.Tech CSE", "B.Tech ECE",
                             "B.Tech EEE", "B.Tech ME", "B.Tech CE"});
        panel.add(course);

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
        terms = new JCheckBox("I agree to Terms and Conditions");
        panel.add(terms);

        // Navigation & Register buttons
        backToLogin = new JButton("Back to Login");
        register = new JButton("Register");
        panel.add(backToLogin);
        panel.add(register);

        // Navigation: Back to Login button
        backToLogin.addActionListener(e -> navigateBackToLogin());

        // Handle window close event cleanly
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                navigateBackToLogin();
            }
        });

        // Register button action
        register.addActionListener(e -> performRegistration());

        add(panel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void performRegistration() {
        // Collect inputs
        String fullName = name.getText().trim();
        String registerNum = regNo.getText().trim();
        String emailAddr = email.getText().trim();
        String phoneNum = phone.getText().trim();
        String dobVal = dob.getText().trim();
        String dept = (String) department.getSelectedItem();
        String sem = (String) semester.getSelectedItem();
        String crse = (String) course.getSelectedItem();
        String uname = username.getText().trim();
        char[] pwd = password.getPassword();
        boolean termsAccepted = terms.isSelected();

        // Client-side validation
        String validationError = ValidationUtil.validateStudentInput(
                fullName, registerNum, emailAddr, phoneNum, dobVal, uname, pwd, termsAccepted
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

        AuthService.StudentData data = new AuthService.StudentData(
                fullName, registerNum, emailAddr, phoneNum, dobVal, dept, sem, crse, uname, pwdCopy
        );

        SwingWorker<AuthService.RegistrationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AuthService.RegistrationResult doInBackground() {
                return AuthService.registerStudent(data);
            }

            @Override
            protected void done() {
                try {
                    AuthService.RegistrationResult result = get();
                    setFormBusy(false);

                    if (result.isSuccess()) {
                        JOptionPane.showMessageDialog(
                                StudentRegistration.this,
                                result.getMessage(),
                                "Registration Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        navigateBackToLogin();
                    } else {
                        JOptionPane.showMessageDialog(
                                StudentRegistration.this,
                                result.getMessage(),
                                "Registration Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    setFormBusy(false);
                    JOptionPane.showMessageDialog(
                            StudentRegistration.this,
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
        regNo.setEnabled(!busy);
        email.setEnabled(!busy);
        phone.setEnabled(!busy);
        dob.setEnabled(!busy);
        department.setEnabled(!busy);
        semester.setEnabled(!busy);
        course.setEnabled(!busy);
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
        SwingUtilities.invokeLater(() -> new StudentRegistration());
    }
}
