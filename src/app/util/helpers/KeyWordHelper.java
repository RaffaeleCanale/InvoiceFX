package app.util.helpers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created on 11/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class KeyWordHelper {

    public static List<String> keyWordsIn(String text) {
        int i = 0;

        List<String> keyWords = new LinkedList<>();
        while ((i = text.indexOf("${", i)) >= 0) {
            int start = i + 2;
            int end = text.indexOf('}', start);
            if (end < 0) end = text.length();

            keyWords.add(text.substring(start, end));
            i = end;
        }

        return keyWords;
    }

    public static String replace(String target, String keyWord, String value) {
        return target.replace(keyPattern(keyWord), value);
    }

    public static String keyPattern(String key) {
        return "${" + key + "}";
    }

    public static String getDirectoryPath(File directory) {
        return getDirectoryPath(directory.getAbsolutePath());
    }

    public static String getDirectoryPath(String path) {
        return path.endsWith(File.separator) ? path : path + File.separator;
    }
}
