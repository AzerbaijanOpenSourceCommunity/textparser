package com.owary;

import java.io.Serializable;
import java.util.Objects;

public class WordPair implements Comparable<WordPair>, Serializable {

    private String text;
    private String next;
    private int occurred = 1;
    private int pairOccurred = 1;

    public WordPair(String text, String next) {
        this.text = text;
        this.next = next;
    }

    public void wordOccurred(int i){
        this.occurred = i;
    }

    public void pairOccurred(){
        pairOccurred++;
    }

    public String getText() {
        return text;
    }

    public String getNext() {
        return next;
    }

    public int getOccurred() {
        return occurred;
    }

    public int getPairOccurred() {
        return pairOccurred;
    }

    @Override
    public int compareTo(WordPair o) {
        return Integer.compare(o.getPairOccurred(), this.getPairOccurred());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WordPair)) return false;
        WordPair word = (WordPair) o;
        return text.equals(word.text) &&
                next.equals(word.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, next);
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %d", text, next, pairOccurred);
    }
}
