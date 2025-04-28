package org.example;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Test database connection first


            // Launch application
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}