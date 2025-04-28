package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import static org.example.Helper.*;

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
        Helper.addLabel(this,"Username:", gbc, 0);
        usernameField = new JTextField(20);
        Helper.addField (this, usernameField, gbc, 1, 0);

        // Email
        Helper.addLabel(this,"Email:", gbc, 1);
        emailField = new JTextField(20);
        Helper.addField(this, emailField, gbc, 1, 1);

        // Password
        Helper.addLabel(this,"Password:", gbc, 2);
        passwordField = new JPasswordField(20);
        Helper.addField(this, passwordField, gbc, 1, 2);

        // Confirm Password
        Helper.addLabel(this,"Confirm Password:", gbc, 3);
        confirmPasswordField = new JPasswordField(20);
        Helper.addField(this, confirmPasswordField, gbc, 1, 3);

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
            Helper.showError(this,"Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            Helper.showError(this,"Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            Helper.showError(this,"Password must be at least 6 characters");
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
                        Helper.showError(this,conflict + " already exists");
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
            Helper.showError(this,"Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


}