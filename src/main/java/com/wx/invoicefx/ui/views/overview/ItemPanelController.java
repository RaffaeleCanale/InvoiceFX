package com.wx.invoicefx.ui.views.overview;

import com.wx.fx.Lang;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.entities.ModelComparator;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.entities.item.Vats;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.ui.animation.Animator;
import com.wx.invoicefx.ui.components.suggestion.textfield.SuggestionTextField;
import com.wx.invoicefx.ui.components.suggestion.combobox.SuggestionComboBoxController;
import com.wx.invoicefx.ui.components.suggestion.itemized.simple.ItemizedSuggestionTextField;
import com.wx.invoicefx.util.ModelUtil;
import com.wx.invoicefx.util.string.SentenceItemsParser;
import com.wx.invoicefx.ui.components.NumberTextField;
import com.wx.util.pair.Pair;
import javafx.beans.binding.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static com.wx.invoicefx.ui.views.overview.OverviewController.addInvalidRemovalListener;

/**
 * Created on 01/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class ItemPanelController {

    @FXML
    private Button removeButton;
    @FXML
    private Pane sumPane;
    @FXML
    private Label sumLabel;
    @FXML
    private ItemizedSuggestionTextField<Client> clientNameField;
    @FXML
    private NumberTextField priceField;
    @FXML
    private CheckBox fromDateCheckbox;
    @FXML
    private Label vatLabel;
    @FXML
    private SuggestionComboBoxController<Item> itemComboBoxController;
    private Item lastSelectedItem;

    @FXML
    private Spinner<Integer> itemCountSpinner;
    @FXML
    private CheckBox toDateCheckbox;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private DatePicker fromDatePicker;

    private Vat vat;

    Button getRemoveButton() {
        return removeButton;
    }

    void setVat(Vat vat) {
        this.vat = vat;

        vatLabel.setText(InvoiceFormats.vatFormat().format(vat.getValue()));
        vatLabel.getStyleClass().setAll("text-disabled-1");

        Vats allVats = AppResources.getAllVats();
        if (!allVats.contains(vat)) {
            vatLabel.getStyleClass().addAll("text-error", "warning-label");
        }
    }

    Vat getVat() {
        return vat;
    }

    void setAutoCompleteItems(SortedMap<String, Item> items, SortedMap<String, Client> clients) {
        itemComboBoxController.getTextField().setEntries(items);

        clientNameField.setEntries(clients);
    }

    @FXML
    private void initialize() {
        // ITEM
        itemComboBoxController.getTextField().setPromptText(Lang.getString("stage.overview.item.label.item_name"));
        itemComboBoxController.setToStringFn(Item::getName);
        itemComboBoxController.getTextField().setItemConsumer(item -> {
            this.lastSelectedItem = item;

            itemComboBoxController.getTextField().setTextAndMoveCaret(item.getName());

            priceField.setNumber(item.getPrice());

            Animator.instance().removeInvalid(priceField).run();
            Animator.instance().removeInvalid(itemComboBoxController.getTextField()).run();
        });

        itemComboBoxController.setCellFactory(item -> {
            BorderPane borderPane = new BorderPane();
            Label nameLabel = new Label(item.getName());
            Label priceLabel = new Label(InvoiceFormats.moneyFormat().format(item.getPrice()));

            priceLabel.getStyleClass().add("text-disabled-1");

            borderPane.setLeft(nameLabel);
            borderPane.setRight(priceLabel);

            nameLabel.setPrefWidth(itemComboBoxController.getTextField().getWidth());

            return borderPane;
        });

        addInvalidRemovalListener(itemComboBoxController.getTextField());

        // ITEM COUNT
        itemCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        addInvalidRemovalListener(itemCountSpinner);

        // PRICE
        priceField.setNumberFormat(InvoiceFormats.moneyFormat());
        priceField.setNumber(0);

        addInvalidRemovalListener(priceField);

        // FROM DATE
        fromDatePicker.setValue(LocalDate.now());
        fromDatePicker.setConverter(InvoiceFormats.dateConverter());

        BooleanBinding fromDisabled = fromDateCheckbox.selectedProperty().not();
        fromDatePicker.disableProperty().bind(fromDisabled);

        addInvalidRemovalListener(fromDatePicker);

        // TO DATE
        toDatePicker.setValue(LocalDate.now().plusDays(1));
        toDatePicker.setConverter(InvoiceFormats.dateConverter());

        toDateCheckbox.disableProperty().bind(fromDisabled);
        BooleanBinding toDisabled = toDateCheckbox.selectedProperty().not();
        toDatePicker.disableProperty().bind(toDisabled.or(fromDisabled));

        addInvalidRemovalListener(toDatePicker);

        // CLIENT
        clientNameField.setToStringFn(Client::getName);

        addInvalidRemovalListener(clientNameField);

        // TOTAL
        sumLabel.prefWidthProperty().bind(priceField.widthProperty());
        IntegerExpression itemCountExpression = IntegerExpression.integerExpression(itemCountSpinner.valueProperty());
        BooleanBinding countGreaterThanOne = itemCountExpression.greaterThan(1);
        sumPane.visibleProperty().bind(countGreaterThanOne);

        NumberBinding total = itemCountExpression.multiply(priceField.numberProperty());
        sumLabel.textProperty().bind(InvoiceFormats.formattedPrice(total));
    }

    Set<Node> getInvalidNodes(boolean clientIsOptional) {
        Set<Node> nodes = new HashSet<>();

        // ITEM
        SuggestionTextField<Item> itemField = itemComboBoxController.getTextField();
        if (itemField.getText().trim().isEmpty()) {
            nodes.add(itemField);
        }


        // ITEM COUNT
        Integer count = itemCountSpinner.getValue();
        if (count == null || count <= 0) {
            nodes.add(itemCountSpinner);
        }

        // PRICE
        Number price = priceField.getNumber();
        if (price.doubleValue() < 0) {
            nodes.add(priceField);
        }

        // FROM DATE
        if (fromDateCheckbox.isSelected() && fromDatePicker.getValue() == null) {
            nodes.add(fromDatePicker);
        }

        // TO DATE
        if (toDateCheckbox.isSelected() && toDatePicker.getValue() == null) {
            nodes.add(toDatePicker);
        }

        // CLIENT
        if (clientNameField.getText().isEmpty()) {
            if (!clientIsOptional) {
                nodes.add(clientNameField);
            }
        } else {
            List<String> clientNames = SentenceItemsParser.splitStopWords(
                    SentenceItemsParser.parseItems(clientNameField.getText())
            ).get1();
            if (clientNames.stream()
                    .map(String::trim)
                    .filter(String::isEmpty)
                    .findAny().isPresent()) {
                nodes.add(clientNameField);
            }
        }

        return nodes;
    }

    Pair<List<Client>, List<String>> buildClients() {
        return ModelUtil.parseClients(clientNameField.getText());
    }

    Item buildItem() {
        Item item = new Item();

        item.setName(itemComboBoxController.getTextField().getText());
        item.setPrice(priceField.getNumber().doubleValue());
        item.setVat(vat);
        item.setDefaultDateEnabled(getDateEnabled());
        item.setActive(true);

        if (ModelComparator.contentEquals(item, lastSelectedItem)) {
            return lastSelectedItem;
        }

        return item;
    }

    Purchase buildPurchase() {
        Purchase purchase = new Purchase();

        purchase.setItem(buildItem());
        purchase.setItemCount(itemCountSpinner.getValue());
        purchase.setDateEnabled(getDateEnabled());
        if (fromDateCheckbox.isSelected()) purchase.setFromDate(fromDatePicker.getValue());
        if (toDateCheckbox.isSelected()) purchase.setToDate(toDatePicker.getValue());

        return purchase;
    }

    private DateEnabled getDateEnabled() {
        if (fromDateCheckbox.isSelected()) {
            if (toDateCheckbox.isSelected()) {
                return DateEnabled.BOTH;
            }

            return DateEnabled.ONLY_FROM;
        }

        return DateEnabled.NONE;
    }

    void loadPurchase(Purchase purchase) {
        itemComboBoxController.getTextField().setTextSilent(purchase.getItem().getName());
        itemCountSpinner.getValueFactory().setValue(purchase.getItemCount());
        priceField.setNumber(purchase.getItem().getPrice());

        fromDateCheckbox.setSelected(purchase.getDateEnabled() != DateEnabled.NONE);
        if (fromDateCheckbox.isSelected()) {
            fromDatePicker.setValue(purchase.getFromDate());
        }

        toDateCheckbox.setSelected(purchase.getDateEnabled() == DateEnabled.BOTH);
        if (toDateCheckbox.isSelected()) {
            toDatePicker.setValue(purchase.getToDate());
        }
    }

    void loadClients(List<Client> clients, List<String> stopWords) {
        List<String> words = clients.stream().map(Client::getName).collect(Collectors.toList());

        if (words.isEmpty()) {
            clientNameField.setTextSilent("");
            return;
        }

        String clientsSentence = SentenceItemsParser.rebuildSentence(words, stopWords);
        clientNameField.setTextSilent(clientsSentence);
    }
}
