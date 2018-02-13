package com.wx.invoicefx.ui.components.sort;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    private final ObjectProperty<Comparator<T>> comparator = new SimpleObjectProperty<>();
    private final IntegerProperty sortValue = new SimpleIntegerProperty();

    public SortLabelGroup() {
        sortValue.addListener((observable, oldValue, newValue) -> {
            int value = newValue.intValue();

            boolean descending = value < 0;

            SortLabel<T> newLabel = getLabelFor(value);
            newLabel.setSortLabel(descending);

            comparator.set(descending ? newLabel.getComparator().reversed() : newLabel.getComparator());
        });
    }

    public Comparator<T> getComparator() {
        return comparator.get();
    }

    public ObjectProperty<Comparator<T>> comparatorProperty() {
        return comparator;
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator.set(comparator);
    }

    public int getSortValue() {
        return sortValue.get();
    }

    public IntegerProperty sortValueProperty() {
        return sortValue;
    }

    public void setSortValue(int sortValue) {
        this.sortValue.set(sortValue);
    }

    public void addLabel(SortLabel<T> label) {
        labels.add(label);

        final int index = labels.size();
        label.setIndex(index);
        label.setOnMouseClicked(this::onClick);
    }

    private void onClick(Event event) {
        int newValue = ((SortLabel) event.getSource()).getIndex();

        if (getSortValue() == 0) {
            sortValue.set(newValue);

        } else if (abs(getSortValue()) != newValue) {
            SortLabel<T> previous = getLabelFor(getSortValue());
            previous.restoreLabel();

            sortValue.set(newValue);
        } else {
            sortValue.set(- getSortValue());
        }
    }


    private SortLabel<T> getLabelFor(int sortValue) {
        return labels.get(abs(sortValue) - 1);
    }

}
