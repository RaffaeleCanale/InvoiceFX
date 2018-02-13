package com.wx.invoicefx.ui.components.suggestion.itemized.simple;

import com.wx.invoicefx.ui.components.suggestion.textfield.SuggestionTextField;

import java.util.stream.Stream;

import static com.wx.invoicefx.util.string.SentenceItemsParser.STOP_WORD;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 07.06.17.
 */
public class ItemizedSuggestionTextField<T> extends SuggestionTextField<T> {


    public ItemizedSuggestionTextField() {
        setItemConsumer(this::setItem);
    }

    @Override
    protected String processSearchString(String textFieldValue) {
        int lastIndex = getLastSeparatorIndex(textFieldValue);

        if (lastIndex > getCaretPosition()) return ""; // TODO: 07.06.17 Not exactly right

        if (lastIndex > 0 && lastIndex < textFieldValue.length()) {
            textFieldValue = textFieldValue.substring(lastIndex);
        }

        return super.processSearchString(textFieldValue);
    }

    private int getLastSeparatorIndex(String textFieldValue) {
        return Stream.of(STOP_WORD)
                .mapToInt((word) -> lastIndexOf(textFieldValue, word))
                .max().orElse(-1);
    }

    private void setItem(T item) {
        String textFieldValue = getText();

        int lastIndex = getLastSeparatorIndex(textFieldValue);

        if (lastIndex > 0) {
            String prefix = getText().substring(0, lastIndex);

//            if (prefix.charAt(prefix.length() - 1) != ' ') {
//                prefix += " ";
//            }
            setText(prefix + getToStringFn().apply(item));
        } else {
            setText(getToStringFn().apply(item));
        }

        super.positionCaret(getText().length());
    }

    private static int lastIndexOf(String string, String sub) {
        int lastIndex = string.lastIndexOf(sub);

        return lastIndex >= 0 ? lastIndex + sub.length() : -1;
    }
}
