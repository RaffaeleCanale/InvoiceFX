package com.wx.invoicefx.util.view;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.function.Function;

/**
 * Created on 10/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class FormattedTableFactory<E, F> implements Callback<TableColumn<E, F>, TableCell<E, F>> {

    private Function<F, String> converter;
    private Pos cellAlignment;

    public FormattedTableFactory() {
        this(Object::toString);
    }

    public FormattedTableFactory(Function<F, String> converter) {
        this.converter = converter;
    }

    public void setConverter(Function<F, String> converter) {
        this.converter = converter;
    }

    public void setCellAlignment(Pos cellAlignment) {
        this.cellAlignment = cellAlignment;
    }

    @Override
    public TableCell<E, F> call(TableColumn<E, F> param) {
        TableCell<E, F> cell = new FormattedTableCell();

        if (cellAlignment != null) {
            cell.setAlignment(cellAlignment);
        }

        return cell;
    }

    private class FormattedTableCell extends TableCell<E, F> {

        @Override
        protected void updateItem(F item, boolean empty) {
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
