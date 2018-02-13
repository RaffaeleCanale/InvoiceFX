package com.wx.invoicefx.util.string;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class that allows to extract and replace keywords in a given text.
 * <p>
 * Keywords are defined with the following format: ${key_word_name}
 * <p>
 * Created on 11/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class KeyWordHelper {

    /**
     * List all the keywords from the given text.
     * <p>
     * A keyword is defined with the following format: ${key_word_name}
     *
     * @param text Text to analyze
     *
     * @return List of all keywords present in this text
     */
    public static List<String> keyWordsIn(String text) {
        int i = 0;

        List<String> keyWords = new LinkedList<>();
        while ((i = text.indexOf("${", i)) >= 0) {
            int start = i + 2;
            int end = text.indexOf('}', start);
            if (end < 0)
                throw new IllegalArgumentException("Enclosing brace '}' not found for keyword starting at: " + start);

            keyWords.add(text.substring(start, end));
            i = end;
        }

        return keyWords;
    }

    /**
     * Replaces all occurrences of a keyword with some value in some target text.
     *
     * @param target      Text to process
     * @param keyWordName Name of the keyword to replace
     * @param value       Value to replace the keywords with
     *
     * @return The text with all keywords replaced with the corresponding value
     */
    public static String replace(String target, String keyWordName, String value) {
        return target.replace(keyPattern(keyWordName), value);
    }

    /**
     * Build a keyword out of the given name.
     *
     * @param keyWordName Keyword name
     *
     * @return A string representing a keyword whose name is the given key
     */
    public static String keyPattern(String keyWordName) {
        return "${" + keyWordName + "}";
    }

    /**
     * Get the absolute path of the given directory, ensuring it ends with a file separator
     *
     * @param directory Directory whose path to get
     *
     * @return The absolute path for this directory
     */
    public static String validatePath(File directory) {
        return validatePath(directory.getAbsolutePath());
    }

    /**
     * Ensures that the path ends with a file separator
     *
     * @param path Path
     *
     * @return Same path but ensuring that it ends with a file separator
     */
    public static String validatePath(String path) {
        return path.endsWith(File.separator) ? path : path + File.separator;
    }

    private KeyWordHelper() {}
}
