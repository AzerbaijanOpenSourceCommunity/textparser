package com.owary;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class TextParser {

    public static List<WordPair> parse(String filename) throws IOException {
        String string = getString(filename);
        String clean = clean(string);
        List<String> sentences = getSentences(clean);
        List<WordPair> relations = getRelations(sentences);
        relations.sort(WordPair::compareTo);
        relations.stream().limit(25).forEach(e -> System.out.println(e.getWord() + " -> " + e.getNext() + " => "+e.getPairOccurred()+" === "+e.getOccurred()));
        serialize(relations, filename);
        return relations;
    }

    private static List<String> getSentences(String string){
        String[] split = string.split("\\.");
        return Arrays.asList(split);
    }

    private static String getString(String filename) throws IOException {
        InputStream inputStream = TextParser.class.getClassLoader().getResourceAsStream(filename);
        if (inputStream==null) throw new NullPointerException("InputStream is null");
//        final int bufferSize = 1024;
//        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//        for (; ; ) {
//            int rsz = in.read(buffer, 0, buffer.length);
//            if (rsz < 0)
//                break;
//            out.append(buffer, 0, rsz);
//        }
        String line;
        try (BufferedReader bufferedReader = new BufferedReader(in)) {
            while ((line = bufferedReader.readLine()) != null) {
                out.append(line);
            }
        }
        return out.toString();
    }

    private static String clean(String input){
        String string;
        string  = input.toLowerCase();
        string  = string.replace(",", "");
        string  = string.replace("%", "");
        string  = string.replaceAll("[0-9]+", "");
        string  = string.replaceAll("-[a-zA-Zəıöü]+", "");
        string  = string.replaceAll("\\s+[a-zA-Z\\.]+\\.(az|com|org|io|ru|info|biz)", "");
        string  = string.replace("(", "");
        string  = string.replace(")", "");
        string  = string.replaceAll("\"([^\"]*)\"", "");
        string  = string.replaceAll("“([^”]*)”", "");
        string  = string.replaceAll("«([^»]*)»", "");
        string  = string.replaceAll("[\"”“]", "");
        string  = string.replaceAll("\\s+", " ");
        string  = string.replaceAll("[!?]", ".");
        return string;
    }

    private static List<String> getWords(String input){
        String sentence = input.replaceAll("^[a-zA-ZəğüöƏĞÖÜıIİ]", "");
        String[] split = sentence.split("\\s+");
        List<String> words = Arrays.asList(split);
        return words
                .stream()
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .filter(word -> word.length() > 1)
                .collect(toList());
    }

    private static List<WordPair> getRelations(List<String> sentences){
        List<WordPair> wordsList = new ArrayList<>();
        Map<WordPair, WordPair> lookupForPairs = new HashMap<>();
        Map<String, Integer> lookupForWords = new HashMap<>();
        for (String sentence : sentences) {
            List<String> words = getWords(sentence);
            for (int i = 0; i < words.size()-1; i++) {
                String theWord = words.get(i);
                String theNext = words.get(i+1);
                WordPair word = new WordPair(theWord, theNext);
                addToList(wordsList, word, lookupForPairs, lookupForWords);
            }
        }
        for (WordPair word : wordsList) {
            String text = word.getWord().getText();
            Integer i = lookupForWords.get(text);
            word.wordOccurred(i);
        }
        return wordsList;
    }

    private static void addToList(List<WordPair> list, WordPair word, Map<WordPair, WordPair> lookupForPairs, Map<String, Integer> lookupForWords){
        lookupForWords.computeIfPresent(word.getWord().getText(), (k, v) -> ++v);
        lookupForWords.putIfAbsent(word.getWord().getText(), 1);

        boolean pairContained = lookupForPairs.containsKey(word);
        if (!pairContained) {
            list.add(word);
            lookupForPairs.put(word, word);
            return;
        }
        lookupForPairs.get(word).pairOccurred();
    }

    private static void serialize(List<WordPair> words, String filename){
        try {
            String outName = String.format("%s_%d", filename, System.currentTimeMillis());
            FileOutputStream out = new FileOutputStream("src/main/resources/dumps/"+outName);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(words);
            oos.flush();
        } catch (Exception e) {
            System.out.println("Problem serializing: " + e);
        }
    }

    public static List<WordPair> deserialize(String filename){
        try {
            String inName = getInName(filename);
            FileInputStream in = new FileInputStream(inName);
            ObjectInputStream ois = new ObjectInputStream(in);
            return (List<WordPair>) (ois.readObject());
        } catch (Exception e) {
            System.out.println("Problem serializing: " + e);
        }
        return null;
    }

    private static String getInName(String filename){
        String folder = "dumps";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(folder);
        String path = url.getPath();
        List<File> files = Arrays.asList(new File(path).listFiles());
        Long aLong = files.stream()
                .map(File::getName)
                .filter(e -> e.contains(filename))
                .map(TextParser::parseLong)
                .map(Number::longValue)
                .max(Long::compareTo)
                .get();
        return String.format("src/main/resources/dumps/%s_%d", filename, aLong);
    }

    private static Long parseLong(String string){
        String[] split = string.split("_");
        String s = split[1];
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            return -1L;
        }
    }

    public static List<WordPair> merge(List<WordPair>...wordLists){
        List<WordPair> finalList = new ArrayList<>();
        Map<WordPair, WordPair> lookup = new HashMap<>();
        Map<String, Integer> lookupForWords = new HashMap<>();
        for (List<WordPair> words : wordLists) {
            for (WordPair word : words) {
                addToList(finalList, word, lookup, lookupForWords);
            }
        }
        return finalList;
    }
}
