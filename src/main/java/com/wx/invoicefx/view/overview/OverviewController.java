package com.wx.invoicefx.view.overview;


import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.config.ConfigProperties;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.config.preferences.SharedProperty;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.save.SaveManager;
import com.wx.invoicefx.util.BindingUtils;
import com.wx.invoicefx.util.string.DoubleArrayConverter;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.invoicefx.view.Stages;
import com.wx.invoicefx.view.other.animation.Animator;
import com.wx.invoicefx.view.other.FormElement;
import com.wx.invoicefx.view.other.NumberTextField;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static javafx.scene.input.KeyCode.K;

/**
 * Created on 30/06/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class OverviewController implements StageController {

    @FXML
    private Pane buttonsPane;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private DatePicker datePicker;
    @FXML
    private NumberTextField idTextField;
    @FXML
    private VBox itemsPanel;
    @FXML
    private Button createButton;

    private SaveManager saveManager;
    private ObservableList<Item> autocompleteItems;

//    private final InvoiceModel invoice = InvoiceHelper.createDefaultInvoice();
    private Invoice invoice = new Invoice();


    @Override
    public void setArguments(Object... args) {
        this.autocompleteItems = FXCollections.observableArrayList();
        this.saveManager = (SaveManager) Objects.requireNonNull(args[0]);


        try {
            this.saveManager.getAllItems().collect(this.autocompleteItems); // TODO Filter, maybe?
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        // ADDRESS
//        addressTextArea.textProperty().bindBidirectional(invoice.addressProperty());
//        forms.add(FormElement.simple(invoice.addressValidityProperty(), addressTextArea));

        // DATE
        datePicker.setConverter(InvoiceFormats.dateConverter());
//        datePicker.valueProperty().bindBidirectional(invoice.dateProperty());
//        forms.add(FormElement.simple(invoice.dateValidityProperty(), datePicker));

        // ID
        idTextField.setNumberFormat(InvoiceFormats.idFormat());
//        idTextField.numberProperty().bindBidirectional(invoice.idProperty());
//        forms.add(FormElement.simple(invoice.idValidityProperty(), idTextField));

//        // ITEMS
//        AlternateColorPanel.bind(itemsPanel);
//
//        GenericListContentBinding.bind(itemsPanel.getChildren(), invoice.getItems(), this::createItemPane);
//
//
        // BUTTONS
        StringProperty encodedVats = ConfigProperties.sharedPreferences().stringProperty(SharedProperty.VAT);
        ReadOnlyProperty<Double[]> vats = BindingUtils.map(encodedVats, DoubleArrayConverter::convert);

        vats.addListener((observable, oldValue, newValue) -> {
            updateVatButtons(newValue);
        });
        updateVatButtons(vats.getValue());
    }

    private Pane createItemPane() {
        FXMLLoader loader = new FXMLLoader(
                OverviewController.class.getResource("/com/wx/invoicefx/view/overview/ItemPanel.fxml"),
                Lang.getBundle());

        try {
            VBox itemPanel = loader.load();
            HBox.setHgrow(itemPanel, Priority.ALWAYS);

            ItemPanelController typeController = loader.getController();

            typeController.setAutoCompleteItems(autocompleteItems);
//            Set<FormElement> itemForms = typeController.bind(item);
//            forms.addAll(itemForms);


            typeController.setOnClose(e -> {
                Animator.instance().collapseAnimation(itemPanel,
                        event -> itemsPanel.getChildren().remove(itemPanel));
            });


            return itemPanel;

        } catch (IOException e) {
            ExceptionLogger.logException(e);
            AlertBuilder.error(e).show();
            return null;
        }
    }

    private void updateVatButtons(Double[] vat) {
        buttonsPane.getChildren().clear();
        for (double t : vat) {
            buttonsPane.getChildren().add(createVatButton(t));
        }
    }

    private Button createVatButton(double vat) {
//        Button button = new Button(Lang.getString("overview.add_item", vat));
        Button button = new Button("VAT " + vat);

        button.setOnAction(e -> {
            Pane itemPane = createItemPane();

            itemsPanel.getChildren().add(itemPane);

            Animator.instance().expandAnimation(itemPane);

        });
        // TODO: 15.05.17 ACTION

        return button;
    }


    public void createInvoice() {
        if (!validateInvoice()) {
            return;
        }

//        ModelManager<InvoiceModel> invoicesManager = Config.invoicesManager();
//
//
//        InvoiceModel existingInvoice = Config.getInvoiceById(invoice.getId());
//        InvoiceModel savedInvoice;
//        if (existingInvoice == null) {
//            savedInvoice = InvoiceModel.copyOf(invoice);
//            invoicesManager.get().add(savedInvoice);
//        } else {
//
//            int suggestedId = Config.suggestId();
//            int choice = AlertBuilder.warning()
//                    .key("confirmation.existing_invoice", invoice.getId(), suggestedId)
//                    .button("confirmation.existing_invoice.create_new")
//                    .button("confirmation.existing_invoice.update_old")
//                    .expandableContent(InvoiceViewer.createViewer(existingInvoice))
//                    .button(ButtonType.CANCEL)
//                    .show();
//
//            switch (choice) {
//                case 0:
//                    invoice.setId(suggestedId);
//                    savedInvoice = InvoiceModel.copyOf(invoice);
//                    invoicesManager.get().add(savedInvoice);
//                    break;
//                case 1:
//                    InvoiceModel.copyValues(invoice, existingInvoice);
//                    savedInvoice = existingInvoice;
//                    break;
//                default:
//                    return;
//            }
//        }
//
//        TexCreatorHelper.createAsync(savedInvoice, createButton);
//
//        List<ItemModel> activeItems = invoice.getItems().stream()
//                .map(ClientItem::getItem)
//                .filter(i -> !i.getItemName().isEmpty())
//                .map(ItemModel::copyOf)
//                .collect(Collectors.toList());
//        boolean changed = Config.mergeLists(
//                Config.itemsManager().get(),
//                activeItems,
//                i -> new Pair<>(i.getTva(), i.getItemName()),
//                false);
//
//        if (changed) {
//            Config.saveSafe(Config.itemsManager());
//        }
//
//        StageManager.close(Stages.OVERVIEW);
//        StageManager.show(Stages.OVERVIEW, savedInvoice);
    }

    public void newInvoice() {
//        InvoiceModel.copyValues(InvoiceHelper.createDefaultInvoice(), invoice);
    }

    public void editItems() {
        StageManager.show(Stages.ITEM_EDITOR);
    }

    public void invoices() {
//        StageManager.show(Stages.INVOICES_ARCHIVE, Config.invoicesManager());
    }

    public void settings() {
        StageManager.show(Stages.SETTINGS);
    }

    public void quit() {
        StageManager.closeAll();
    }

    private boolean validateInvoice() {
//        boolean allValid = true;
//
//        for (FormElement form : forms) {
//            if (!form.isValid()) {
//                Animator.instance().animateInvalid(form);
//                allValid = false;
//            }
//        }
//
//        return allValid;
        return false;
    }


    public void editCurrency() {
        StageManager.show(Stages.CURRENCY_SHORTCUT);
    }
}
