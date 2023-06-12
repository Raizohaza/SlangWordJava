import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SlangDictionaryUI {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JTextField searchField;
    private JButton searchButton;
    private JButton addButton;
    private SlangDictionary slangDictionary;
    private List<String> searchHistory;
    private DefaultTableModel tableModel;

    public SlangDictionaryUI(SlangDictionary slangDictionary) {
        this.slangDictionary = slangDictionary;
        this.searchHistory = new ArrayList<>();

        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        frame = new JFrame("Slang Dictionary");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create and add the panels for each tab
        JPanel searchPanel = createSearchPanel();
        tabbedPane.addTab("Search", searchPanel);

        JPanel historyPanel = createHistoryPanel();
        tabbedPane.addTab("History", historyPanel);

        JPanel addPanel = createAddPanel();
        tabbedPane.addTab("Add Word", addPanel);

        frame.getContentPane().add(tabbedPane);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        tableModel = new DefaultTableModel();
        JTable searchTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(searchTable);

        // Add columns to the table model
        tableModel.addColumn("Slang Word");
        tableModel.addColumn("Definition");
        tableModel.addColumn(""); // Empty column for delete button

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Search Word:"));
        inputPanel.add(searchField);
        inputPanel.add(searchButton);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchWord = searchField.getText();

                SlangWord searchResults = slangDictionary.searchBySlangWord(searchWord);
                List<SlangWord> searchResultsBySlangWords = new ArrayList<>();
                if (searchResults != null)
                    searchResultsBySlangWords.add(searchResults);

                List<SlangWord> searchResultsByDefinition = slangDictionary.searchByDefinition(searchWord);

                // Clear the table model
                tableModel.setRowCount(0);

                // Add search results to the table model
                for (SlangWord slangWord : searchResultsBySlangWords) {
                    for (String meaning:slangWord.getAllMeanings()) {
                        tableModel.addRow(new Object[]{slangWord.getWord(), meaning, "Delete"});

                    }
                }

                for (SlangWord slangWord : searchResultsByDefinition) {
                    for (String meaning:slangWord.getAllMeanings()) {
                        tableModel.addRow(new Object[]{slangWord.getWord(), meaning, "Delete"});

                    }
                }

                String historyEntry = searchWord + ":" + System.currentTimeMillis();
                searchHistory.add(historyEntry);
            }
        });

        // Add delete button functionality to the table
        searchTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = searchTable.rowAtPoint(e.getPoint());
                int col = searchTable.columnAtPoint(e.getPoint());

                if (col == 2) { // Delete button column
                    String slangWord = (String) tableModel.getValueAt(row, 0);
                    SlangWord word = slangDictionary.searchBySlangWord(slangWord);
                    if (word != null) {
                        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this slang word?",
                                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            slangDictionary.deleteSlangWord(word);
                            tableModel.removeRow(row);
                            JOptionPane.showMessageDialog(null, "Slang word deleted successfully!",
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel historyTableModel = new DefaultTableModel();
        JTable historyTable = new JTable(historyTableModel);
        JScrollPane scrollPane = new JScrollPane(historyTable);

        // Add columns to the table model
        historyTableModel.addColumn("Search Word");
        historyTableModel.addColumn("Timestamp");

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedIndex() == 1) {
                    // Clear the table model first
                    historyTableModel.setRowCount(0);

                    // Add search history entries to the table model
                    for (String historyEntry : searchHistory) {
                        String[] entryParts = historyEntry.split(":");
                        if (entryParts.length >= 2) {
                            String searchWord = entryParts[0];
                            String timestamp = entryParts[1];

                            // Format the timestamp into a readable format
                            long timestampMillis = Long.parseLong(timestamp);
                            Date date = new Date(timestampMillis);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String formattedTimestamp = format.format(date);

                            historyTableModel.addRow(new Object[]{searchWord, formattedTimestamp});
                        }
                    }
                }
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2));
        JLabel wordLabel = new JLabel("Word:");
        JTextField wordField = new JTextField();
        JLabel meaningsLabel = new JLabel("Meanings (separated by '|'):");
        JTextArea meaningsArea = new JTextArea(5, 20);
        JScrollPane meaningsScrollPane = new JScrollPane(meaningsArea);

        formPanel.add(wordLabel);
        formPanel.add(wordField);
        formPanel.add(meaningsLabel);
        formPanel.add(meaningsScrollPane);

        JButton addButton = new JButton("Add Word");

        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(addButton, BorderLayout.SOUTH);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = wordField.getText().trim();
                String meaningsText = meaningsArea.getText().trim();

                if (!word.isEmpty() && !meaningsText.isEmpty()) {
                    String[] meanings = meaningsText.split("\\|");

                    // Check if the slang word already exists
                    SlangWord existingSlangWord = slangDictionary.searchBySlangWord(word);
                    if (existingSlangWord != null) {
                        int choice = JOptionPane.showConfirmDialog(frame, "Slang word already exists. Overwrite?", "Duplicate Slang Word", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                            // Overwrite the existing slang word
                            existingSlangWord = new SlangWord(word);
                            for (int i = 0; i < meanings.length; i++) {
                                existingSlangWord.addMeaning(i + 1, meanings[i].trim());
                            }

                            slangDictionary.addSlangWord(existingSlangWord);

                            JOptionPane.showMessageDialog(frame, "Slang word added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            wordField.setText("");
                            meaningsArea.setText("");
                        } else if (choice == JOptionPane.NO_OPTION) {
                            // Duplicate a new slang word
                            String newWord = word + "_copy";
                            SlangWord newSlangWord = new SlangWord(newWord);
                            for (int i = 0; i < meanings.length; i++) {
                                newSlangWord.addMeaning(i + 1, meanings[i].trim());
                            }
                            System.out.println(newWord);
                            slangDictionary.addSlangWord(newSlangWord);

                            JOptionPane.showMessageDialog(frame, "Slang word duplicated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            wordField.setText("");
                            meaningsArea.setText("");
                        }
                    } else {
                        // Add the new slang word
                        SlangWord slangWord = new SlangWord(word);
                        for (int i = 0; i < meanings.length; i++) {
                            slangWord.addMeaning(i + 1, meanings[i].trim());
                        }
                        slangDictionary.addSlangWord(slangWord);

                        JOptionPane.showMessageDialog(frame, "Slang word added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        wordField.setText("");
                        meaningsArea.setText("");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please fill in both word and meanings!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SlangDictionary slangDictionary = new SlangDictionary();
        SlangDictionaryUI slangDictionaryUI = new SlangDictionaryUI(slangDictionary);
        slangDictionaryUI.show();
    }
}
