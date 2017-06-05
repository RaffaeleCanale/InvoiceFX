package com.wx.invoicefx.view.other.sort;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.abs;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 13.05.17.
 */
public class SortLabelGroup<T> {

    private final List<SortLabel<T>> labels = new ArrayList<>();
    private int sortIndex;

    private final ObjectProperty<Comparator<T>> comparator = new SimpleObjectProperty<>();

    public ReadOnlyObjectProperty<Comparator<T>> comparatorProperty() {
        return comparator;
    }

    public void addLabel(SortLabel<T> label) {
        labels.add(label);

        final int index = labels.size();
        label.setIndex(index);
        label.setOnMouseClicked(this::onClick);
    }


    private void onClick(Event event) {
        int newIndex = ((SortLabel) event.getSource()).getIndex();

        if (sortIndex == 0) {
            sortIndex = newIndex;

        } else if (abs(sortIndex) != newIndex && sortIndex != 0) {
            SortLabel<T> previous = labels.get(abs(sortIndex) - 1);
            previous.restoreLabel();

            sortIndex = newIndex;
        } else {
            sortIndex = -sortIndex;
        }

        boolean descending = sortIndex < 0;

        SortLabel<T> newLabel = labels.get(abs(sortIndex) - 1);
        newLabel.setSortLabel(descending);

        comparator.set(descending ? newLabel.getComparator().reversed() : newLabel.getComparator());
    }




}
