package com.wx.invoicefx.view.archives;


import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.config.ConfigProperties;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.save.SaveManager;
import com.wx.invoicefx.util.DesktopUtils;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.invoicefx.util.view.FormatFactory;
import com.wx.invoicefx.view.Stages;
import com.wx.util.log.LogHelper;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.wx.invoicefx.config.ConfigProperties.localPreferences;
import static com.wx.invoicefx.config.ConfigProperties.sharedPreferences;
import static com.wx.invoicefx.config.preferences.LocalProperty.*;
import static com.wx.invoicefx.config.preferences.SharedProperty.ARCHIVES_DEFAULT_ACTION;


/**
 * Created on 10/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class InvoicesArchiveControllerOld implements StageController {

    private static final Logger LOG = LogHelper.getLogger(InvoicesArchiveControllerOld.class);

    @FXML
    private WebView invoiceView;
    @FXML
    private ToggleButton invoiceViewToggle;
    @FXML
    private SplitPane splitPane;
    @FXML
    private TableView<Invoice> invoicesTable;
    @FXML
    private Button openButton;
    @FXML
    private Button editButton;
    @FXML
    private Button removeButton;
    @FXML
    private TextField searchField;
    @FXML
    private ToggleGroup defaultActionGroup;

    private SaveManager manager;
    private FilteredList<Invoice> filteredInvoices;

    private Runnable defaultAction;

    @Override
    public void setArguments(Object... args) {
        this.manager = (SaveManager) args[0];

        try {
            ObservableList<Invoice> invoices = FXCollections.observableArrayList(manager.getAllInvoices().collect());
            filteredInvoices = new FilteredList<>(invoices);
            SortedList<Invoice> sortedList = new SortedList<>(filteredInvoices);
            sortedList.comparatorProperty().bind(invoicesTable.comparatorProperty());
            invoicesTable.setItems(sortedList);


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
            }
        }

    }

    @Override
    public void setContext(Stage stage) {
        // DEFAULT ACTION
        defaultActionGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            int index = defaultActionGroup.getToggles().indexOf(newValue);
            sharedPreferences().setProperty(ARCHIVES_DEFAULT_ACTION, index);
            setDefaultAction(index);
        });

        int index = sharedPreferences().getInt(ARCHIVES_DEFAULT_ACTION);
        setDefaultAction(index);
        defaultActionGroup.getToggles().get(index).setSelected(true);


        // SEARCH FIELD
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                filteredInvoices.setPredicate(null);
            } else {
//                filteredInvoices.setPredicate(i -> InvoiceFormats.queryContains(i, newValue));
            }
        });
        searchField.setDisable(true); // TODO: 11.05.17 Implement this


        // VIEW TOGGLE
        BooleanProperty viewEnabled = localPreferences().booleanProperty(ARCHIVES_VIEW_ENABLED);
        DoubleProperty viewSplitter = localPreferences().doubleProperty(ARCHIVES_VIEW_SPLITTER);
        DoubleProperty dividerPosition = splitPane.getDividers().get(0).positionProperty();

        invoiceViewToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
            private double lastValue = viewSplitter.getValue();


            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean viewEnabled) {
                if (viewEnabled) {
                    splitPane.setId(null);
                    dividerPosition.setValue(lastValue);

                    viewSplitter.bind(dividerPosition);
                } else {
                    viewSplitter.unbind();

                    splitPane.setId("hidden_divider");
                    lastValue = dividerPosition.get();
                    dividerPosition.set(1.0);
                }
            }
        });
        dividerPosition.set(viewSplitter.get());
        invoiceViewToggle.selectedProperty().bindBidirectional(viewEnabled);
        if (!viewEnabled.get()) {
            splitPane.setId("hidden_divider");
            dividerPosition.set(1.0);
        }


        // INVOICES TABLE
        invoicesTable.getSelectionModel().selectionModeProperty().setValue(SelectionMode.MULTIPLE);
        invoicesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String body = newValue == null ?
                    Lang.getString("stage.invoices_archive.view.prompt") :
                    InvoiceHtmlPrinter.print(newValue);
            invoiceView.getEngine().loadContent(getHtmlContent(body));
        });
        invoicesTable.setOnKeyReleased(event -> {
            int selectedItemsCount = invoicesTable.getSelectionModel().getSelectedItems().size();
            if (selectedItemsCount > 0) {
                switch (event.getCode()) {
                    case DELETE:
                        remove();
                        break;
                    case ENTER:
                        if (selectedItemsCount == 1) {
                            defaultAction.run();
                        }
                        break;
                }
            }
        });
        invoicesTable.setOnMouseClicked(event -> {
            int selectedItemsCount = invoicesTable.getSelectionModel().getSelectedItems().size();
            if (event.getClickCount() == 2 && selectedItemsCount == 1) {
                defaultAction.run();
            }
        });

        // id column
        TableColumn<Invoice, Long> idColumn = getIdColumn();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setCellFactory(new FormatFactory<>(InvoiceFormats.idFormat()::format));

        //  name column
//        getClientsNameColumn().setCellValueFactory(param -> clientNameExpression(param.getValue()));
        getClientsNameColumn().setCellValueFactory(param -> new SimpleStringProperty(getClientNames(param.getValue())));
        // date column
        TableColumn<Invoice, LocalDate> dateColumn = getDateColumn();
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(new FormatFactory<>(InvoiceFormats.dateConverter()::toString));

        // TODO: 2/18/16 Temp code, use preferences instead
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        invoicesTable.getSortOrder().add(dateColumn);

        // sum column
        TableColumn<Invoice, Double> sumColumn = getSumColumn();
        sumColumn.setCellValueFactory(new PropertyValueFactory<>("sum"));
        sumColumn.setCellFactory(new FormatFactory<>(InvoiceFormats.moneyFormat()::format));

        // INVOICE VIEWER
        InvoiceViewer.initViewer(invoiceView);
        invoiceView.getEngine().loadContent(getHtmlContent(Lang.getString("stage.invoices_archive.view.prompt")));

        // BUTTONS
        IntegerBinding selectionSize = Bindings.size(invoicesTable.getSelectionModel().getSelectedCells());
        openButton.disableProperty().bind(selectionSize.isNotEqualTo(1));
        editButton.disableProperty().bind(selectionSize.isNotEqualTo(1));
        removeButton.disableProperty().bind(selectionSize.isEqualTo(0));
    }

    private String getClientNames(Invoice invoice) {
//        return invoice.getPurchases().stream()
//                .map(Purchase::getClient)
//                .filter(Objects::nonNull)
//                .map(Client::getName)
//                .distinct()
//                .collect(Collectors.joining(", "));
        return "BROKEN";
    }


    private String getHtmlContent(String body) {
        return "<html>\n" +
                "<head>\n" +
                "    <link rel=\"stylesheet\" href=\"" + InvoicesArchiveControllerOld.class.getResource("/html_model/PreviewStyle.css") + "\">\n" +
                "</head>\n" +
                "<body>\n" +
                body + "\n" +
                "</body>\n" +
                "</html>";
    }

    private void setDefaultAction(int action) {
        defaultAction = action == 0 ? this::open : this::edit;
    }

    public void close() {
        StageManager.close(Stages.INVOICES_ARCHIVE);
    }

    public void open() {
        Invoice selectedInvoice = getSelectedInvoice();
        File pdf = getFile(selectedInvoice);

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

    private Invoice getSelectedInvoice() {
        return invoicesTable.getSelectionModel().getSelectedItem();
    }

    public void remove() {
        ObservableList<Invoice> invoices = invoicesTable.getSelectionModel().getSelectedItems();

        int choice = AlertBuilder.confirmation()
                .key("confirmation.remove_confirmation")
                .show();

        if (choice != 0) {
            return;
        }

        for (Invoice invoice : invoices) {
            File pdf = getFile(invoice);
            if (pdf != null && pdf.exists()) {
                pdf.delete();
            }
        }

        try {
            manager.removeInvoices(invoices);
            filteredInvoices.getSource().removeAll(invoices);
        } catch (IOException e) {
            // TODO: 12.05.17 Error handling
        }

    }

    public void openDirectory() {
        DesktopUtils.open(getInvoiceDirectory());
    }

    public void advancedView() {
        StageManager.show(Stages.DEBUG_VIEW);
    }

    private File getInvoiceDirectory() {
        return ConfigProperties.localPreferences().getPath(INVOICE_DIRECTORY);
    }

    private File getFile(Invoice invoice) {
        String pdfName = invoice.getPdfFilepath();
        if (pdfName == null) {
            return null;
        }
        return new File(getInvoiceDirectory(), pdfName);
    }


    @SuppressWarnings("unchecked")
    private TableColumn<Invoice, Long> getIdColumn() {
        return (TableColumn<Invoice, Long>) invoicesTable.getColumns().get(0);
    }

    @SuppressWarnings("unchecked")
    private TableColumn<Invoice, String> getClientsNameColumn() {
        return (TableColumn<Invoice, String>) invoicesTable.getColumns().get(1);
    }

    @SuppressWarnings("unchecked")
    private TableColumn<Invoice, LocalDate> getDateColumn() {
        return (TableColumn<Invoice, LocalDate>) invoicesTable.getColumns().get(2);
    }

    @SuppressWarnings("unchecked")
    private TableColumn<Invoice, Double> getSumColumn() {
        return (TableColumn<Invoice, Double>) invoicesTable.getColumns().get(3);
    }


}
