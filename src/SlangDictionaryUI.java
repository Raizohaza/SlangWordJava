//import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class SlangDictionaryUI {
    private static final String SLANG_FILE_PATH = "slang.txt";
    private String HISTORY_FILE_PATH = "history.txt";
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JTextField searchField;
    private SlangDictionary slangDictionary;
    private List<String> searchHistory;
    private DefaultTableModel tableModel;
    JButton searchButton;

    public SlangDictionaryUI(SlangDictionary slangDictionary) {
        this.slangDictionary = slangDictionary;
        this.searchHistory = new ArrayList<>();
        loadSearchHistoryFromFile();

//        try {
//            UIManager.setLookAndFeel(new FlatMacDarkLaf());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

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

        JPanel randomPanel = createRandomPanel();
        tabbedPane.addTab("Random Word", randomPanel);

        JPanel quizPanel = createQuizPanel();
        tabbedPane.addTab("Quiz Slang", quizPanel);

        JPanel quizMeaningPanel = createQuizMeaningPanel();
        tabbedPane.addTab("Quiz Meaning", quizMeaningPanel);

        frame.getContentPane().add(tabbedPane);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(Color.BLUE);
        JButton resetButton = new JButton("Reset");
        resetButton.setBackground(Color.RED);
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Disable editing for the Slang Word column (column index 0) and the Delete column (column index 2)
                return column != 0 && column != 2;
            }
        };
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
        inputPanel.add(resetButton);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        searchButton.doClick();
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to reset the dictionary to default?",
                        "Reset Dictionary",
                        JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    slangDictionary.resetToDefault("slang_default.txt");
                    JOptionPane.showConfirmDialog(frame,
                            "Dictionary reset to default.",
                            "Notification",
                            JOptionPane.CLOSED_OPTION);
                }
            }
        });

        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchButton.doClick(); // Simulate click on searchButton
            }
        });
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchWord = searchField.getText().trim();

                SlangWord searchResults = slangDictionary.searchBySlangWord(searchWord);
                List<SlangWord> searchResultsBySlangWords = new ArrayList<>();
                if (searchResults != null)
                    searchResultsBySlangWords.add(searchResults);

                List<SlangWord> searchResultsByDefinition = slangDictionary.searchByDefinition(searchWord);

                // Clear the table model
                tableModel.setRowCount(0);

                // Add search results to the table model
                for (SlangWord slangWord : searchResultsBySlangWords) {
                    for (String meaning : slangWord.getAllMeanings()) {
                        tableModel.addRow(new Object[]{slangWord.getWord(), meaning, "Delete"});

                    }
                }

                for (SlangWord slangWord : searchResultsByDefinition) {
                    for (String meaning : slangWord.getAllMeanings()) {
                        tableModel.addRow(new Object[]{slangWord.getWord(), meaning, "Delete"});

                    }
                }
