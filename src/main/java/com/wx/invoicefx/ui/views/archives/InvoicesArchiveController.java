package com.wx.invoicefx.ui.views.archives;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.backup.BackupManager;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.dataset.impl.InvoiceFxDataSet;
import com.wx.invoicefx.dataset.impl.event.ChangeEvent;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.model.ModelException;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.model.save.ModelSaver;
import com.wx.invoicefx.sync.PushPullSync;
import com.wx.invoicefx.tex.TexDocumentCreatorHelper;
import com.wx.invoicefx.ui.animation.Animator;
import com.wx.invoicefx.ui.components.NumberTextField;
import com.wx.invoicefx.ui.components.sort.SortLabel;
import com.wx.invoicefx.ui.components.sort.SortLabelGroup;
import com.wx.invoicefx.ui.components.suggestion.itemized.overlay.ItemizedSuggestionPaneController;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.invoicefx.util.DesktopUtils;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.util.concurrent.Callback;
import com.wx.util.future.IoIterator;
import com.wx.util.future.IoSupplier;
import com.wx.util.log.LogHelper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.pdfsam.ui.RingProgressIndicator;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wx.invoicefx.AppResources.getInvoiceFile;
import static com.wx.invoicefx.config.Places.Dirs.TEMP_DATA_DIR;
import static com.wx.invoicefx.config.preferences.local.LocalProperty.INVOICE_DIRECTORY;
import static com.wx.util.concurrent.ConcurrentUtil.executeAsync;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 13.05.17.
 */
public class InvoicesArchiveController implements StageController {

    private static final int DEFAULT_SORT_VALUE = -2;
    private static final Logger LOG = LogHelper.getLogger(InvoicesArchiveController.class);

    @FXML
    private BorderPane contentPane;
    @FXML
    private RingProgressIndicator progressIndicator;


    @FXML
    private Menu backupMenu;
    @FXML
    private MenuItem openDirectoryItem;
    @FXML
    private Pane typePane;
    @FXML
    private Label typeLabel;
    @FXML
    private TextField searchField;
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
    private NumberTextField sumLowerBoundField;
    @FXML
    private NumberTextField sumUpperBoundField;
    @FXML
    private ItemizedSuggestionPaneController<Client> clientSearchPaneController;
    @FXML
    private Pane advancedSearchPane;
    @FXML
    private ToggleButton advancedSearchButton;
    @FXML
    private ListView<Invoice> invoicesList;

    private final Observer dataChangedListener = (o, source) -> onDataSetChanged((Collection<ChangeEvent>) source);
    private final FilteredList<Invoice> filteredInvoices = new FilteredList<>(FXCollections.observableArrayList());
    private DataSet dataSet;
    private boolean readOnly = false;


    @Override
    public void setArguments(Object... args) {
        if (dataSet != null) {
            throw new IllegalStateException("Cannot set arguments more than once");
        }

        dataSet = Objects.requireNonNull((DataSet) args[0]);
        dataSet.addDataChangedListener(dataChangedListener);

        String dataType = dataSet.getProperty("type").orElseThrow(() -> new RuntimeException("Missing type property"));
        double dataSetVersion = dataSet.getIndex() == null ? 0.0 : dataSet.getIndex().getVersion();

        if (dataType.equals("local")) {
            ((VBox) typePane.getParent()).getChildren().remove(typePane);
        } else {
            typeLabel.setText(Lang.getString("stage.invoices_archive.label.viewing_" + dataType, dataSetVersion));
            typeLabel.getStyleClass().setAll("type-label-" + dataType);
            setReadOnly();
        }

        // BACKUP
        try {
            List<DataSet> backups = BackupManager.getAllBackups();
            if (backups.isEmpty()) {
                backupMenu.setDisable(true);
            } else {
                for (DataSet backup : backups) {
                    if (!backup.isCorrupted()) {
                        double version = backup.getIndex().getVersion();
                        Date lastModifiedDate = backup.getIndex().getLastModifiedDate();

                        String formattedDate = InvoiceFormats.formatDateTime(lastModifiedDate);
                        RadioMenuItem item = new RadioMenuItem(Lang.getString("stage.backup.label.backup", version, formattedDate));
                        if (dataType.equals("backup") && dataSetVersion == version) {
                            item.setSelected(true);
                        }
                        item.setOnAction(event -> {
                            StageManager.close(Stages.INVOICES_ARCHIVE);
                            StageManager.show(Stages.INVOICES_ARCHIVE, backup);
                        });

                        backupMenu.getItems().add(item);
                    }
                }
            }

        } catch (IOException e) {
            ExceptionLogger.logException(e);
            backupMenu.setDisable(true);
        }


        loadData();
    }

