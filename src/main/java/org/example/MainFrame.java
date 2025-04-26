package org.example;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final LoginPanel loginPanel;
    private final RegisterPanel registerPanel;

    public MainFrame() {
        setTitle("Game Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        // Initialize panels
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);

        // Add to card panel
        cardPanel.add(loginPanel, "login");
        cardPanel.add(registerPanel, "register");

        add(cardPanel);
        showLoginPanel();
    }

    public void showLoginPanel() {
        cardLayout.show(cardPanel, "login");
    }

    public void showRegisterPanel() {
        cardLayout.show(cardPanel, "register");
    }

    public void showGameManagementSystem(String username) {
        SwingUtilities.invokeLater(() -> {
            GameManagementSystem system = new GameManagementSystem(username);
            system.setVisible(true);
            this.dispose();
        });
    }
}