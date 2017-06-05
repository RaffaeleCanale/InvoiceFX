package com.wx.invoicefx.view.other.autocomplete;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is a TextField which implements an "autocomplete" functionality, based on a supplied list of entries.
 *
 * @author Caleb Brinkman
 */
public class AutoCompleteTextField<T> extends TextField {

    private final SortedMap<String, T> entries;
    Function<T, String> toStringFn = Object::toString;
    private ContextMenu entriesPopup;

    private Consumer<T> itemConsumer;

    /**
     * Construct a new AutoCompleteTextField.
     */
    public AutoCompleteTextField() {
        super();
        entries = new TreeMap<>();

        entriesPopup = new ContextMenu();
        textProperty().addListener((observableValue, oldValue, newValue) -> {
            String searchString = newValue.trim().toLowerCase();

            if (searchString.length() == 0) {
                entriesPopup.hide();

            } else {
                Map<String, T> searchResult = new HashMap<>();


                searchResult.putAll(entries.subMap(searchString, searchString + Character.MAX_VALUE));
                if (!entries.isEmpty()) {
                    populatePopup(searchResult);
                    if (!entriesPopup.isShowing()) {
                        entriesPopup.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
                    }
                } else {
                    entriesPopup.hide();
                }
            }
        });

        setOnAction(event -> {
            T value = entries.get(getText().trim().toLowerCase());

            if (value != null) {
                itemConsumer.accept(value);
                setText("");
            }
        });




        focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            entriesPopup.hide();

            if (!newValue) {
                T value = entries.get(getText().trim().toLowerCase());

                if (value != null) {
                    itemConsumer.accept(value);
                    setText("");
                }
            }
        });

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

    public void setItemConsumer(Consumer<T> itemConsumer) {
        this.itemConsumer = itemConsumer;
    }

    public void addEntries(Collection<T> entries) {
        for (T entry : entries) {
            this.entries.put(toStringFn.apply(entry).trim().toLowerCase(), entry);
        }
    }

    /**
     * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
     *
     * @param searchResult The set of matching strings.
     */
    private void populatePopup(Map<String, T> searchResult) {


        List<CustomMenuItem> menuItems = new LinkedList<>();
        // If you'd like more entries, modify this line.
        int maxEntries = 10;
//        int count = Math.min(searchResult.size(), maxEntries);
        int i = 0;

        for (T value : searchResult.values()) {
            String result = toStringFn.apply(value);

            Label entryLabel = new Label(result);
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            item.setOnAction(actionEvent -> {
                if (itemConsumer != null) {
                    itemConsumer.accept(value);
                }
                setText("");
                entriesPopup.hide();
            });
            menuItems.add(item);

            i++;
            if (i > maxEntries) {
                break;
            }
        }
        entriesPopup.getItems().clear();
        entriesPopup.getItems().addAll(menuItems);

    }
}