    @Override
    public void closing() {
        dataSet.removeDataChangedListener(dataChangedListener);
    }

    private void onDataSetChanged(Collection<ChangeEvent> sources) {
        if (sources.stream().anyMatch(e -> e.getType().equals(ChangeEvent.Type.MODEL))) {
            loadData();
        }
    }

    private void loadData() {
        if (dataSet.isCorrupted()) {
            Platform.runLater(() -> {
                filteredInvoices.getSource().clear();
                clientSearchPaneController.setEntries(Collections.emptyList());
            });
            return;
        }

        if (dataSet instanceof InvoiceFxDataSet) {
            loadModelSaver(((InvoiceFxDataSet) dataSet).getModelSaver());
        } else {
            LOG.info("Downloading data set to temporary location");
            showProgress();
            executeAsync(this::downloadDataWithRepo, new Callback<Object>() {
                @Override
                public Void success(Object o) {
                    return null;
                }

                @Override
                public Void failure(Throwable ex) {
                    ex.printStackTrace();
                    return null;
                }
            });
        }
    }

    private void loadModelSaver(ModelSaver modelSaver) {
        executeAsync(() -> {
//            List<Invoice> invoices = modelSaver.getAllInvoices().collect();
            List<Invoice> invoices = getInvoices(modelSaver);
            List<Client> clients = modelSaver.getAllClients().collect();

            Platform.runLater(() -> {
                ((ObservableList<Invoice>) filteredInvoices.getSource()).setAll(invoices);
                clientSearchPaneController.setEntries(clients);
            });
        }, new Callback<Object>() {
            @Override
            public Void success(Object o) {
                return null;
            }

            @Override
            public Void failure(Throwable e) {
                ExceptionLogger.logException(e);

                Platform.runLater(() -> {
                    StageManager.close(Stages.INVOICES_ARCHIVE);

                    int action = AlertBuilder.error(e)
                            .key("stage.invoices_archive.errors.load_fail")
                            .button("stage.common.button.retry")
                            .button("stage.invoices_archive.errors.load_fail.button.debug_view")
                            .button("stage.invoices_archive.errors.load_fail.button.backup_view")
                            .button("stage.invoices_archive.errors.load_fail.button.close")
                            .show();


                    switch (action) {
                        case 0:
                            StageManager.show(Stages.INVOICES_ARCHIVE, dataSet);
                            break;
                        case 1:
                            StageManager.show(Stages.DEBUG_VIEW, dataSet);
                            break;
                        case 2:
                            StageManager.show(Stages.BACK_UP);
                            break;
                        default:
                            break;
                    }
                });
                return null;

            }
        });
    }

    private List<Invoice> getInvoices(ModelSaver modelSaver) throws IOException {
        IoIterator<Invoice> it = modelSaver.getAllInvoices();

        List<Invoice> result = new ArrayList<>();
        while (it.hasNext()) {
            try {
                result.add(it.next());
            } catch (IOException e) {
                ExceptionLogger.logException(e);
            }
        }

        return result;
    }

    private void downloadDataWithRepo() throws IOException {
        InvoiceFxDataSet tmpDataSet = new InvoiceFxDataSet(Places.getDir(TEMP_DATA_DIR), "tmp");

        PushPullSync pushPullSync = new PushPullSync(tmpDataSet, dataSet);
        Platform.runLater(() -> progressIndicator.progressProperty().bind(pushPullSync.progressProperty().multiply(100.0)));

        pushPullSync.pullForce();

        loadModelSaver(tmpDataSet.getModelSaver());
        Platform.runLater(() -> {
            setReadOnly();
            hideProgress();
        });

    }

    private void setReadOnly() {
        removeButton.disableProperty().unbind();
        removeButton.setDisable(true);
        editButton.disableProperty().unbind();
        editButton.setDisable(true);
        openDirectoryItem.setDisable(true);
        readOnly = true;
    }

    private void showProgress() {
        progressIndicator.progressProperty().unbind();
        progressIndicator.setProgress(-1);

        contentPane.setVisible(false);
        progressIndicator.setVisible(true);
    }

    private void hideProgress() {
        progressIndicator.setVisible(false);
        contentPane.setVisible(true);
    }

