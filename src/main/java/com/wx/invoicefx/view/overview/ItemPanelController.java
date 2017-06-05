package com.wx.invoicefx.view.overview;

import com.sun.javafx.binding.BidirectionalBinding;
import com.wx.invoicefx.config.ConfigProperties;
import com.wx.invoicefx.config.preferences.SharedProperty;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.view.other.FormElement;
import com.wx.invoicefx.view.other.NumberTextField;
import javafx.beans.binding.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.util.List;
import java.util.Set;

/**
 * Created on 01/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class ItemPanelController {

    @FXML
    private Pane sumPane;
    @FXML
    private Label sumLabel;
    @FXML
    private TextField clientNameField;
    @FXML
    private NumberTextField priceField;
    @FXML
    private CheckBox fromDateCheckbox;
    @FXML
    private Label vatLabel;
    @FXML
    private ComboBox<Item> itemComboBox;
    @FXML
    private Spinner<Integer> itemCountSpinner;
    @FXML
    private CheckBox toDateCheckbox;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private DatePicker fromDatePicker;

    private EventHandler<ActionEvent> onClose;

    public void setOnClose(EventHandler<ActionEvent> onClose) {
        this.onClose = onClose;
    }

    public void setAutoCompleteItems(ObservableList<Item> items) {
        itemComboBox.setItems(items);
    }

    public void initialize() {
        // ITEM
        itemComboBox.setCellFactory(param -> new ItemCell());

        BooleanBinding itemActive = itemComboBox.getEditor().textProperty().isNotEmpty();
        createActiveBinding(itemComboBox, itemActive);

        itemComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            priceField.setNumber(newValue.getPrice());
        });

        // ITEM COUNT
        itemCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));

        BooleanProperty showCount = ConfigProperties.sharedPreferences().booleanProperty(SharedProperty.SHOW_ITEM_COUNT);
        createActiveBinding(itemCountSpinner, itemActive.and(showCount));

        // PRICE
        priceField.setNumberFormat(InvoiceFormats.moneyFormat());
        createActiveBinding(priceField, itemActive);

        // FROM DATE
        fromDatePicker.setConverter(InvoiceFormats.dateConverter());

        BooleanBinding fromDisabled = fromDateCheckbox.selectedProperty().not();
        fromDatePicker.disableProperty().bind(fromDisabled);

        createActiveBinding(fromDatePicker, fromDateCheckbox.selectedProperty());

        // TO DATE
        toDateCheckbox.disableProperty().bind(fromDisabled);
        BooleanBinding toDisabled = toDateCheckbox.selectedProperty().not();
        toDatePicker.disableProperty().bind(toDisabled.or(fromDisabled));

        toDatePicker.setConverter(InvoiceFormats.dateConverter());


        // CLIENT
        createActiveBinding(clientNameField, clientNameField.textProperty().isNotEmpty());

        // TOTAL
        sumLabel.prefWidthProperty().bind(priceField.widthProperty());
        BooleanBinding countGreaterThaneOne = IntegerExpression.integerExpression(itemCountSpinner.valueProperty()).greaterThan(1);
        sumPane.visibleProperty().bind(itemActive.and(countGreaterThaneOne));
    }

//    public Set<FormElement> bind() {
//        Set<FormElement> forms = new HashSet<>();
//        double vat = clientItem.getItem().getTva();

        // CLIENT NAME
//        clientNameField.textProperty().bindBidirectional(clientItem.clientNameProperty());
//        forms.add(FormElement.simple(clientItem.clientNameValidityProperty(), clientNameField));








//        forms.add(FormElement.simple(clientItem.getItem().itemNameValidityProperty(), itemComboBox));



//        itemComboBox.getSelectionModel().select(clientItem.getItem().getItemName());
//        clientItem.getItem().itemNameProperty().bind(itemComboBox.getSelectionModel().selectedItemProperty());
//        //clientItem.getItem().nameProperty().bind(itemComboBox.getEditor().textProperty());
//        itemComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
//            itemComboBox.getSelectionModel().select(newValue);
//            // FIXME: 11/3/15 A bit overkill
//        });



        // ITEM COUNT
//        BidirectionalBinding.bindNumber(
//                itemCountSpinner.getValueFactory().valueProperty(),
//                clientItem.itemCountProperty());
//        forms.add(FormElement.simple(clientItem.itemCountValidityProperty(), itemCountSpinner));




        // PRICE


//        priceField.numberProperty().bindBidirectional(clientItem.getItem().priceProperty());
//        itemComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
//            ObservableList<ItemModel> group = Config.getItemGroup(vat);
//            int index = newValue.intValue();
//
//            if (index >= 0 && index < group.size()) {
//                clientItem.getItem().setPrice(group.get(newValue.intValue()).getPrice());
//            }
//        });


        // FROM DATE
//        fromDateCheckbox.setSelected(!clientItem.getDateEnabled().equals(DateEnabled.NONE));
//
//        fromDatePicker.valueProperty().bindBidirectional(clientItem.fromDateProperty());
//        forms.add(FormElement.simple(clientItem.fromDateValidityProperty(), fromDatePicker));



        // TO DATE
//        toDateCheckbox.setSelected(clientItem.getDateEnabled().equals(DateEnabled.BOTH));
//        toDatePicker.setConverter(InvoiceHelper.dateConverter());
//        toDatePicker.valueProperty().bindBidirectional(clientItem.toDateProperty());
//        forms.add(FormElement.simple(clientItem.toDateValidityProperty(), toDatePicker));

        // DATE ENABLED
//        ObjectBinding<DateEnabled> dateEnabledBinding = Bindings.when(fromDateCheckbox.selectedProperty()).then(
//                Bindings.when(toDateCheckbox.selectedProperty())
//                        .then(DateEnabled.BOTH).otherwise(DateEnabled.ONLY_FROM)
//        ).otherwise(DateEnabled.NONE);
//        clientItem.dateEnabledProperty().bind(dateEnabledBinding);

        // VAT
//        vatLabel.setText(InvoiceHelper.vatFormat().format(vat));


//        sumLabel.textProperty().bind(InvoiceHelper.formattedPrice(clientItem.sumProperty()));
//        sumPane.visibleProperty().bind(itemActive.and(clientItem.itemCountProperty().greaterThan(1)));

//        return forms;
//    }

    private void createActiveBinding(Node node, BooleanExpression condition) {
        node.idProperty().bind(Bindings.when(condition.or(node.focusedProperty())).then("").otherwise("empty_field"));
    }


    public void remove() {
        if (onClose != null) {
            onClose.handle(new ActionEvent());
        }
    }

    private static class ItemCell extends ListCell<Item> {
        // TODO: 16.05.17 Export/generalize such factory

        @Override
        protected void updateItem(Item item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText("");
            } else {
                setText(item.getName());
            }
        }
    }
}
