package com.wx.invoicefx.util.string;

import com.wx.util.pair.Pair;

import java.util.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 10.06.17.
 */
public class SentenceItemsParser {

    public static final String[] STOP_WORD = {", ", " und ", " and ", " et "};

    public static Pair<List<String>, List<String>> splitStopWords(List<String> parsedSentence) {
        if ((parsedSentence.size() & 1) != 1) {
            throw new IllegalArgumentException("Parsed sentence should have an odd number of words");
        }

        List<String> words = new ArrayList<>((int) Math.ceil(parsedSentence.size() / 2));
        List<String> stopWords = new ArrayList<>((int) Math.floor(parsedSentence.size() / 2));

        boolean addToWords = true;
        for (String w : parsedSentence) {
            if (addToWords) {
                words.add(w);
            } else {
                stopWords.add(w);
            }

            addToWords = !addToWords;
        }

        return Pair.of(words, stopWords);
    }

    public static String rebuildSentence(List<String> words, List<String> stopWords) {
        if (words.isEmpty()) {
            return "";
        }
        if (words.size() != stopWords.size() + 1) {
            throw new IllegalArgumentException("Invalid number of stop words");
        }

        StringBuilder result = new StringBuilder();

        Iterator<String> wordsIt = words.iterator();
        Iterator<String> stopWordsIt = stopWords.iterator();

        while (wordsIt.hasNext()) {
            result.append(wordsIt.next());

            if (stopWordsIt.hasNext()) {
                result.append(stopWordsIt.next());
            }
        }

        return result.toString();
    }

    public static List<String> parseItems(String sentence) {
        return parse(sentence, 0);
    }

    private static List<String> parse(String text, int stopWordIndex) {
        if (text.isEmpty()) {
            return Collections.singletonList("");
        }

        if (stopWordIndex >= STOP_WORD.length) {
            return Collections.singletonList(text);
        }

        List<String> result = new LinkedList<>();
        String word = STOP_WORD[stopWordIndex];

        String[] split = text.split(word, -1);

        for (int i = 0; i < split.length; i++) {
            result.addAll(parse(split[i], stopWordIndex + 1));

            if (i != split.length - 1) {
                result.add(word);
            }
        }

        return result;
    }

    private SentenceItemsParser() {
    }
}