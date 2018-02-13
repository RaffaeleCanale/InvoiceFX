package com.wx.invoicefx.ui.components.sort;

import javafx.scene.control.Label;

import java.util.Comparator;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 13.05.17.
 */
public class SortLabel<T> extends Label {

    private int index;
    private Comparator<T> comparator;

    public SortLabel() {
        restoreLabel();
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public Comparator<T> getComparator() {
        return comparator;
    }


    void setIndex(int index) {
        if (index < 1) throw new IllegalArgumentException();

        this.index = index;
    }

    int getIndex() {
        return index;
    }

    void restoreLabel() {
        getStyleClass().setAll("sort-label");
    }

    void setSortLabel(boolean descending) {
        getStyleClass().setAll("sort-label", "sort-label-" + (descending ? "up" : "down"));
    }


}
