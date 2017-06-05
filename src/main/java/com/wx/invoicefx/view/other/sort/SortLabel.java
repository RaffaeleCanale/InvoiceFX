package com.wx.invoicefx.view.other.sort;

import javafx.scene.control.Label;

import java.util.Comparator;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 13.05.17.
 */
public class SortLabel<T> extends Label {

    private static final String DESCENDING_SUFFIX = " \u25BC";
    private static final String ASCENDING_SUFFIX = " \u25B2";

    private String baseLabel;
    private int index;
    private Comparator<T> comparator;


    public void setBaseLabel(String baseLabel) {
        this.baseLabel = baseLabel;
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
        this.setText(baseLabel);
    }

    void setSortLabel(boolean descending) {
        this.setText(baseLabel + (descending ? DESCENDING_SUFFIX : ASCENDING_SUFFIX));
    }


}
