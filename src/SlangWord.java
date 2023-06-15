import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlangWord {
    private String word;
    private Map<Integer, String> meanings;

    public SlangWord(String word) {
        this.word = word;
        this.meanings = new HashMap<>();
    }

    public String getWord() {
        return word;
    }

    public void addMeaning(int meaningNumber, String meaning) {
        meanings.put(meaningNumber, meaning);
    }

    public String getMeaning(int meaningNumber) {
        return meanings.get(meaningNumber);
    }

    public List<String> getAllMeanings() {
        return new ArrayList<>(meanings.values());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(word).append("`");
        for (int i = 1; i <= meanings.size(); i++) {
            sb.append(meanings.get(i));
            if (i < meanings.size()) {
                sb.append("| ");
            }
        }
        return sb.toString();
    }
}
