package com.wx.invoicefx.ui.views.item;

import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.dataset.impl.InvoiceFxDataSet;
import com.wx.invoicefx.dataset.impl.event.ChangeEvent;
import com.wx.invoicefx.dataset.impl.event.ModelEvent;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.entities.ModelComparator;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.save.ModelSaver;
import com.wx.invoicefx.ui.animation.Animator;
import com.wx.invoicefx.ui.animation.AnimatorInterface;
import com.wx.invoicefx.ui.components.NumberTextField;
import com.wx.invoicefx.ui.components.google.RemoteStatusPaneController;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.invoicefx.ui.views.overview.OverviewController;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.invoicefx.util.view.FormattedTableFactory;
import com.wx.util.concurrent.Callback;
import com.wx.util.concurrent.ConcurrentUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.17.
 */
public class ItemsEditorController implements StageController {


    @FXML
    private Button editButton;
    @FXML
    private Button removeButton;
    @FXML
    private HBox editControlsPane;
    @FXML
    private HBox defaultControlsPane;
    @FXML
    private Pane itemEditPanel;
    @FXML
    private TextField itemNameField;
    @FXML
    private NumberTextField itemPriceField;
    @FXML
    private ChoiceBox<Vat> itemVatChoiceBox;
    @FXML
    private TableView<Item> itemsTable;
    @FXML
    private RemoteStatusPaneController remoteStatusPaneController;

    private final Observer dataChangedListener = (o, source) -> onDataSetChanged((Collection<ChangeEvent>) source);

    private boolean isDefaultMode = true;
    private Item editingItem;

    private InvoiceFxDataSet dataSet;

    @Override
    public void setArguments(Object... args) {
        dataSet = Objects.requireNonNull((InvoiceFxDataSet) args[0]);
        dataSet.addDataChangedListener(dataChangedListener);


        loadItems();
    }

    private void loadItems() {
        ConcurrentUtil.executeAsync(() -> dataSet.getModelSaver().getAllActiveItems().collect(), new Callback<List<Item>>() {
            @Override
            public Void success(List<Item> items) {
                Platform.runLater(() -> itemsTable.setItems(FXCollections.observableArrayList(items)));
                return null;
            }

            @Override
            public Void failure(Throwable e) {
                Platform.runLater(() -> {
                    ExceptionLogger.logException(e);

                    StageManager.close(Stages.ITEMS_EDITOR);

                    AlertBuilder.error(e)
                            .key("stage.invoices_archive.errors.load_fail")
                            .show();

                });
                return null;
            }
        });
    }

    @FXML
    private void initialize() {
        // TABLE
        itemsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // TABLE COLUMNS
        getNameColumn().setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Item, Double> priceColumn = getPriceColumn();
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setCellFactory(new FormattedTableFactory<>(InvoiceFormats.moneyFormat()::format));

        TableColumn<Item, Vat> vatColumn = getVatColumn();
        FormattedTableFactory<Item, Vat> vatFormat = new FormattedTableFactory<>(v -> InvoiceFormats.vatFormat().format(v.getValue()));

        vatColumn.setCellValueFactory(new PropertyValueFactory<>("vat"));
        vatColumn.setCellFactory(vatFormat);
        vatFormat.setCellAlignment(Pos.CENTER_RIGHT);

        // ITEM NAME
        OverviewController.addInvalidRemovalListener(itemNameField);

        // ITEM PRICE
        itemPriceField.setNumberFormat(InvoiceFormats.moneyFormat());

        // ITEM VAT
        ObservableList<Vat> vats = FXCollections.observableArrayList(AppResources.getAllVats().values());
        itemVatChoiceBox.setItems(vats);
        itemVatChoiceBox.setConverter(new StringConverter<Vat>() {
            @Override
            public String toString(Vat vat) {
                return InvoiceFormats.vatFormat().format(vat.getValue());
            }

            @Override
            public Vat fromString(String string) {
                throw new UnsupportedOperationException("not used");
            }
        });

        // CONTROL BUTTONS
        IntegerBinding selectionSize = Bindings.size(itemsTable.getSelectionModel().getSelectedItems());
        editButton.disableProperty().bind(selectionSize.isNotEqualTo(1));
        removeButton.disableProperty().bind(selectionSize.isEqualTo(0));
    }

    @FXML
    private void addItem() {
        showItemEditControls(createNewItem());
    }

    @FXML
    private void editItem() {
        showItemEditControls(itemsTable.getSelectionModel().getSelectedItem());
    }

