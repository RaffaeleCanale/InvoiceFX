package app.gui.overview;

import app.config.Config;
import app.util.helpers.InvoiceHelper;
import app.config.preferences.properties.SharedProperty;
import app.legacy.model.DateEnabled;
import app.legacy.model.item.ClientItem;
import app.legacy.model.item.ItemModel;
import app.util.bindings.FormElement;
import app.util.bindings.GenericListContentBinding;
import app.util.gui.components.NumberTextField;
import com.sun.javafx.binding.BidirectionalBinding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.util.HashSet;
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
    private ComboBox<String> itemComboBox;
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

    public void initialize() {
        // TOTAL
        sumLabel.prefWidthProperty().bind(priceField.widthProperty());

        // PRICE
        priceField.setNumberFormat(InvoiceHelper.moneyFormat());


        // ITEM COUNT
        itemCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));


        // FROM DATE
        BooleanBinding fromDisabled = fromDateCheckbox.selectedProperty().not();
        fromDatePicker.disableProperty().bind(fromDisabled);

        // TO DATE
        toDateCheckbox.disableProperty().bind(fromDisabled);
        BooleanBinding toDisabled = toDateCheckbox.selectedProperty().not();
        toDatePicker.disableProperty().bind(toDisabled.or(fromDisabled));
    }

    public Set<FormElement> bind(ClientItem clientItem) {
        Set<FormElement> forms = new HashSet<>();
        double vat = clientItem.getItem().getTva();

        // CLIENT NAME
        clientNameField.textProperty().bindBidirectional(clientItem.clientNameProperty());
        forms.add(FormElement.simple(clientItem.clientNameValidityProperty(), clientNameField));

        createActiveBinding(clientNameField, clientItem.clientNameProperty().isNotEmpty());


        // ITEMS
        ObservableList<String> itemNames = FXCollections.observableArrayList();
        GenericListContentBinding.bind(itemNames, Config.getItemGroup(vat), ItemModel::getItemName);

        itemComboBox.setItems(itemNames);

        forms.add(FormElement.simple(clientItem.getItem().itemNameValidityProperty(), itemComboBox));

        BooleanBinding itemActive = clientItem.getItem().itemNameProperty().isNotEmpty();
        createActiveBinding(itemComboBox, itemActive);

        itemComboBox.getSelectionModel().select(clientItem.getItem().getItemName());
        clientItem.getItem().itemNameProperty().bind(itemComboBox.getSelectionModel().selectedItemProperty());
        //clientItem.getItem().nameProperty().bind(itemComboBox.getEditor().textProperty());
        itemComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            itemComboBox.getSelectionModel().select(newValue);
            // FIXME: 11/3/15 A bit overkill
        });



        // ITEM COUNT
        BidirectionalBinding.bindNumber(
                itemCountSpinner.getValueFactory().valueProperty(),
                clientItem.itemCountProperty());
        forms.add(FormElement.simple(clientItem.itemCountValidityProperty(), itemCountSpinner));

        BooleanProperty showCount = Config.sharedPreferences().booleanProperty(SharedProperty.SHOW_ITEM_COUNT);
        createActiveBinding(itemCountSpinner, itemActive.and(showCount));


        // PRICE
        createActiveBinding(priceField, itemActive);

        priceField.numberProperty().bindBidirectional(clientItem.getItem().priceProperty());
        itemComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<ItemModel> group = Config.getItemGroup(vat);
            int index = newValue.intValue();

            if (index >= 0 && index < group.size()) {
                clientItem.getItem().setPrice(group.get(newValue.intValue()).getPrice());
            }
        });
        

        // FROM DATE
        fromDateCheckbox.setSelected(!clientItem.getDateEnabled().equals(DateEnabled.NONE));
        fromDatePicker.setConverter(InvoiceHelper.dateConverter());
        fromDatePicker.valueProperty().bindBidirectional(clientItem.fromDateProperty());
        forms.add(FormElement.simple(clientItem.fromDateValidityProperty(), fromDatePicker));

        createActiveBinding(fromDatePicker, clientItem.dateEnabledProperty().isNotEqualTo(DateEnabled.NONE));

        // TO DATE
        toDateCheckbox.setSelected(clientItem.getDateEnabled().equals(DateEnabled.BOTH));
        toDatePicker.setConverter(InvoiceHelper.dateConverter());
        toDatePicker.valueProperty().bindBidirectional(clientItem.toDateProperty());
        forms.add(FormElement.simple(clientItem.toDateValidityProperty(), toDatePicker));

        // DATE ENABLED
        ObjectBinding<DateEnabled> dateEnabledBinding = Bindings.when(fromDateCheckbox.selectedProperty()).then(
                Bindings.when(toDateCheckbox.selectedProperty())
                        .then(DateEnabled.BOTH).otherwise(DateEnabled.ONLY_FROM)
        ).otherwise(DateEnabled.NONE);
        clientItem.dateEnabledProperty().bind(dateEnabledBinding);

        // VAT
        vatLabel.setText(InvoiceHelper.vatFormat().format(vat));


        sumLabel.textProperty().bind(InvoiceHelper.formattedPrice(clientItem.sumProperty()));
        sumPane.visibleProperty().bind(itemActive.and(clientItem.itemCountProperty().greaterThan(1)));

        return forms;
    }

    private void createActiveBinding(Node node, BooleanBinding condition) {
        node.idProperty().bind(Bindings.when(condition.or(node.focusedProperty())).then("").otherwise("empty_field"));
    }


    public void remove() {
        if (onClose != null) {
            onClose.handle(new ActionEvent());
        }
    }
}
