package app.gui.config.properties;

import app.util.gui.cell.CTextFieldTableCell;
import app.util.gui.cell.CellLookUpFactory;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created on 15/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class PropertiesTableController<K> {

    @FXML
    private TableView<Property> table;

    private final ObservableList<Property> properties = FXCollections.observableArrayList();

    private final Map<Property, StringProperty[]> styleProperties = new HashMap<>();

    private Function<K, String> keyToString;
    private Function<K, StringProperty> valuePropertyFunction;
    private Function<K, Boolean> useAlternativeStyle;

    private final CellLookUpFactory<Property> valuesLookUp = new CellLookUpFactory<>();
    private final CellLookUpFactory<Property> keysLookUp = new CellLookUpFactory<>();

    public void initialize() {
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        TableColumn<Property, String> keyColumn = (TableColumn<Property, String>) table.getColumns().get(0);
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyColumn.setCellFactory(keysLookUp);

        TableColumn<Property, String> valueColumn = (TableColumn<Property, String>) table.getColumns().get(1);
        valueColumn.setEditable(true);
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory(valuesLookUp);

        EventHandler<TableColumn.CellEditEvent<Property, String>> handler = valueColumn.getOnEditCommit();
        valueColumn.setOnEditCommit(e -> {
            handler.handle(e);
            refreshCellsStyle();
        });


        table.setItems(properties);
    }

    public void setFunctionsEditable(Function<K, String> keyToString,
                                     Function<K, StringProperty> valuePropertyFunction,
                                     Function<K, Boolean> useAlternativeStyle,
                                     BiFunction<K, StringProperty, Boolean> removeFunction) {
        this.valuePropertyFunction = valuePropertyFunction;
        initFunctions(keyToString, useAlternativeStyle, removeFunction);
    }

    public void setFunctions(Function<K, String> keyToString,
                             Function<K, String> valueToString,
                             Function<K, Boolean> useAlternativeStyle,
                             BiFunction<K, StringProperty, Boolean> removeFunction) {
        this.valuePropertyFunction = valueToString.andThen(ReadOnlyStringWrapper::new);
        table.getColumns().get(1).setEditable(false);

        initFunctions(keyToString, useAlternativeStyle, removeFunction);
    }

    public void setAction(Consumer<K> actionFunction) {
        table.setOnMouseClicked(event -> {
            int selectedItemsCount = table.getSelectionModel().getSelectedItems().size();
            if (event.getClickCount() == 2 && selectedItemsCount == 1) {
                Property selection = table.getSelectionModel().getSelectedItem();
                actionFunction.accept(selection.key);
            }
        });
    }

    private void initFunctions(Function<K, String> keyToString,
                               Function<K, Boolean> useAlternativeStyle,
                               BiFunction<K, StringProperty, Boolean> removeFunction) {
        this.keyToString = keyToString;
        this.useAlternativeStyle = useAlternativeStyle == null ? k -> false : useAlternativeStyle;

        if (removeFunction != null) {
            table.setOnKeyPressed(event -> {
                if (event.getCode().equals(KeyCode.DELETE)) {
                    Set<Property> toRemove = table.getSelectionModel().getSelectedItems().stream()
                            .filter(selection -> removeFunction.apply(selection.key, selection.value))
                            .collect(Collectors.toSet());

                    properties.removeAll(toRemove);
                }
            });
        }
    }

    public void setItems(K[] props) {
        setItems(Arrays.asList(props));
    }

    public void setItems(Iterable<K> props) {
        properties.clear();
        for (K prop : props) {
            properties.add(new Property(prop));
        }

        Collections.sort(properties, Comparator.comparing(Property::getKey));

        Platform.runLater(this::refreshCellsStyle);
    }

    private void refreshCellsStyle() {
        for (Property property : properties) {
            String style = useAlternativeStyle.apply(property.key) ? "alternate_cell" : "";

            TableCell<Property, String> valueCell = valuesLookUp.lookUp(property);
            if (valueCell != null) {
                valueCell.getTableRow().setId(style);
//                valueCell.setId(style);
//                valueCell.setEffect(new InnerShadow());
            }
//            TableCell<Property, String> keyCell = keysLookUp.lookUp(property);
//            if (keyCell != null) {
//                keyCell.setId(style);
//            }
        }
    }

    private void updateStyle(Property property) {
        String style = useAlternativeStyle.apply(property.key) ? "alternate_cell" : "";

        for (StringProperty cell : styleOf(property)) {
            if (cell != null) {
                cell.setValue(style);
            }
        }
    }

    private StringProperty[] styleOf(Property property) {
        StringProperty[] style = styleProperties.get(property);
        if (style == null) {
            style = new StringProperty[2];
            styleProperties.put(property, style);
        }

        return style;
    }

    public class Property {
        private final K key;
        private final StringProperty value;

        public Property(K key) {
            this.key = key;
            this.value = valuePropertyFunction.apply(key);
        }

        public String getKey() {
            return keyToString.apply(key);
        }

        public String getValue() {
            return value.getValueSafe();
        }

        public void setValue(String value) {
            this.value.set(value);
        }

        public StringProperty valueProperty() {
            return value;
        }
    }



    private class StyleFactory implements Callback<TableColumn<Property, String>, TableCell<Property, String>> {

        @Override
        public TableCell<Property, String> call(TableColumn<Property, String> param) {
            return new CellImpl();
        }


        private class CellImpl extends CTextFieldTableCell<Property, String> {

            public CellImpl() {
                super(new DefaultStringConverter());
            }


            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {

                    if (useAlternativeStyle.apply(properties.get(getIndex()).key)) {
                        setStyle("-fx-background-color: blue");
                    } else {
                        setStyle("");
                    }

//                    if (useAlternativeStyle.apply(item)) {
//                        setStyle("-fx-background-color: blue");
//                    } else {
//                        setStyle("");
//                    }
//
//
//                    setText(keyToString.apply(item));
                }
            }
        }
    }
}