    @Override
    public void closing() {
        remoteStatusPaneController.onRemove();
        dataSet.removeDataChangedListener(dataChangedListener);
    }

    private void onDataSetChanged(Collection<ChangeEvent> sources) {
        if (sources.stream().anyMatch(e ->
                e.getType() == ChangeEvent.Type.MODEL && ((ModelEvent) e).isItemChange())) {
            loadItems();
        }
    }

    @FXML
    private void removeItem() {
        ObservableList<Item> itemsToRemove = itemsTable.getSelectionModel().getSelectedItems();

        try {
            dataSet.getModelSaver().removeOrDisable(itemsToRemove);

            itemsTable.getItems().removeAll(itemsToRemove);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void editConfirm() {
        Item newItem = buildNewItem();

        if (newItem.getName().isEmpty()) {
            Animator.instance().setInvalid(Collections.singleton(itemNameField)).run();
            return;
        }

        ModelSaver modelSaver = dataSet.getModelSaver();

        if (newItem.getId() > 0) {
            // EDIT
            if (ModelComparator.contentEquals(newItem, editingItem)) {
                return;
            }

            try {

                if (modelSaver.isReferenced(editingItem)) {
                    editingItem.setActive(false);
                    modelSaver.updateItem(editingItem);

                    newItem.setId(0);
                    modelSaver.addItem(newItem);
                } else {
                    modelSaver.updateItem(newItem);
                }

                itemsTable.getItems().remove(editingItem);
                itemsTable.getItems().add(newItem);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            // ADD
            try {
                modelSaver.addItem(newItem);

                itemsTable.getItems().add(newItem);
            } catch (IOException e) {
                ExceptionLogger.logException(e);
                AlertBuilder.error(e)
                        .key("stage.overview.dialog.save_error")
                        .show();
            }
        }

        showDefaultControls();
    }

    @FXML
    private void editCancel() {
        showDefaultControls();
    }

    @FXML
    private void onClose() {
        StageManager.close(Stages.ITEMS_EDITOR);
    }


    private Item buildNewItem() {
        Item item = new Item();

        item.setId(editingItem.getId());
        item.setDefaultDateEnabled(editingItem.getDefaultDateEnabled());
        item.setActive(editingItem.isActive());

        item.setName(itemNameField.getText());
        item.setPrice(itemPriceField.getNumber().doubleValue());
        item.setVat(itemVatChoiceBox.getValue());

        return item;
    }

    private Item createNewItem() {
        Item item = new Item();
        item.setId(-1);
        item.setName("");
        item.setPrice(0.0);
        item.setVat(itemVatChoiceBox.getItems().get(0));
        item.setDefaultDateEnabled(DateEnabled.BOTH);
        item.setActive(true);

        return item;
    }


    private void showItemEditControls(Item item) {
        if (!isDefaultMode) return;

        AnimatorInterface animator = Animator.instance();
        animator.expandAnimation(itemEditPanel, itemEditPanel.getPrefHeight()).run();
        animator.fadeOut(defaultControlsPane)
                .then(animator.fadeIn(editControlsPane))
                .run();

        defaultControlsPane.setMouseTransparent(true);
        editControlsPane.setMouseTransparent(false);

        itemsTable.setDisable(true);

        isDefaultMode = false;
        editingItem = item;

        itemNameField.setText(item.getName());
        itemPriceField.setNumber(item.getPrice());
        itemVatChoiceBox.setValue(item.getVat());
    }

    private void showDefaultControls() {
        if (isDefaultMode) return;

        AnimatorInterface animator = Animator.instance();
        animator.collapseAnimation(itemEditPanel).run();
        animator.fadeOut(editControlsPane)
                .then(animator.fadeIn(defaultControlsPane))
                .run();

        defaultControlsPane.setMouseTransparent(false);
        editControlsPane.setMouseTransparent(true);

        itemsTable.setDisable(false);

        isDefaultMode = true;
        editingItem = null;
    }


    @SuppressWarnings("unchecked")
    private TableColumn<Item, String> getNameColumn() {
        return (TableColumn<Item, String>) itemsTable.getColumns().get(0);
    }

    @SuppressWarnings("unchecked")
    private TableColumn<Item, Double> getPriceColumn() {
        return (TableColumn<Item, Double>) itemsTable.getColumns().get(1);
    }

    @SuppressWarnings("unchecked")
    private TableColumn<Item, Vat> getVatColumn() {
        return (TableColumn<Item, Vat>) itemsTable.getColumns().get(2);
    }


}
