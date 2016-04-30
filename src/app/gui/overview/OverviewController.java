package app.gui.overview;

import app.Stages;
import app.config.Config;
import app.config.manager.ModelManager;
import app.config.preferences.properties.SharedProperty;
import app.model.invoice.InvoiceModel;
import app.model.item.ClientItem;
import app.model.item.ItemModel;
import app.util.ExceptionLogger;
import app.util.bindings.FormElement;
import app.util.bindings.GenericListContentBinding;
import app.util.gui.AlertBuilder;
import app.util.gui.components.AlternateColorPanel;
import app.util.gui.components.NumberTextField;
import app.util.helpers.InvoiceHelper;
import app.util.helpers.InvoiceViewer;
import app.util.helpers.TexCreatorHelper;
import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final InvoiceModel invoice = InvoiceHelper.createDefaultInvoice();

    private final Set<FormElement> forms = new HashSet<>();

    @Override
    public void setArguments(Object... args) {
        InvoiceModel.copyValues((InvoiceModel) args[0], invoice);
    }

    public void initialize() {
        // ADDRESS
        addressTextArea.textProperty().bindBidirectional(invoice.addressProperty());
        forms.add(FormElement.simple(invoice.addressValidityProperty(), addressTextArea));

        // DATE
        datePicker.setConverter(InvoiceHelper.dateConverter());
        datePicker.valueProperty().bindBidirectional(invoice.dateProperty());
        forms.add(FormElement.simple(invoice.dateValidityProperty(), datePicker));

        // ID
        idTextField.setNumberFormat(InvoiceHelper.idFormat());
        idTextField.numberProperty().bindBidirectional(invoice.idProperty());
        forms.add(FormElement.simple(invoice.idValidityProperty(), idTextField));

        // ITEMS
        AlternateColorPanel.bind(itemsPanel);

        GenericListContentBinding.bind(itemsPanel.getChildren(), invoice.getItems(), this::createItemPane);


        // BUTTONS
        ObjectProperty<double[]> vats = Config.sharedPreferences().doubleArrayProperty(SharedProperty.VAT);
        vats.addListener((observable, oldValue, newValue) -> {
            updateVatButtons(newValue);
        });
        updateVatButtons(vats.get());
    }

    private Pane createItemPane(ClientItem item) {
        FXMLLoader loader = new FXMLLoader(
                OverviewController.class.getResource("/app/gui/overview/ItemPanel.fxml"),
                Lang.getBundle());

        try {
            VBox itemPanel = loader.load();
            HBox.setHgrow(itemPanel, Priority.ALWAYS);

            ItemPanelController typeController = loader.getController();

            Set<FormElement> itemForms = typeController.bind(item);
            forms.addAll(itemForms);


            typeController.setOnClose(e -> {
                forms.removeAll(itemForms);
                invoice.getItems().remove(item);
            });


            return itemPanel;

        } catch (IOException e) {
            ExceptionLogger.logException(e);
            AlertBuilder.error(e).show();
            return null;
        }
    }

    private void updateVatButtons(double[] vat) {
        buttonsPane.getChildren().clear();
        for (double t : vat) {
            buttonsPane.getChildren().add(createVatButton(t));
        }
    }

    private Button createVatButton(double vat) {
        Button button = new Button(Lang.getString("overview.add_item", vat));
        button.setOnAction(e -> invoice.getItems().add(InvoiceHelper.createDefaultClientItem(vat)));

        return button;
    }


    public void createInvoice() {
        if (!validateInvoice()) {
            return;
        }

        ModelManager<InvoiceModel> invoicesManager = Config.invoicesManager();


        InvoiceModel existingInvoice = Config.getInvoiceById(invoice.getId());
        InvoiceModel savedInvoice;
        if (existingInvoice == null) {
            savedInvoice = InvoiceModel.copyOf(invoice);
            invoicesManager.get().add(savedInvoice);
        } else {

            int suggestedId = Config.suggestId();
            int choice = AlertBuilder.warning()
                    .key("confirmation.existing_invoice", invoice.getId(), suggestedId)
                    .button("confirmation.existing_invoice.create_new")
                    .button("confirmation.existing_invoice.update_old")
                    .expandableContent(InvoiceViewer.createViewer(existingInvoice))
                    .button(ButtonType.CANCEL)
                    .show();

            switch (choice) {
                case 0:
                    invoice.setId(suggestedId);
                    savedInvoice = InvoiceModel.copyOf(invoice);
                    invoicesManager.get().add(savedInvoice);
                    break;
                case 1:
                    InvoiceModel.copyValues(invoice, existingInvoice);
                    savedInvoice = existingInvoice;
                    break;
                default:
                    return;
            }
        }

        TexCreatorHelper.createAsync(savedInvoice, createButton);

        List<ItemModel> activeItems = invoice.getItems().stream()
                .map(ClientItem::getItem)
                .filter(i -> !i.getItemName().isEmpty())
                .map(ItemModel::copyOf)
                .collect(Collectors.toList());
        boolean changed = Config.mergeLists(
                Config.itemsManager().get(),
                activeItems,
                i -> new Pair<>(i.getTva(), i.getItemName()),
                false);

        if (changed) {
            Config.saveSafe(Config.itemsManager());
        }

        setArguments(savedInvoice); // TODO: 11/4/15 A bit overkill
    }

    public void newInvoice() {
        InvoiceModel.copyValues(InvoiceHelper.createDefaultInvoice(), invoice);
    }

    public void editItems() {
        StageManager.show(Stages.ITEM_EDITOR);
    }

    public void invoices() {
        StageManager.show(Stages.INVOICES_ARCHIVE, Config.invoicesManager());
    }

    public void settings() {
        StageManager.show(Stages.SETTINGS);
    }

    public void quit() {
        StageManager.closeAll();
    }

    private boolean validateInvoice() {
        return forms.stream()
                .map(FormElement::animateIfInvalid)
                .reduce(true, (v1, v2) -> v1 && v2);
    }


    public void editCurrency() {
        StageManager.show(Stages.CURRENCY_SHORTCUT);
    }
}
