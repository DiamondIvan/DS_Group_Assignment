import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class LoginGUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton loginButton;
    private JButton registerButton;

    private static final Map<String, String> librarianDatabase = new HashMap<>();
    private static final Map<String, String> studentDatabase = new HashMap<>();
    
    // Tracks the authenticated session user context string
    private static String loggedInUsername = ""; 

    static {
        librarianDatabase.put("admin", "admin123");
        studentDatabase.put("student", "pass123");
    }

    public LoginGUI() {
        // Read accounts list dynamically from file right away on startup
        LibraryStorage.loadUsers(librarianDatabase, studentDatabase);

        setTitle("Smart Library - Authentication Gateway");
        setSize(420, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("Library Authentication Portal", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        formPanel.add(new JLabel("Select Role:"));
        String[] roles = {"Librarian", "Student"};
        roleComboBox = new JComboBox<>(roles);
        formPanel.add(roleComboBox);

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 13));
        loginButton.setBackground(new Color(70, 130, 180)); 
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        
        getRootPane().setDefaultButton(loginButton);

        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 13));
        registerButton.setBackground(new Color(46, 139, 87)); 
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> handleRegistration());
    }

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    // Public getter so LibraryGUI can verify if a student username exists
    public static Map<String, String> getStudentDatabase() {
        return studentDatabase;
    }

    private void handleLogin() {
        String selectedRole = (String) roleComboBox.getSelectedItem();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Login fields cannot be left empty!", "Input Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isAuthenticated = false;

        if ("Librarian".equals(selectedRole) && password.equals(librarianDatabase.get(username))) {
            isAuthenticated = true;
        } else if ("Student".equals(selectedRole) && password.equals(studentDatabase.get(username))) {
            isAuthenticated = true;
        }

        if (isAuthenticated) {
            loggedInUsername = username; 
            
            JOptionPane.showMessageDialog(this, "Access Granted! Welcome back.", "Success", JOptionPane.INFORMATION_MESSAGE);
            this.dispose(); 
            
            // Launch dashboard with specific role restriction parameters applied
            SwingUtilities.invokeLater(() -> {
                LibraryGUI dashboard = new LibraryGUI(selectedRole); 
                dashboard.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password matching that role.", "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegistration() {
        String selectedRole = (String) roleComboBox.getSelectedItem();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both fields to complete registration.", "Registration Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Librarian".equals(selectedRole)) {
            if (librarianDatabase.containsKey(username)) {
                JOptionPane.showMessageDialog(this, "Librarian username already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            } else {
                librarianDatabase.put(username, password);
                LibraryStorage.saveUsers(librarianDatabase, studentDatabase);
                JOptionPane.showMessageDialog(this, "Librarian profile registered successfully!", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                clearInputs();
            }
        } else if ("Student".equals(selectedRole)) {
            if (studentDatabase.containsKey(username)) {
                JOptionPane.showMessageDialog(this, "Student username already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            } else {
                studentDatabase.put(username, password);
                LibraryStorage.saveUsers(librarianDatabase, studentDatabase);
                JOptionPane.showMessageDialog(this, "Student profile registered successfully!", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                clearInputs();
            }
        }
    }

    private void clearInputs() {
        usernameField.setText("");
        passwordField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}