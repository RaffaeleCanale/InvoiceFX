package com.wx.invoicefx.util.view;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.util.function.Function;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 08.06.17.
 */
public class FormattedListFactory<T> implements Callback<ListView<T>, ListCell<T>> {

    private Function<T, String> converter;

    public FormattedListFactory() {
        this(Object::toString);
    }

    public FormattedListFactory(Function<T, String> converter) {
        this.converter = converter;
    }

    public void setConverter(Function<T, String> converter) {
        this.converter = converter;
    }

    @Override
    public ListCell<T> call(ListView<T> param) {
        ListCell<T> cell = new FormattedListCell();

        cell.widthProperty().addListener((observable, oldValue, newValue) -> {
            notifyCellWidth(cell, newValue.doubleValue());
        });

        cell.hoverProperty().addListener((observable, oldValue, newValue) -> {
            onHover(cell, newValue);
        });

        return cell;
    }

    protected void notifyCellWidth(ListCell<T> cell, double width) {}

    protected void onHover(ListCell<T> cell, Boolean hover) {}

    private class FormattedListCell extends ListCell<T> {

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(converter.apply(item));
            }
        }
    }
}
