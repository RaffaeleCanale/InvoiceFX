package com.wx.invoicefx.view.archives;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.config.ConfigProperties;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.model.save.SaveManager;
import com.wx.invoicefx.util.DesktopUtils;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.invoicefx.view.Stages;
import com.wx.invoicefx.view.archives.debug.DebugViewController;
import com.wx.invoicefx.view.other.animation.Animator;
import com.wx.invoicefx.view.other.autocomplete.AutoCompletePaneController;
import com.wx.invoicefx.view.other.sort.SortLabel;
import com.wx.invoicefx.view.other.sort.SortLabelGroup;
import com.wx.util.log.LogHelper;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.wx.invoicefx.config.preferences.LocalProperty.INVOICE_DIRECTORY;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 13.05.17.
 */
public class InvoicesArchiveController implements StageController {

    private static final Logger LOG = LogHelper.getLogger(InvoicesArchiveController.class);

    @FXML
    private Button removeButton;
    @FXML
    private Button editButton;
    @FXML
    private Button openButton;
    @FXML
    private SortLabel<Invoice> dateSortLabel;
    @FXML
    private SortLabel<Invoice> idSortLabel;
    @FXML
    private SortLabel<Invoice> sumSortLabel;
    @FXML
    private CheckBox afterDateBox;
    @FXML
    private DatePicker afterDatePicker;
    @FXML
    private CheckBox beforeDateBox;
    @FXML
    private DatePicker beforeDatePicker;
    @FXML
    private TextField sumLowerBoundField;
    @FXML
    private TextField sumUpperBoundField;
    @FXML
    private AutoCompletePaneController<Client> clientSearchPaneController;
    @FXML
    private Pane advancedSearchPane;
    @FXML
    private ToggleButton advancedSearchButton;
    @FXML
    private ListView<Invoice> invoicesList;

    private SaveManager saveManager;
    private FilteredList<Invoice> filteredInvoices;

    @Override
    public void setArguments(Object... args) {
        this.saveManager = (SaveManager) args[0];

        try {
            loadInvoicesList();


            ObservableList<Client> clients = FXCollections.observableArrayList(saveManager.getAllClients().collect());
            clientSearchPaneController.addEntries(clients);


        } catch (IOException e) {
            ExceptionLogger.logException(e);

            StageManager.close(Stages.INVOICES_ARCHIVE);

            int action = AlertBuilder.error(e)
                    .key("stage.invoices_archive.errors.load_fail")
                    .button("stage.invoices_archive.errors.load_fail.button.debug_view")
                    .button("stage.invoices_archive.errors.load_fail.button.backup_view")
                    .button("stage.invoices_archive.errors.load_fail.button.close")
                    .show();


            if (action == 0) {
                StageManager.show(Stages.DEBUG_VIEW);
            } else if (action == 1) {
                // TODO: 13.05.17 Implement this
                throw new UnsupportedOperationException();
            } else if (action == 2) {
                // TODO: 13.05.17 Implement this
                throw new UnsupportedOperationException();
            }

        }
    }


