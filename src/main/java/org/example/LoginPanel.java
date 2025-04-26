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
        addLabel("Username:", gbc, 0);
        usernameField = new JTextField(20);
        addField(usernameField, gbc, 1, 0);

        // Password
        addLabel("Password:", gbc, 1);
        passwordField = new JPasswordField(20);
        addField(passwordField, gbc, 1, 1);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton loginButton = createButton("Login", new Color(0, 120, 215), this::handleLogin);
        JButton registerButton = createButton("Register", new Color(100, 100, 100),
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
            showError("Please enter both username and password");
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
                        showError("Invalid username or password");
                    }
                }
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Helper methods
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