// Check if the search word contains spaces or non-visible characters using regex
                System.out.println("("+  searchWord.getClass().getName() + ")");
                if (!searchWord.isEmpty()) {
                    String historyEntry = searchWord + ":" + System.currentTimeMillis();
                    searchHistory.add(historyEntry);
                    saveSearchHistoryToFile();
                }

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
                            slangDictionary.saveToFile();
                            tableModel.removeRow(row);
                            JOptionPane.showMessageDialog(null, "Slang word deleted successfully!",
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        // Add TableModelListener to detect changes in the table model
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 1) {
                    String slangWord = (String) tableModel.getValueAt(row, 0);
                    List<String> meanings = new ArrayList<>();
                    meanings.add((String) tableModel.getValueAt(row, 1));
                    slangDictionary.updateSlangWord(slangDictionary.searchBySlangWord(slangWord), slangWord, meanings);
                    slangDictionary.saveToFile();
                }
            }
        });
        searchButton.doClick();
        return panel;
    }

    private void saveSearchHistoryToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE_PATH))) {
            for (String entry : searchHistory) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving search history to file: " + e.getMessage());
        }
    }

    private void loadSearchHistoryFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                searchHistory.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error loading search history from file: " + e.getMessage());
        }
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
                    // Load search history from file
                    loadSearchHistoryFromFile(historyTableModel);
                }
            }
        });


        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadSearchHistoryFromFile(DefaultTableModel historyTableModel) {
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] entryParts = line.split(":");
                if (entryParts.length >= 2) {
                    String searchWord = entryParts[0];
                    String timestamp = entryParts[1];

                    // Format the timestamp into a readable format
                    long timestampMillis = Long.parseLong(timestamp);
                    java.util.Date date = new java.util.Date(timestampMillis);
                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedTimestamp = format.format(date);

                    historyTableModel.addRow(new Object[]{searchWord, formattedTimestamp});
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading search history from file: " + e.getMessage());
        }
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
                    slangDictionary.saveToFile();
                } else {
                    JOptionPane.showMessageDialog(frame, "Please fill in both word and meanings!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }


    private JPanel createRandomPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel randomSlangWordLabel = new JLabel();
        randomSlangWordLabel.setFont(randomSlangWordLabel.getFont().deriveFont(18f));
        randomSlangWordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(randomSlangWordLabel, BorderLayout.CENTER);

        JButton generateButton = new JButton("Generate Random Slang Word");
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateRandomSlangWord(randomSlangWordLabel);
            }
        });
        panel.add(generateButton, BorderLayout.SOUTH);

        return panel;
    }

    public JPanel createQuizPanel() {
        JPanel masterPanel = new JPanel(new GridLayout(2, 1));
        masterPanel.setPreferredSize(new Dimension(400, 200));
        JPanel quizPanel = new JPanel(new GridLayout(2, 2));
        List<SlangWord> randomSlangWords = slangDictionary.getRandomSlangWords(4);
        SlangWord correctSlangWord = randomSlangWords.get(0);

        JButton[] answerButtons = new JButton[4];

        for (int i = 0; i < 4; i++) {
            JButton button = new JButton();
            button.setText(randomSlangWords.get(i).getAllMeanings().get(0));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton clickedButton = (JButton) e.getSource();
                    System.out.println(clickedButton.getText());
                    if (clickedButton.getText().equals(correctSlangWord.getMeaning(1))) {
                        JOptionPane.showMessageDialog(null, "Correct!");
                        // Notify the tabbedPane to create a new quizPanel
                        int index = tabbedPane.indexOfTab("Quiz Slang");
                        if (index != -1) {
                            tabbedPane.remove(index);
                            JPanel quizPanel = createQuizPanel();
                            tabbedPane.addTab("Quiz Slang", quizPanel);
                            tabbedPane.setSelectedIndex(index);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Incorrect! Try again.");
                    }
                }
            });
            answerButtons[i] = button;
        }

        // Randomize the answer buttons' positions
        for (int i = 3; i > 0; i--) {
            int randIndex = (int) (Math.random() * (i + 1));
            JButton temp = answerButtons[i];
            answerButtons[i] = answerButtons[randIndex];
            answerButtons[randIndex] = temp;
        }

        JLabel wordLabel = new JLabel("Guess the meaning of: " + correctSlangWord.getWord());
        Font labelFont = wordLabel.getFont();
        wordLabel.setFont(labelFont.deriveFont(labelFont.getSize() + 16f));

        masterPanel.add(wordLabel);
        // Add the answer buttons to the quiz panel
        for (JButton button : answerButtons) {
            quizPanel.add(button);
        }

        masterPanel.add(quizPanel);
        return masterPanel;
    }

    public JPanel createQuizMeaningPanel() {
        JPanel masterPanel = new JPanel(new GridLayout(2, 1));
        masterPanel.setPreferredSize(new Dimension(400, 200));
        JPanel quizPanel = new JPanel(new GridLayout(2, 2));
        List<SlangWord> randomSlangWords = slangDictionary.getRandomSlangWords(4);
        SlangWord correctSlangWord = randomSlangWords.get(0);

        JButton[] answerButtons = new JButton[4];

        for (int i = 0; i < 4; i++) {
            JButton button = new JButton();
            button.setText(randomSlangWords.get(i).getWord());
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton clickedButton = (JButton) e.getSource();
                    System.out.println(clickedButton.getText());
                    if (clickedButton.getText().equals(correctSlangWord.getWord())) {
                        JOptionPane.showMessageDialog(null, "Correct!");
                        // Notify the tabbedPane to create a new quizMeaningPanel
                        int index = tabbedPane.indexOfTab("Quiz Meaning");
                        if (index != -1) {
                            tabbedPane.remove(index);
                            JPanel quizMeaningPanel = createQuizMeaningPanel();
                            tabbedPane.addTab("Quiz Meaning", quizMeaningPanel);
                            tabbedPane.setSelectedIndex(index);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Incorrect! Try again.");
                    }
                }
            });
            answerButtons[i] = button;
        }

        // Randomize the answer buttons' positions
        for (int i = 3; i > 0; i--) {
            int randIndex = (int) (Math.random() * (i + 1));
            JButton temp = answerButtons[i];
            answerButtons[i] = answerButtons[randIndex];
            answerButtons[randIndex] = temp;
        }

        JLabel meaningLabel = new JLabel("Guess the slang word for: " + correctSlangWord.getMeaning(1));
        Font labelFont = meaningLabel.getFont();
        meaningLabel.setFont(labelFont.deriveFont(labelFont.getSize() + 16f));

        masterPanel.add(meaningLabel);
        // Add the answer buttons to the quiz panel
        for (JButton button : answerButtons) {
            quizPanel.add(button);
        }

        masterPanel.add(quizPanel);
        return masterPanel;
    }

    private void generateRandomSlangWord(JLabel slangWordLabel) {
        SlangWord randomSlangWord = slangDictionary.getRandomSlangWord();
        if (randomSlangWord != null) {
            slangWordLabel.setText(randomSlangWord.getWord() + randomSlangWord.getAllMeanings());
        } else {
            slangWordLabel.setText("No slang words available.");
        }
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SlangDictionary slangDictionary = new SlangDictionary(SLANG_FILE_PATH);
        slangDictionary.loadDataFromFile();
        SlangDictionaryUI slangDictionaryUI = new SlangDictionaryUI(slangDictionary);
        slangDictionaryUI.show();
    }
}