    @FXML
    private void initialize() {
        progressIndicator.getStylesheets().add(StageManager.getStyleSheet());

        // SEARCH FIELD
        ObjectBinding<Predicate<Invoice>> listPredicate = new ObjectBinding<Predicate<Invoice>>() {
            @Override
            protected Predicate<Invoice> computeValue() {
                return InvoicesArchiveController.this::listPredicate;
            }
        };
        filteredInvoices.predicateProperty().bind(listPredicate);


        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            listPredicate.invalidate();
        });

        // ADVANCED SEARCH
        advancedSearchButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            listPredicate.invalidate();
            if (newValue) {
                Animator.instance().expandAnimation(advancedSearchPane, 120).run();
            } else {
                Animator.instance().collapseAnimation(advancedSearchPane).run();
            }
        });

        // ADVANCED SEARCH - Client search
        clientSearchPaneController.setToStringFn(Client::getName);
        clientSearchPaneController.getSelectedItems().addListener((ListChangeListener<Client>) c -> {
            Animator.instance().fadeOut(invoicesList)
                    .then(Animator.instance().fadeIn(invoicesList))
                    .then(listPredicate::invalidate)
                    .run();
        });

        // ADVANCED SEARCH - Date search
        ChangeListener<Object> updateListener = (observable, oldValue, newValue) -> {
            Animator.instance().fadeOut(invoicesList)
                    .then(Animator.instance().fadeIn(invoicesList))
                    .then(listPredicate::invalidate)
                    .run();
        };

        beforeDatePicker.disableProperty().bind(beforeDateBox.selectedProperty().not());

        beforeDateBox.selectedProperty().addListener(updateListener);
        beforeDatePicker.valueProperty().addListener(updateListener);

        afterDatePicker.disableProperty().bind(afterDateBox.selectedProperty().not());
        afterDateBox.selectedProperty().addListener(updateListener);
        afterDatePicker.valueProperty().addListener(updateListener);

        // ADVANCED SEARCH - Sum search
        NumberFormat numberFormat = InvoiceFormats.getNumberFormat("#0.00");
        NumberFormat customFormat = new NumberFormat() {

            @Override
            public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
                if (number == 0.0) return new StringBuffer();

                return numberFormat.format(number, toAppendTo, pos);
            }

            @Override
            public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
                if (number == 0) return new StringBuffer();

                return numberFormat.format(number, toAppendTo, pos);
            }

            @Override
            public Number parse(String source, ParsePosition parsePosition) {
                return numberFormat.parse(source, parsePosition);
            }
        };

        sumLowerBoundField.setNumberFormat(customFormat);
        sumUpperBoundField.setNumberFormat(customFormat);

        sumLowerBoundField.setDefaultToZero(true);
        sumUpperBoundField.setDefaultToZero(true);

        sumLowerBoundField.numberProperty().addListener(updateListener);
        sumUpperBoundField.numberProperty().addListener(updateListener);


        // SORT LABELS
        dateSortLabel.setComparator(Comparator.comparing(Invoice::getDate).thenComparing(Comparator.comparingLong(Invoice::getId)));
        idSortLabel.setComparator(Comparator.comparingLong(Invoice::getId));
        sumSortLabel.setComparator(Comparator.comparingDouble(Invoice::getSum));

        // INVOICES LIST
        invoicesList.setCellFactory(param -> new InvoiceListCell());
        invoicesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        SortedList<Invoice> sortedList = new SortedList<>(filteredInvoices);
        sortedList.comparatorProperty().bind(createSortGroup().comparatorProperty());
        invoicesList.setItems(sortedList);


        // ACTION BUTTONS
        IntegerBinding selectionSize = Bindings.size(invoicesList.getSelectionModel().getSelectedItems());
        openButton.disableProperty().bind(selectionSize.isNotEqualTo(1));
        editButton.disableProperty().bind(selectionSize.isNotEqualTo(1));
        removeButton.disableProperty().bind(selectionSize.isEqualTo(0));

    }

    public void openDirectoryExplorer() {
        if (readOnly) {
            throw new UnsupportedOperationException();
        }

        DesktopUtils.open(AppResources.localPreferences().getPath(INVOICE_DIRECTORY));
    }

    @FXML
    private void openAdvancedView() {
        StageManager.show(Stages.DEBUG_VIEW, dataSet);
    }

    @FXML
    private void close() {
        StageManager.close(Stages.INVOICES_ARCHIVE);
    }

    @FXML
    private void openInvoice() {
        Invoice selectedInvoice = getSelectedInvoice();

        if (readOnly) {
            createAndOpenInvoice(() -> TexDocumentCreatorHelper.createTmpDocument(selectedInvoice));
        } else {

            File pdf = AppResources.getInvoiceFile(selectedInvoice);

            if (pdf != null && pdf.exists()) {
                DesktopUtils.open(pdf);
            } else {
                LOG.warning("Missing PDF at " + pdf + ", regenerating...");
                createAndOpenInvoice(() -> TexDocumentCreatorHelper.createDocument(selectedInvoice, pdf));
            }
        }

    }

    private void createAndOpenInvoice(IoSupplier<File> pdfGenerator) {
        Task<Void> creationTask = new Task<Void>() {
            @Override
            protected Void call() {
                if (isCancelled()) {
                    return tearDown();
                }

                try {
                    File documentFile = pdfGenerator.get();
                    DesktopUtils.open(documentFile);

                } catch (IOException e) {
                    e.printStackTrace();
                    AlertBuilder.error(e)
                            .key("errors.tex")
                            .show();
                }

                return tearDown();
            }

            private Void tearDown() {
                Platform.runLater(() -> {
                    Animator.instance().restoreBusyButton(openButton, Lang.getString("stage.common.button.open")).run();
                });

                return null;
            }
        };

        Animator.instance().animateBusyButton(openButton, Lang.getString("stage.invoices_archive.button.open_creating")).run();
        new Thread(creationTask).start();
    }


    @FXML
    private void edit() {
        if (readOnly) {
            throw new UnsupportedOperationException();
        }

        StageManager.show(Stages.OVERVIEW, dataSet, getSelectedInvoice());
    }


    @FXML
    private void remove() {
        if (readOnly) {
            throw new UnsupportedOperationException();
        }

        ObservableList<Invoice> invoices = invoicesList.getSelectionModel().getSelectedItems();

        DecimalFormat idFormat = InvoiceFormats.idFormat();
        String invoicesId = invoices.stream().map(i -> idFormat.format(i.getId())).collect(Collectors.joining(", "));

        int choice = AlertBuilder.confirmation()
                .key("stage.invoices_archive.dialog.remove", invoices.size(), invoicesId)
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
            ((InvoiceFxDataSet) dataSet).getModelSaver().removeInvoices(invoices);
            filteredInvoices.getSource().removeAll(invoices);
        } catch (IOException e) {
            ExceptionLogger.logException(e);

            AlertBuilder.error(e)
                    .key("stage.invoices_archive.errors.remove")
                    .show();
        }

    }

    private Invoice getSelectedInvoice() {
        return invoicesList.getSelectionModel().getSelectedItem();
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

            double lowerBound = sumLowerBoundField.getNumber().doubleValue();
            double upperBound = sumUpperBoundField.getNumber().doubleValue();

            if (lowerBound > 0 && invoice.getSum() < lowerBound) {
                return false;
            }

            if (upperBound > 0 && invoice.getSum() > upperBound) {
                return false;
            }
        }

        String searchQuery = searchField.getText().trim().toLowerCase();
        if (!searchQuery.isEmpty()) {
            Set<String> matchingFields = new HashSet<>(Arrays.asList(
                    invoice.getAddress(),
                    InvoiceFormats.idFormat().format(invoice.getId()),
                    InvoiceFormats.dateConverter().toString(invoice.getDate())
            ));

            for (PurchaseGroup group : invoice.getPurchaseGroups()) {
                for (Client client : group.getClients()) {
                    matchingFields.add(client.getName());
                }

                for (Purchase purchase : group.getPurchases()) {
                    matchingFields.add(purchase.getItem().getName());
                }
            }

            String[] queries = searchQuery.split(" ");
            return Stream.of(queries).allMatch(query -> matchingFields.stream().map(f -> f.trim().toLowerCase()).anyMatch(f -> f.contains(query)));
        }

        return true;
    }


    private SortLabelGroup<Invoice> createSortGroup() {
        SortLabelGroup<Invoice> group = new SortLabelGroup<>();
        group.addLabel(idSortLabel);
        group.addLabel(dateSortLabel);
        group.addLabel(sumSortLabel);

        group.setSortValue(DEFAULT_SORT_VALUE);

        return group;
    }


    private class InvoiceListCell extends ListCell<Invoice> {

        @Override
        protected void updateItem(Invoice invoice, boolean empty) {
            super.updateItem(invoice, empty);

            if (invoice != null && !empty) {
                try {
                    this.setGraphic(loadInvoiceCell(invoice));
                } catch (IOException e) {
                    /* no-op */
                }
            } else {
                this.setGraphic(null);
            }
        }
    }

    private Pane loadInvoiceCell(Invoice invoice) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                InvoicesArchiveController.class.getResource("/com/wx/invoicefx/ui/views/archives/InvoiceListCell.fxml"),
                Lang.getBundle());

        Pane invoiceCell = loader.load();

        InvoiceListCellController controller = loader.getController();
        controller.setInvoice(invoice, advancedSearchButton.isSelected() ?
                clientSearchPaneController.getSelectedItems().stream().map(Client::getId).collect(Collectors.toSet()) :
                Collections.emptySet());

        return invoiceCell;
    }
}
