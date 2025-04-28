package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPanel extends JPanel {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final MainFrame mainFrame;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        Helper.addLabel(this,"Username:", gbc, 0);
        usernameField = new JTextField(20);
        Helper.addField(this,usernameField, gbc, 1, 0);

        // Password
        Helper.addLabel(this,"Password:", gbc, 1);
        passwordField = new JPasswordField(20);
        Helper.addField(this,passwordField, gbc, 1, 1);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton loginButton = Helper.createButton("Login", new Color(0, 120, 215), this::handleLogin);
        JButton registerButton = Helper.createButton("Register", new Color(100, 100, 100),
                e -> mainFrame.showRegisterPanel());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            Helper.showError(this,"Please enter both username and password");
            return;
        }

        try (Connection conn = DCconnection.connect()) {
            String sql = "SELECT user_id FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        mainFrame.showGameManagementSystem(username);
                    } else {
                        Helper.showError(this,"Invalid username or password");
                    }
                }
            }
        } catch (SQLException ex) {
           Helper.showError(this,"Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Helper methods

}