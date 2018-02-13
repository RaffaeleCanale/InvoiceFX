package com.wx.invoicefx.util.string;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 10.06.17.
 */
public class StringUtils {

    /**
     * Replace all occurrences of a target string with a replacement string in the given source.
     *
     * @param source      Source to process
     * @param target      Target string to find and replace
     * @param replacement Replacement for all occurrences of the target string
     */
    public static void replaceAll(StringBuilder source, String target, String replacement) {
        int index;
        int lastIndex = -1;

        while ((index = source.indexOf(target, lastIndex)) >= 0) {
            source.replace(index, index + target.length(), replacement);

            lastIndex = index + replacement.length();
        }
    }


    private StringUtils() {}
}
