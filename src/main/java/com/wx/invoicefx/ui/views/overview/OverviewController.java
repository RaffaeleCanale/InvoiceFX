package com.wx.invoicefx.ui.views.overview;


import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.dataset.impl.InvoiceFxDataSet;
import com.wx.invoicefx.dataset.impl.event.ChangeEvent;
import com.wx.invoicefx.dataset.impl.event.ModelEvent;
import com.wx.invoicefx.dataset.impl.event.PreferencesEvent;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.entities.item.Vats;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.model.save.ModelSaver;
import com.wx.invoicefx.tex.TexDocumentCreatorHelper;
import com.wx.invoicefx.ui.animation.Animator;
import com.wx.invoicefx.ui.components.NumberTextField;
import com.wx.invoicefx.ui.components.suggestion.textfield.SuggestionTextArea;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.invoicefx.util.DesktopUtils;
import com.wx.invoicefx.util.concurrent.LazyCallback;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.util.concurrent.ConcurrentUtil;
import com.wx.util.pair.Pair;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static com.wx.invoicefx.config.preferences.shared.SharedProperty.VAT;

/**
 * Created on 30/06/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class OverviewController implements StageController {

    public static void addInvalidRemovalListener(Node node) {
        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Animator.instance().removeInvalid(node).run();
            }
        });
    }

    @FXML
    private Pane buttonsPane;
    @FXML
    private SuggestionTextArea<String> addressTextArea;
    @FXML
    private DatePicker datePicker;
    @FXML
    private NumberTextField idTextField;
    @FXML
    private VBox itemsPane;
    @FXML
    private Button createButton;
    @FXML
    private Pane vatWarningPane;

    private final Observer modelSaverObserver = (o, source) -> onDataSetChanged((Collection<ChangeEvent>) source);
    private final Map<Vat, SortedMap<String, Item>> itemEntries = new HashMap<>(); // TODO: 06.06.17 Should these be placed elsewhere?
    private final SortedMap<String, Client> clientEntries = new TreeMap<>();
    private final SortedMap<String, String> addressEntries = new TreeMap<>();

    private InvoiceFxDataSet dataSet;

    @Override
    public void setArguments(Object... args) {
        InvoiceFxDataSet newDataSet = (InvoiceFxDataSet) Objects.requireNonNull(args[0]);

        if (dataSet != newDataSet) {
            if (dataSet != null) {
                dataSet.removeDataChangedListener(modelSaverObserver);
            }

            dataSet = newDataSet;
            dataSet.addDataChangedListener(modelSaverObserver);
            loadAutoCompleteData(true, true, true);
        }

        if (args.length > 1) {
            loadInvoice((Invoice) args[1]);
        } else {
            loadEmptyInvoice();
        }
    }

    @Override
    public void closing() {
        dataSet.removeDataChangedListener(modelSaverObserver);
    }

    private void onDataSetChanged(Collection<ChangeEvent> sources) {
        for (ChangeEvent change : sources) {
            if (change.getType() == ChangeEvent.Type.MODEL) {
                ModelEvent modelChange = (ModelEvent) change;

                loadAutoCompleteData(modelChange.isItemChange(), modelChange.isClientChange(), modelChange.isInvoiceChange());
            } else if (change.getType() == ChangeEvent.Type.PREFERENCES) {
                PreferencesEvent preferencesChange = (PreferencesEvent) change;

                if (preferencesChange.getKey().equals(Optional.of(VAT))) {
                    Platform.runLater(this::updateVatButtons);
                }
            }
        }
    }

    private void loadAutoCompleteData(boolean loadItems, boolean loadClients, boolean loadAddresses) {
        if (!loadItems && !loadClients) {
            return;
        }

        ModelSaver modelSaver = dataSet.getModelSaver();

        ConcurrentUtil.executeAsync(() -> {
            if (loadItems) loadItems(modelSaver);
            if (loadClients) loadClients(modelSaver);
            if (loadAddresses) loadAddresses(modelSaver);

            Platform.runLater(() -> {
                for (Node itemPane : itemsPane.getChildren()) {
                    ItemPanelController controller = (ItemPanelController) itemPane.getUserData();
                    controller.setAutoCompleteItems(itemEntries.getOrDefault(controller.getVat(), Collections.emptySortedMap()), clientEntries);
                }

                addressTextArea.setEntries(addressEntries);
            });

        }, ConcurrentUtil.NO_OP);
    }

    private void loadClients(ModelSaver modelSaver) throws IOException {
        clientEntries.clear();
        List<Client> clients = modelSaver.getAllClients().collect();
        for (Client client : clients) {
            clientEntries.put(client.getName().trim().toLowerCase(), client);
        }
    }

    private void loadItems(ModelSaver modelSaver) throws IOException {
        itemEntries.clear();
        List<Item> items = modelSaver.getAllActiveItems().collect(); // TODO: 08.06.17 Filter? Limit?
        for (Item item : items) {
            itemEntries.putIfAbsent(item.getVat(), new TreeMap<>());
            itemEntries.get(item.getVat()).put(item.getName().trim().toLowerCase(), item);
        }
    }

    private void loadAddresses(ModelSaver modelSaver) throws IOException {
        addressEntries.clear();

        List<String> addresses = modelSaver.getAllAddresses().collect();
        for (String address : addresses) {
            addressEntries.put(address, address);
        }
    }

    @FXML
    private void loadEmptyInvoice() {
        addressTextArea.setText("");
        idTextField.setNumber(dataSet.getModelSaver().getNextInvoiceId());
        datePicker.setValue(LocalDate.now());

        itemsPane.getChildren().clear();

        Vats allVats = AppResources.getAllVats();
        addItemPane(allVats.values().iterator().next(), false);
    }

    private void loadInvoice(Invoice invoice) {
        itemsPane.getChildren().clear();

        addressTextArea.setText(invoice.getAddress());
        idTextField.setNumber(invoice.getId());
        datePicker.setValue(invoice.getDate());

        for (PurchaseGroup group : invoice.getPurchaseGroups()) {
            Iterator<Purchase> purchasesIt = group.getPurchases().iterator();

            Purchase firstPurchase = purchasesIt.next();

            ItemPanelController controller = addItemPane(firstPurchase.getItem().getVat(), false);

            controller.loadPurchase(firstPurchase);
            controller.loadClients(group.getClients(), group.getStopWords());

            while (purchasesIt.hasNext()) {
                Purchase purchase = purchasesIt.next();

                addItemPane(purchase.getItem().getVat(), false)
                        .loadPurchase(purchase);
            }
        }
    }

    @FXML
    private void initialize() {
        // ADDRESS
        addInvalidRemovalListener(addressTextArea);
        addressTextArea.setItemConsumer(address -> addressTextArea.setTextAndMoveCaret(address));

        // DATE
        datePicker.setConverter(InvoiceFormats.dateConverter());
        datePicker.setValue(LocalDate.now());

        addInvalidRemovalListener(datePicker);

        // ID
        idTextField.setNumberFormat(InvoiceFormats.idFormat());

        addInvalidRemovalListener(idTextField);


        // BUTTONS
        updateVatButtons();

        // VAT WARN
        itemsPane.getChildren().addListener((ListChangeListener<Node>) c -> updateVatWarning());
    }

    private void updateVatWarning() {
        Vats vats = AppResources.getAllVats();
        boolean warn = itemsPane.getChildren().stream()
                .anyMatch(itemPane -> {
                    ItemPanelController controller = (ItemPanelController) itemPane.getUserData();
                    return !vats.contains(controller.getVat());
                });


        vatWarningPane.setManaged(warn);
        vatWarningPane.setVisible(warn);
    }

    private ItemPanelController addItemPane(Vat vat, boolean animate) {
        FXMLLoader loader = new FXMLLoader(
                OverviewController.class.getResource("/com/wx/invoicefx/ui/views/overview/ItemPanel.fxml"),
                Lang.getBundle());

        try {
            VBox itemPane = loader.load();
            HBox.setHgrow(itemPane, Priority.ALWAYS);

            ItemPanelController typeController = loader.getController();

            typeController.setAutoCompleteItems(itemEntries.getOrDefault(vat, Collections.emptySortedMap()), clientEntries);
            typeController.setVat(vat);

            typeController.getRemoveButton().setOnAction(e -> {
                Animator.instance().collapseAnimation(itemPane)
                        .then(() -> itemsPane.getChildren().remove(itemPane))
                        .run();
            });


            itemPane.setUserData(typeController);

            itemsPane.getChildren().add(itemPane);
            if (animate) Animator.instance().expandAnimation(itemPane, itemPane.getPrefHeight()).run();

            return typeController;

        } catch (IOException e) {
            ExceptionLogger.logException(e);
            throw new RuntimeException(e);
        }
    }

    private void updateVatButtons() {
        Vats vats = AppResources.getAllVats();

        buttonsPane.getChildren().clear();
        for (Vat vat : vats.values()) {
            buttonsPane.getChildren().add(createVatButton(vat));
        }
    }

    private Button createVatButton(Vat vat) {
        Button button = new Button(Lang.getString("stage.overview.button.vat", vat.getValue()));
        button.getStyleClass().add("custom-button");

        button.setOnAction(e -> addItemPane(vat, true));

        return button;
    }


    @FXML
    private void createInvoice() {
        Set<Node> invalidNodes = getInvalidNodes();

        if (!invalidNodes.isEmpty()) {
            Animator.instance().setInvalid(invalidNodes).run();
            return;
        }

        Invoice invoice = buildInvoice();
        try {
            ModelSaver modelSaver = dataSet.getModelSaver();
            if (modelSaver.invoiceExists(invoice.getId())) {
                long suggestedId = modelSaver.getNextInvoiceId();
                int choice = AlertBuilder.warning()
                        .key("stage.overview.dialog.existing_invoice", invoice.getId(), suggestedId)
                        .button("stage.overview.dialog.existing_invoice.create_new")
                        .button("stage.overview.dialog.existing_invoice.update_old")
                        .button(ButtonType.CANCEL)
                        .show();

                switch (choice) {
                    case 0:
                        invoice.setId(suggestedId);
                        break;
                    case 1:
                        modelSaver.removeInvoice(invoice.getId());
                        break;
                    default:
                        return;
                }
            }


            createAndOpenInvoice(invoice);


        } catch (IOException e) {
            // TODO: 06.07.17 Load and repair
            ExceptionLogger.logException(e);
            AlertBuilder.error(e)
                    .key("stage.overview.dialog.save_error")
                    .show();
        }
    }

    private void createAndOpenInvoice(Invoice invoice) {
        Animator.instance().animateBusyButton(createButton, Lang.getString("stage.overview.button.creating")).run();

        ConcurrentUtil.executeAsync(() -> {
            File documentFile = TexDocumentCreatorHelper.createDocument(invoice, AppResources.getInvoiceFile(invoice));
            dataSet.getModelSaver().addInvoice(invoice);

            DesktopUtils.open(documentFile);
        }, (LazyCallback<Object>) (ex, r) -> {
            Platform.runLater(() -> {
                Animator.instance().restoreBusyButton(createButton, Lang.getString("stage.overview.button.create")).run();

                if (ex != null) {
                    ExceptionLogger.logException(ex);
                    AlertBuilder.error(ex)
                            .key("stage.overview.dialog.save_error")
                            .show();
                }
            });
            return null;
        });
    }

    private Invoice buildInvoice() {
        Invoice invoice = new Invoice();

        invoice.setId(idTextField.getNumber().longValue());
        invoice.setAddress(addressTextArea.getText());
        invoice.setDate(datePicker.getValue());

        for (Node itemPane : itemsPane.getChildren()) {
            ItemPanelController controller = (ItemPanelController) itemPane.getUserData();

            Pair<List<Client>, List<String>> clients = controller.buildClients();
            Purchase purchase = controller.buildPurchase();


            PurchaseGroup group;

            if (clients.get1().isEmpty() && invoice.getPurchaseGroups().size() == 0) {
                group = new PurchaseGroup();
                group.setClients(Collections.singletonList(Client.EMPTY_CLIENT));

                invoice.getPurchaseGroups().add(group);
            } else if (clients.get1().isEmpty()) {
                group = invoice.getPurchaseGroups().get(invoice.getPurchaseGroups().size() - 1);
            } else {
                group = new PurchaseGroup();
                group.setClients(clients.get1());
                group.setStopWords(clients.get2());

                invoice.getPurchaseGroups().add(group);
            }

            group.getPurchases().add(purchase);
        }

        invoice.setPdfFilename(InvoiceFormats.idFormat().format(invoice.getId()) + ".pdf");

        return invoice;
    }


    @FXML
    private void editItems() {
        StageManager.show(Stages.ITEMS_EDITOR, dataSet);
    }

    @FXML
    private void openInvoicesArchive() {
        StageManager.show(Stages.INVOICES_ARCHIVE, dataSet);
    }

    @FXML
    private void settings() {
        StageManager.show(Stages.SETTINGS);
    }

    @FXML
    private void quit() {
        StageManager.closeAll();
    }

    @FXML
    private void openBackups() {
        StageManager.show(Stages.BACK_UP);
    }

    private Set<Node> getInvalidNodes() {
        Set<Node> nodes = new HashSet<>();

        // ADDRESS
        if (addressTextArea.getText().trim().isEmpty()) {
            nodes.add(addressTextArea);
        }

        // ID
        if (idTextField.getNumber().longValue() <= 0) {
            nodes.add(idTextField);
        }

        // DATE
        if (datePicker.getValue() == null) {
            nodes.add(datePicker);
        }

        // ITEMS
        boolean first = true;
        for (Node itemPane : itemsPane.getChildren()) {
            ItemPanelController controller = (ItemPanelController) itemPane.getUserData();

            nodes.addAll(controller.getInvalidNodes(true));

            first = false;
        }

        return nodes;
    }


    @FXML
    private void updateItemsVats() {
        Vats vats = AppResources.getAllVats();

        for (Node itemPane : itemsPane.getChildren()) {
            ItemPanelController controller = (ItemPanelController) itemPane.getUserData();

            Vat oldVat = controller.getVat();
            Optional<Vat> newVat = vats.getVat(oldVat.getCategory());
            if (!newVat.isPresent()) {
                throw new RuntimeException("No vat for category: " + oldVat.getCategory());
            }

            controller.setVat(newVat.get());
        }

        updateVatWarning();
    }
}
