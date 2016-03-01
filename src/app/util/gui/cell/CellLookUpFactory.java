package app.util.gui.cell;

import app.App;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created on 06/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class CellLookUpFactory<E> implements Callback<TableColumn<E, String>, TableCell<E, String>> {

    private static final String EMPTY_CELL_LABEL = App.getLang().getString("overview.name_prompt");

    private final Map<Integer, TableCell<E, String>> lookUp = new HashMap<>();

    @Override
    public TableCell<E, String> call(TableColumn<E, String> param) {
        return new CellImpl();
    }

    public Collection<TableCell<E, String>> allCells() {
        return lookUp.values();
    }

    public TableCell<E, String> lookUp(int index) {
        return lookUp.get(index);
    }

    public TableCell<E, String> lookUp(E model) {
        Iterator<TableCell<E, String>> it = lookUp.values().iterator();
        if (!it.hasNext()) {
            return null;
        }
        TableView<E> table = it.next().getTableView();
        return lookUp(table.getItems().indexOf(model));
    }

    private class CellImpl extends CTextFieldTableCell<E, String> {



        public CellImpl() {
            super(new DefaultStringConverter());


        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            setId("");
            if (!empty) {
                lookUp.put(getIndex(), this);

                if (item == null || item.isEmpty()) {
                    setText(EMPTY_CELL_LABEL);
                    setId("table_empty_cell");
                }
            } else {
                lookUp.remove(getIndex());
            }
        }
    }
}
