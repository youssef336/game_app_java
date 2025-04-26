package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class GameManagementSystem extends JFrame {
    private final String currentUser;
    private JTextField gameTitleField, genreField, yearField, developerField;
    private JTextArea descriptionArea, resultArea;
    private JTextField searchField;

    public GameManagementSystem(String username) {
        this.currentUser = username;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Game Management System - Welcome " + currentUser);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Add Game", createAddGamePanel());
        tabbedPane.addTab("Search Game", createSearchGamePanel());

        // Add test button
        JButton testButton = new JButton("Test Connection");
        testButton.addActionListener(e -> DCconnection.testConnection());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(testButton);

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createAddGamePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Game Title
        addLabel(panel, gbc, "Game Title:", 0);
        gameTitleField = new JTextField(20);
        addField(panel, gbc, gameTitleField, 1, 0);

        // Genre
        addLabel(panel, gbc, "Genre:", 1);
        genreField = new JTextField(20);
        addField(panel, gbc, genreField, 1, 1);

        // Release Year
        addLabel(panel, gbc, "Release Year:", 2);
        yearField = new JTextField(20);
        addField(panel, gbc, yearField, 1, 2);

        // Developer
        addLabel(panel, gbc, "Developer:", 3);
        developerField = new JTextField(20);
        addField(panel, gbc, developerField, 1, 3);

        // Description
        addLabel(panel, gbc, "Description:", 4);
        descriptionArea = new JTextArea(5, 20);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        addField(panel, gbc, scrollPane, 1, 4);

        // Add Button
        JButton addButton = createButton("Add Game", new Color(0, 120, 215), this::addGame);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        panel.add(addButton, gbc);

        return panel;
    }

    private JPanel createSearchGamePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // Search Panel
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(25);
        JButton searchButton = createButton("Search", new Color(0, 120, 215), this::searchGame);

        searchPanel.add(new JLabel("Game Title:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Results Area
        resultArea = new JTextArea(15, 50);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addGame(ActionEvent e) {
        String title = gameTitleField.getText().trim();
        String genre = genreField.getText().trim();
        String yearText = yearField.getText().trim();
        String developer = developerField.getText().trim();
        String description = descriptionArea.getText().trim();

        // Validation
        if (title.isEmpty() || genre.isEmpty() || yearText.isEmpty()) {
            showError("Title, Genre and Year are required");
            return;
        }

        try {
            int year = Integer.parseInt(yearText);
            try (Connection conn = DCconnection.connect()) {
                // Get user ID
                int userId = getUserId(conn);
                if (userId == -1) {
                    showError("User not found in database");
                    return;
                }

                // Insert game
                String sql = "INSERT INTO games (title, genre, release_year, developer, description, added_by) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, title);
                    pstmt.setString(2, genre);
                    pstmt.setInt(3, year);
                    pstmt.setString(4, developer);
                    pstmt.setString(5, description);
                    pstmt.setInt(6, userId);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Game added successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearAddGameFields();
                    }
                }
            }
        } catch (NumberFormatException ex) {
            showError("Please enter a valid year (numbers only)");
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void searchGame(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            showError("Please enter a search term");
            return;
        }

        try (Connection conn = DCconnection.connect()) {
            String sql = "SELECT * FROM games WHERE title LIKE ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + searchTerm + "%");

                StringBuilder result = new StringBuilder();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        result.append(String.format(
                                "Title: %s\nGenre: %s\nYear: %d\nDeveloper: %s\nDescription: %s\n\n",
                                rs.getString("title"),
                                rs.getString("genre"),
                                rs.getInt("release_year"),
                                rs.getString("developer"),
                                rs.getString("description")
                        ));
                    }
                }

                if (result.length() == 0) {
                    result.append("No games found matching: ").append(searchTerm);
                }

                resultArea.setText(result.toString());
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private int getUserId(Connection conn) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUser);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("user_id") : -1;
            }
        }
    }

    private void clearAddGameFields() {
        gameTitleField.setText("");
        genreField.setText("");
        yearField.setText("");
        developerField.setText("");
        descriptionArea.setText("");
    }

    // Helper methods
    private void addLabel(JPanel panel, GridBagConstraints gbc, String text, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(text), gbc);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, JComponent component, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
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