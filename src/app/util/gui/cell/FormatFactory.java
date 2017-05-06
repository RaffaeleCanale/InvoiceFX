package app.util.gui.cell;

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
public class FormatFactory<E, F> implements Callback<TableColumn<E, F>, TableCell<E, F>> {

    private final Function<F, String> converter;

    public FormatFactory(Function<F, String> converter) {
        this.converter = converter;
    }

    @Override
    public TableCell<E, F> call(TableColumn<E, F> param) {
        return new TableCell<E, F>() {
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
        };
    }
}
