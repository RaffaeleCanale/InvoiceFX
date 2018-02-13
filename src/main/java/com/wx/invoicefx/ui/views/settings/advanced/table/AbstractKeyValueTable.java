package com.wx.invoicefx.ui.views.settings.advanced.table;

import com.wx.fx.Lang;
import com.wx.invoicefx.util.view.FormattedTableFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 20.06.17.
 */
public abstract class AbstractKeyValueTable<K, V> extends BorderPane {

    private final TableView<KeyValue> tableView = new TableView<>();

    private final StringConverter<V> valueConverter;

    public AbstractKeyValueTable(StringConverter<V> valueConverter) {
        this.valueConverter = valueConverter;

        init();
    }

    public void setItems(K[] props) {
        setItems(Arrays.asList(props));
    }

    public void setItems(Iterable<K> props) {
        ObservableList<KeyValue> items = tableView.getItems();

        items.clear();
        for (K prop : props) {
            items.add(new KeyValue(prop));
        }
    }

    protected void init() {
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<KeyValue, K> keyColumn = new TableColumn<>(Lang.getString("stage.settings.advanced.table.key"));
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyColumn.setCellFactory(new FormattedTableFactory<>(this::keyToString));

        TableColumn<KeyValue, V> valueColumn = new TableColumn<>(Lang.getString("stage.settings.advanced.table.value"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn(valueConverter));
        valueColumn.setEditable(true);
        valueColumn.setOnEditCommit(e -> {
            V newValue = e.getNewValue();
            KeyValue keyValue = e.getRowValue();

            if (set(keyValue.key, newValue)) {
                keyValue.value = newValue;
            } else {
                ObservableList<KeyValue> items = FXCollections.observableArrayList(e.getTableView().getItems());
                e.getTableView().getItems().clear();
                e.getTableView().setItems(items);
            }
        });

        tableView.getColumns().add(keyColumn);
        tableView.getColumns().add(valueColumn);


        tableView.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.DELETE)) {
                Set<KeyValue> toRemove = tableView.getSelectionModel().getSelectedItems().stream()
                        .filter(selection -> remove(selection.key, selection.value))
                        .collect(Collectors.toSet());

                tableView.getItems().removeAll(toRemove);
            }
        });

        setCenter(tableView);
    }

    protected String keyToString(K key) {
        return Objects.toString(key);
    }

    protected abstract V get(K key);

    protected abstract boolean set(K key, V value);

    protected boolean remove(K key, V value) {
        return false;
    }


    public class KeyValue {

        private final K key;
        private V value;

        public KeyValue(K key) {
            this.key = key;
            this.value = get(key);
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