    @Override
    public void setContext(Stage stage) {
        // SEARCH FIELD
        // TODO: 14.05.17 Search

        // ADVANCED SEARCH
        advancedSearchButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateListPredicate();
            if (newValue) {
                Animator.instance().expandAnimation(advancedSearchPane, 120);
            } else {
                Animator.instance().collapseAnimation(advancedSearchPane);
            }
        });

        // ADVANCED SEARCH - Client search
        clientSearchPaneController.setToStringFn(Client::getName);
        clientSearchPaneController.getSelectedItems().addListener((ListChangeListener<Client>) c -> {
            updateListPredicate();
        });

        // ADVANCED SEARCH - Date search
        ChangeListener updateListener = (observable, oldValue, newValue) -> updateListPredicate();

        beforeDatePicker.disableProperty().bind(beforeDateBox.selectedProperty().not());

        beforeDateBox.selectedProperty().addListener(updateListener);
        beforeDatePicker.valueProperty().addListener(updateListener);

        afterDatePicker.disableProperty().bind(afterDateBox.selectedProperty().not());
        afterDateBox.selectedProperty().addListener(updateListener);
        afterDatePicker.valueProperty().addListener(updateListener);

        // ADVANCED SEARCH - Sum search
        // TODO: 14.05.17 Sum search

        // SORT LABELS
        dateSortLabel.setBaseLabel(dateSortLabel.getText());
        dateSortLabel.setComparator(Comparator.comparing(Invoice::getDate));

        idSortLabel.setBaseLabel(idSortLabel.getText());
        idSortLabel.setComparator(Comparator.comparing(Invoice::getId));

        sumSortLabel.setBaseLabel(sumSortLabel.getText());
        sumSortLabel.setComparator(Comparator.comparing(Invoice::getSum));

        // INVOICES LIST
        invoicesList.setCellFactory(param -> new InvoiceListCell());
        invoicesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        // ACTION BUTTONS
        IntegerBinding selectionSize = Bindings.size(invoicesList.getSelectionModel().getSelectedItems());
        openButton.disableProperty().bind(selectionSize.isNotEqualTo(1));
        editButton.disableProperty().bind(selectionSize.isNotEqualTo(1));
        removeButton.disableProperty().bind(selectionSize.isEqualTo(0));
    }

    public void openDirectory() {
        DesktopUtils.open(getInvoiceDirectory());
    }

    public void advancedView() {
        StageManager.show(Stages.DEBUG_VIEW);
    }

    public void close() {
        StageManager.close(Stages.INVOICES_ARCHIVE);
    }

    public void open() {
        Invoice selectedInvoice = getSelectedInvoice();
        File pdf = getInvoiceFile(selectedInvoice);

        if (pdf != null && pdf.exists()) {
            DesktopUtils.open(pdf);
        } else {
            LOG.warning("Missing PDF at " + pdf + ", regenerating...");
//            TexCreatorHelper.createAsync(selectedInvoice, openButton);
            // TODO: 12.05.17 Implement this
        }
    }

    public void edit() {
        StageManager.show(Stages.OVERVIEW, getSelectedInvoice());
    }



    public void remove() {
        ObservableList<Invoice> invoices = invoicesList.getSelectionModel().getSelectedItems();

        int choice = AlertBuilder.confirmation()
                .key("???confirmation.remove_confirmation") // TODO: 14.05.17 Here
                .show();

        if (choice != 0) {
            return;
        }

        for (Invoice invoice : invoices) {
            File pdf = getInvoiceFile(invoice);
            if (pdf != null && pdf.exists()) {
                pdf.delete();
            }
        }

        try {
            saveManager.removeInvoices(invoices);
            filteredInvoices.getSource().removeAll(invoices);
        } catch (IOException e) {
            // TODO: 12.05.17 Error handling
        }

    }

    private Invoice getSelectedInvoice() {
        return invoicesList.getSelectionModel().getSelectedItem();
    }

    private void updateListPredicate() {
        Animator.instance().fadeInOut(invoicesList, () -> filteredInvoices.setPredicate(this::listPredicate));
    }

    private boolean listPredicate(Invoice invoice) {

        if (advancedSearchButton.isSelected()) {
            Set<Long> invoiceClients = invoice.getPurchaseGroups().stream()
                    .flatMap(purchaseGroup -> purchaseGroup.getClients().stream())
                    .map(Client::getId)
                    .collect(Collectors.toSet());
            Set<Long> selected = clientSearchPaneController.getSelectedItems().stream().map(Client::getId).collect(Collectors.toSet());

            if (!invoiceClients.containsAll(selected)) {
                return false;
            }

            if (afterDateBox.isSelected()) {
                LocalDate afterDate = afterDatePicker.getValue();
                if (afterDate != null && !invoice.getDate().isAfter(afterDate)) {
                    return false;
                }
            }
            if (beforeDateBox.isSelected()) {
                LocalDate beforeDate = beforeDatePicker.getValue();
                if (beforeDate != null && !invoice.getDate().isBefore(beforeDate)) {
                    return false;
                }
            }

            // TODO: 13.05.17 Add sums check
        }


        return true;
    }



    private void loadInvoicesList() throws IOException {
        ObservableList<Invoice> invoices = FXCollections.observableArrayList(saveManager.getAllInvoices().collect());
        filteredInvoices = new FilteredList<>(invoices);
        SortedList<Invoice> sortedList = new SortedList<>(filteredInvoices);
        sortedList.comparatorProperty().bind(createSortGroup().comparatorProperty());
        invoicesList.setItems(sortedList);
    }

    private SortLabelGroup<Invoice> createSortGroup() {
        SortLabelGroup<Invoice> group = new SortLabelGroup<>();
        group.addLabel(idSortLabel);
        group.addLabel(dateSortLabel);
        group.addLabel(sumSortLabel);

        return group;
    }

    private File getInvoiceFile(Invoice invoice) {
        String pdfName = invoice.getPdfFilepath();
        if (pdfName == null) {
            return null;
        }
        return new File(getInvoiceDirectory(), pdfName);
    }

    private File getInvoiceDirectory() {
        return ConfigProperties.localPreferences().getPath(INVOICE_DIRECTORY);
    }



    private class InvoiceListCell extends ListCell<Invoice> {

        @Override
        protected void updateItem(Invoice invoice, boolean empty) {
            super.updateItem(invoice, empty);

            if (invoice != null && !empty) {
                try {
                    this.setGraphic(loadInvoiceCell(invoice));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                this.setGraphic(null);
            }
        }
    }

    private Pane loadInvoiceCell(Invoice invoice) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                DebugViewController.class.getResource("/com/wx/invoicefx/view/archives/InvoiceListCell.fxml"),
                Lang.getBundle());

        Pane invoiceCell = loader.load();

        InvoiceListCellController controller = loader.getController();
        controller.setInvoice(invoice, advancedSearchButton.isSelected() ?
                clientSearchPaneController.getSelectedItems().stream().map(Client::getId).collect(Collectors.toSet()) :
                Collections.emptySet());

        return invoiceCell;
    }
}
