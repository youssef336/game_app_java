package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegisterPanel extends JPanel {
    private final JTextField usernameField;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;
    private final MainFrame mainFrame;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        addLabel("Username:", gbc, 0);
        usernameField = new JTextField(20);
        addField(usernameField, gbc, 1, 0);

        // Email
        addLabel("Email:", gbc, 1);
        emailField = new JTextField(20);
        addField(emailField, gbc, 1, 1);

        // Password
        addLabel("Password:", gbc, 2);
        passwordField = new JPasswordField(20);
        addField(passwordField, gbc, 1, 2);

        // Confirm Password
        addLabel("Confirm Password:", gbc, 3);
        confirmPasswordField = new JPasswordField(20);
        addField(confirmPasswordField, gbc, 1, 3);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton registerButton = createButton("Register", new Color(0, 120, 215), this::handleRegistration);
        JButton backButton = createButton("Back", new Color(100, 100, 100), e -> mainFrame.showLoginPanel());

        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);
    }

    private void handleRegistration(ActionEvent e) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        try (Connection conn = DCconnection.connect()) {
            // Check if username/email exists
            String checkSql = "SELECT username, email FROM users WHERE username = ? OR email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                checkStmt.setString(2, email);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String conflict = rs.getString("username").equals(username) ?
                                "Username" : "Email";
                        showError(conflict + " already exists");
                        return;
                    }
                }
            }

            // Insert new user
            String insertSql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, email);
                insertStmt.setString(3, password);

                int affectedRows = insertStmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Registration successful!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    mainFrame.showLoginPanel();
                }
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Helper methods (same as LoginPanel)
    private void addLabel(String text, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel(text), gbc);
    }

    private void addField(JComponent component, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(component, gbc);
    }

    private JButton createButton(String text, Color bgColor, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 30));
        button.addActionListener(listener);
        return button;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}