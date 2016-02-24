package adages;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "adage")
public class Adage {

    protected transient String words;
    protected transient int wordCount;


    // properties
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


    @Override
    public String toString() {
        return words + " -- " + wordCount + " words";
    }

}
