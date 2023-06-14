import javax.swing.*;
import java.util.*;

public class SlangDictionaryApp {
    private static final String SLANG_FILE_PATH = "slang.txt";

    private SlangDictionary slangDictionary;
    private Set<String> searchHistory;
    private Random random;

    public SlangDictionaryApp() {
        slangDictionary = new SlangDictionary(SLANG_FILE_PATH);
        searchHistory = new LinkedHashSet<>();
        random = new Random();
    }

    public void run() {
        loadSlangWords();

        Scanner scanner = new Scanner(System.in);

        int choice = 0;

        while (choice != 11) {
            displayMenu();
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    searchBySlangWord(scanner);
                    break;
                case 2:
                    searchByDefinition(scanner);
                    break;
                case 3:
                    displaySearchHistory();
                    break;
                case 4:
                    addSlangWord(scanner);
                    break;
                case 5:
                    editSlangWord(scanner);
                    break;
                case 6:
                    deleteSlangWord(scanner);
                    break;
                case 7:
                    resetSlangWords();
                    break;
                case 8:
                    displayRandomSlangWord();
                    break;
                case 9:
                    playGuessSlangWord(scanner);
                    break;
                case 10:
                    playGuessDefinition(scanner);
                    break;
                case 11:
                    System.out.println("Thank you for using the Slang Dictionary!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }

    private void loadSlangWords() {
        System.out.println("Loading slang words from file...");
        slangDictionary.loadDataFromFile();
        System.out.println("Slang words loaded successfully!");
    }

    private void searchBySlangWord(Scanner scanner) {
        System.out.print("Enter the slang word to search for: ");
        String slangWord = scanner.nextLine();
        SlangWord result = slangDictionary.searchBySlangWord(slangWord);
        if (result != null) {
            System.out.println("Meanings for '" + result.getWord() + "':");
            List<String> meanings = result.getAllMeanings();
            for (String meaning : meanings) {
                System.out.println("- " + meaning);
            }
            addToSearchHistory(result.getWord());
        } else {
            System.out.println("Slang word not found.");
        }
    }

    private void searchByDefinition(Scanner scanner) {
        System.out.print("Enter the keyword to search for in definitions: ");
        String keyword = scanner.nextLine();
        List<SlangWord> results = slangDictionary.searchByDefinition(keyword);
        if (!results.isEmpty()) {
            System.out.println("Slang words with definitions containing '" + keyword + "':");
            for (SlangWord result : results) {
                System.out.println("- " + result.getWord());
            }
            addToSearchHistory(results.get(0).getWord());
        } else {
            System.out.println("No slang words found with definitions containing '" + keyword + "'.");
        }
    }

    private void displaySearchHistory() {
        if (!searchHistory.isEmpty()) {
            System.out.println("Search History:");
            for (String slangWord : searchHistory) {
                System.out.println("- " + slangWord);
            }
        } else {
            System.out.println("Search History is empty.");
        }
    }

    private void addSlangWord(Scanner scanner) {
        System.out.print("Enter the new slang word: ");
        String slangWord = scanner.nextLine();
        SlangWord existingSlangWord = slangDictionary.searchBySlangWord(slangWord);
        if (existingSlangWord != null) {
            System.out.println("Slang word already exists. Do you want to overwrite or duplicate it?");
            System.out.print("Enter 'overwrite' to replace the existing slang word, or 'duplicate' to create a new one: ");
            String choice = scanner.nextLine();
            if (choice.equalsIgnoreCase("overwrite")) {
                slangDictionary.deleteSlangWord(existingSlangWord);
            } else if (choice.equalsIgnoreCase("duplicate")) {
                slangWord = generateUniqueSlangWord(slangWord);
            } else {
                System.out.println("Invalid choice. New slang word not added.");
                return;
            }
        }

        System.out.print("Enter the meaning for the slang word: ");
        String meaning = scanner.nextLine();

        SlangWord newSlangWord = new SlangWord(slangWord);

        newSlangWord.addMeaning(1,meaning);
        slangDictionary.addSlangWord(newSlangWord);
        System.out.println("New slang word added successfully!");
    }

    private void editSlangWord(Scanner scanner) {
        System.out.print("Enter the slang word to edit: ");
        String slangWord = scanner.nextLine();
        SlangWord existingSlangWord = slangDictionary.searchBySlangWord(slangWord);
        if (existingSlangWord != null) {
            System.out.println("Meanings for '" + existingSlangWord.getWord() + "':");
            List<String> meanings = existingSlangWord.getAllMeanings();
            for (int i = 0; i < meanings.size(); i++) {
                System.out.println((i + 1) + ". " + meanings.get(i));
            }

            System.out.print("Enter the index of the meaning to edit: ");
            int meaningIndex = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            if (meaningIndex >= 1 && meaningIndex <= meanings.size()) {
                System.out.print("Enter the new meaning: ");
                String newMeaning = scanner.nextLine();
                existingSlangWord.getAllMeanings().set(meaningIndex - 1, newMeaning);
                System.out.println("Slang word edited successfully!");
            } else {
                System.out.println("Invalid meaning index. Slang word not edited.");
            }
        } else {
            System.out.println("Slang word not found. Slang word not edited.");
        }
    }

    private void deleteSlangWord(Scanner scanner) {
        System.out.print("Enter the slang word to delete: ");
        String slangWord = scanner.nextLine();
        SlangWord existingSlangWord = slangDictionary.searchBySlangWord(slangWord);
        if (existingSlangWord != null) {
            slangDictionary.deleteSlangWord(existingSlangWord);
            System.out.println("Slang word deleted successfully!");
        } else {
            System.out.println("Slang word not found. Slang word not deleted.");
        }
    }

    private void resetSlangWords() {
        slangDictionary.resetSlangWords();
        System.out.println("Slang words reset successfully!");
    }

    private void displayRandomSlangWord() {
        SlangWord randomSlangWord = slangDictionary.getRandomSlangWord();
        if (randomSlangWord != null) {
            System.out.println("Random Slang Word:");
            System.out.println(randomSlangWord);
        } else {
            System.out.println("No slang words available. Add slang words to use this feature.");
        }
    }

    private void playGuessSlangWord(Scanner scanner) {
        SlangWord randomSlangWord = slangDictionary.getRandomSlangWord();
        if (randomSlangWord != null) {
            String correctAnswer = randomSlangWord.getWord();
            List<String> meanings = randomSlangWord.getAllMeanings();
            Collections.shuffle(meanings);

            System.out.println("Guess the Slang Word!");
            System.out.println("Meanings:");

            for (int i = 0; i < meanings.size(); i++) {
                System.out.println((i + 1) + ". " + meanings.get(i));
            }

            System.out.print("Enter the correct meaning number: ");
            int answerIndex = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            if (answerIndex >= 1 && answerIndex <= meanings.size()) {
                String userAnswer = meanings.get(answerIndex - 1);
                if (userAnswer.equalsIgnoreCase(correctAnswer)) {
                    System.out.println("Congratulations! You guessed it right.");
                } else {
                    System.out.println("Wrong answer. The correct slang word is: " + correctAnswer);
                }
            } else {
                System.out.println("Invalid answer. The correct slang word is: " + correctAnswer);
            }
        } else {
            System.out.println("No slang words available. Add slang words to use this feature.");
        }
    }

    private void playGuessDefinition(Scanner scanner) {
        SlangWord randomSlangWord = slangDictionary.getRandomSlangWord();
        if (randomSlangWord != null) {
            String correctDefinition = randomSlangWord.getAllMeanings().get(0);
            List<SlangWord> otherSlangWords = slangDictionary.getRandomSlangWords(3);
            List<String> options = new ArrayList<>();
            options.add(correctDefinition);

            for (SlangWord slangWord : otherSlangWords) {
                options.add(slangWord.getAllMeanings().get(0));
            }

            Collections.shuffle(options);

            System.out.println("Guess the Definition!");
            System.out.println("Definition: " + correctDefinition);
            System.out.println("Options:");

            for (int i = 0; i < options.size(); i++) {
                System.out.println((i + 1) + ". " + options.get(i));
            }

            System.out.print("Enter the correct option number: ");
            int answerIndex = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            if (answerIndex >= 1 && answerIndex <= options.size()) {
                String userAnswer = options.get(answerIndex - 1);
                if (userAnswer.equalsIgnoreCase(correctDefinition)) {
                    System.out.println("Congratulations! You guessed it right.");
                } else {
                    System.out.println("Wrong answer. The correct definition is: " + correctDefinition);
                }
            } else {
                System.out.println("Invalid answer. The correct definition is: " + correctDefinition);
            }
        } else {
            System.out.println("No slang words available. Add slang words to use this feature.");
        }
    }

    private void addToSearchHistory(String slangWord) {
        searchHistory.add(slangWord);
    }

    private String generateUniqueSlangWord(String slangWord) {
        String uniqueSlangWord = slangWord;
        int count = 1;

        while (slangDictionary.searchBySlangWord(uniqueSlangWord) != null) {
            uniqueSlangWord = slangWord + "(" + count + ")";
            count++;
        }

        return uniqueSlangWord;
    }

    private static void displayMenu() {
        System.out.println("\n==== Slang Dictionary ====");
        System.out.println("1. Search by Slang Word");
        System.out.println("2. Search by Definition");
        System.out.println("3. Display Search History");
        System.out.println("4. Add Slang Word");
        System.out.println("5. Edit Slang Word");
        System.out.println("6. Delete Slang Word");
        System.out.println("7. Reset Slang Words");
        System.out.println("8. Display Random Slang Word");
        System.out.println("9. Play Guess the Slang Word");
        System.out.println("10. Play Guess the Definition");
        System.out.println("11. Exit");
    }

//    public static void main(String[] args) {
//        SlangDictionaryApp dictionaryApp = new SlangDictionaryApp();
//        dictionaryApp.run();
//    }

    public static void main(String[] args) {
        // Load slang data from a file into the SlangDictionary instance
        SlangDictionary slangDictionary = new SlangDictionary(SLANG_FILE_PATH);
        System.out.println("Loading slang words from file...");
        slangDictionary.loadDataFromFile();

        // Create and show the SlangDictionaryUI
        SwingUtilities.invokeLater(() -> {
            SlangDictionaryUI ui = new SlangDictionaryUI(slangDictionary);
            ui.show();
        });
    }
}
