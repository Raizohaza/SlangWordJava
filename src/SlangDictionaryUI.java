import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
public class SlangDictionaryUI {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JTextArea searchResultArea;
    private JTextArea historyArea;
    private JTextField searchField;
    private JTextField addWordField;
    private JTextField addDefinitionField;
    private JButton searchButton;
    private JButton addButton;
    private SlangDictionary slangDictionary;
    private List<String> searchHistory;

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

        JPanel editPanel = createEditPanel();
        tabbedPane.addTab("Edit Word", editPanel);

        JPanel deletePanel = createDeletePanel();
        tabbedPane.addTab("Delete Word", deletePanel);

        JPanel resetPanel = createResetPanel();
        tabbedPane.addTab("Reset", resetPanel);

        JPanel quiz1Panel = createQuiz1Panel();
        tabbedPane.addTab("Quiz 1", quiz1Panel);

        JPanel quiz2Panel = createQuiz2Panel();
        tabbedPane.addTab("Quiz 2", quiz2Panel);

        JPanel randomPanel = createRandomPanel();
        tabbedPane.addTab("Random", randomPanel);

        frame.getContentPane().add(tabbedPane);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        DefaultTableModel tableModel = new DefaultTableModel();
        JTable searchTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(searchTable);

        // Add columns to the table model
        tableModel.addColumn("Slang Word");
        tableModel.addColumn("Definition");

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
                if(searchResults != null)
                    searchResultsBySlangWords.add(searchResults);

                List<SlangWord> searchResultsByDefinition = slangDictionary.searchByDefinition(searchWord);

                // Clear the table model
                tableModel.setRowCount(0);

                // Add search results to the table model
                for (SlangWord slangWord : searchResultsBySlangWords) {
                    tableModel.addRow(new Object[]{slangWord.getWord(), slangWord.getAllMeanings().get(0)});
                }

                for (SlangWord slangWord : searchResultsByDefinition) {
                    tableModel.addRow(new Object[]{slangWord.getWord(), slangWord.getAllMeanings().get(0)});
                }

                // Add the searched word to the search history
                searchHistory.add(searchWord);
            }
        });

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        historyArea = new JTextArea(10, 30);
        historyArea.setEditable(false);

        panel.add(new JScrollPane(historyArea), BorderLayout.CENTER);

        // Add a listener to update the history area when the HistoryPanel is clicked
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Check if the selected tab is the HistoryPanel
                if (tabbedPane.getSelectedComponent() == panel) {
                    // Clear the history area first
                    historyArea.setText("");

                    // Append each search history to the history area
                    for (String history : searchHistory) {
                        historyArea.append(history + "\n");
                    }
                }
            }
        });

        return panel;
    }

    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        addWordField = new JTextField(20);
        addDefinitionField = new JTextField(20);
        addButton = new JButton("Add");

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Word:"));
        inputPanel.add(addWordField);
        inputPanel.add(new JLabel("Definition:"));
        inputPanel.add(addDefinitionField);
        inputPanel.add(addButton);

        panel.add(inputPanel, BorderLayout.NORTH);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = addWordField.getText();
                String definition = addDefinitionField.getText();

                // Add the new slang word to the dictionary
                SlangWord slangWord = new SlangWord(word);
                slangWord.addMeaning(1,definition);
                slangDictionary.addSlangWord(slangWord);

                // Clear the input fields
                addWordField.setText("");
                addDefinitionField.setText("");

                JOptionPane.showMessageDialog(frame, "Slang word added successfully!");
            }
        });

        return panel;
    }

    private JPanel createEditPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Add UI components for editing a slang word

        return panel;
    }

    private JPanel createDeletePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Add UI components for deleting a slang word

        return panel;
    }

    private JPanel createResetPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Reset Panel"));
        // Add UI components and functionality for resetting the dictionary

        return panel;
    }

    private JPanel createQuiz1Panel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Quiz 1 Panel"));
        // Add UI components and functionality for Quiz 1

        return panel;
    }

    private JPanel createQuiz2Panel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Quiz 2 Panel"));
        // Add UI components and functionality for Quiz 2

        return panel;
    }

    private JPanel createRandomPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Random Panel"));
        // Add UI components and functionality for displaying a random slang word

        return panel;
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SlangDictionaryUI ui = new SlangDictionaryUI(new SlangDictionary());
            ui.show();
        });
    }
}
