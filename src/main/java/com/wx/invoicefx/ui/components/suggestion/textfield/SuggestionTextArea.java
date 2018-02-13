package com.wx.invoicefx.ui.components.suggestion.textfield;

import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is a TextField which implements an "autocomplete" functionality, based on a supplied list of entries.
 *
 * @author Caleb Brinkman
 */
public class SuggestionTextArea<T> extends TextArea {

    private static final int MAX_ENTRIES = 10;

    private SortedMap<String, T> entries;
    private Function<T, String> toStringFn = Object::toString;
    private final ContextMenu entriesPopup;

    private Consumer<T> itemConsumer;

    /**
     * Construct a new AutoCompleteTextField.
     */
    public SuggestionTextArea() {
        super();
        entries = new TreeMap<>();
        entriesPopup = new ContextMenu();

        textProperty().addListener((observableValue, oldValue, newValue) -> {
            String searchString = processSearchString(newValue);

            if (searchString.length() == 0) {
                entriesPopup.hide();

            } else {
                Map<String, T> searchResult = new HashMap<>();


                searchResult.putAll(entries.subMap(searchString, searchString + Character.MAX_VALUE));
                if (!entries.isEmpty()) {
                    showEntries(searchResult);
                } else {
                    entriesPopup.hide();
                }
            }
        });

        setOnKeyTyped(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                T value = entries.get(getText().trim().toLowerCase());

                if (value != null) {
                    itemConsumer.accept(value);
                    setText("");
                }
            }
        });

        focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            entriesPopup.hide();

            if (!newValue) {
                T value = entries.get(getText().trim().toLowerCase());

                if (value != null) {
                    itemConsumer.accept(value);
                }
            }
        });

    }

    public Consumer<T> getItemConsumer() {
        return itemConsumer;
    }

    protected String processSearchString(String textFieldValue) {
        return textFieldValue.trim().toLowerCase();
    }

    public void setTextSilent(String value) {
        setText(value);
        entriesPopup.hide();
    }

    /**
     * Get the existing set of autocomplete entries.
     *
     * @return The existing autocomplete entries.
     */
    public SortedMap<String, T> getEntries() {
        return entries;
    }

    public void setToStringFn(Function<T, String> toStringFn) {
        this.toStringFn = toStringFn;
    }

    public Function<T, String> getToStringFn() {
        return toStringFn;
    }

    public void setItemConsumer(Consumer<T> itemConsumer) {
        this.itemConsumer = itemConsumer;
    }


    public void setEntries(SortedMap<String, T> entries) {
        this.entries = entries;
    }

    public void setEntries(Collection<T> entries) {
        this.entries.clear();
        for (T entry : entries) {
            this.entries.put(toStringFn.apply(entry).trim().toLowerCase(), entry);
        }
    }

    public void hidePopup() {
        entriesPopup.hide();
    }

    public void showEntries(Map<String, T> searchResult) {
        populatePopup(searchResult);
        if (!entriesPopup.isShowing()) {


            entriesPopup.show(SuggestionTextArea.this, Side.BOTTOM, 0, 0);
        }
    }

    /**
     * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
     *
     * @param searchResult The set of matching strings.
     */
    private void populatePopup(Map<String, T> searchResult) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        int i = 0;

        for (T value : searchResult.values()) {
            String result = toStringFn.apply(value);

            Label entryLabel = new Label(result);
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            item.setOnAction(actionEvent -> {
                if (itemConsumer != null) {
                    itemConsumer.accept(value);
                }
//                setText("");
                entriesPopup.hide();
            });
            menuItems.add(item);

            i++;
            if (i > MAX_ENTRIES) {
                break;
            }
        }
        entriesPopup.getItems().clear();
        entriesPopup.getItems().addAll(menuItems);
    }

    public void setTextAndMoveCaret(String text) {
        setText(text);
        positionCaret(text.length());
    }
}