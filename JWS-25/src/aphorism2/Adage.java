package aphorism2;

public class Adage {


    private String words;
    private transient int wordCount;
    private int id;


    @Override
    public String toString() {
        return String.format("%2d: ", id) + words + " -- " + wordCount
                + " words";
    }

    public void setWords(final String words) {
        this.words = words;
        wordCount = words.trim().split("\\s+").length;
    }

    public String getWords() {
        return words;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}