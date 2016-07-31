package app.gui.archives;

import app.Stages;
import app.config.Config;
import app.config.ModelManager;
import app.legacy.model.item.ClientItem;
import app.model.invoice.Invoice;
import app.util.gui.AlertBuilder;
import app.util.gui.InvoiceViewer;
import app.util.gui.cell.FormatFactory;
import app.util.helpers.Common;
import app.util.helpers.InvoiceHelper;
import app.util.helpers.InvoiceHtmlPrinter;
import app.util.helpers.TexCreatorHelper;
import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.util.log.LogHelper;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.util.logging.Logger;

import static app.config.preferences.LocalProperty.ARCHIVES_VIEW_ENABLED;
import static app.config.preferences.LocalProperty.ARCHIVES_VIEW_SPLITTER;
import static app.config.preferences.SharedProperty.ARCHIVES_DEFAULT_ACTION;


/**
 * Created on 10/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class InvoicesArchiveController implements StageController {

    private static final Logger LOG = LogHelper.getLogger(InvoicesArchiveController.class);

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

    private ModelManager manager;
    private FilteredList<Invoice> filteredInvoices;

    private Runnable defaultAction;

    @Override
    public void setContext(Stage stage) {
        // DEFAULT ACTION
        defaultActionGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            int index = defaultActionGroup.getToggles().indexOf(newValue);
            Config.sharedPreferences().setProperty(ARCHIVES_DEFAULT_ACTION, index);
            setDefaultAction(index);
        });

        int index = Config.sharedPreferences().getInt(ARCHIVES_DEFAULT_ACTION);
        setDefaultAction(index);
        defaultActionGroup.getToggles().get(index).setSelected(true);


        // SEARCH FIELD
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                filteredInvoices.setPredicate(null);
            } else {
                filteredInvoices.setPredicate(i -> InvoiceHelper.queryContains(i, newValue));
            }
        });


        // VIEW TOGGLE
        BooleanProperty viewEnabled = Config.localPreferences().booleanProperty(ARCHIVES_VIEW_ENABLED);
        DoubleProperty viewSplitter = Config.localPreferences().doubleProperty(ARCHIVES_VIEW_SPLITTER);
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
                    Lang.getString("archives.no_selection") :
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


        //  name column
        getClientsNameColumn().setCellValueFactory(param -> clientNameExpression(param.getValue()));
        // date column
        TableColumn<Invoice, LocalDate> dateColumn = getDateColumn();
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(new FormatFactory<>(InvoiceHelper.dateConverter()::toString));

        // TODO: 2/18/16 Temp code, use preferences instead
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        invoicesTable.getSortOrder().add(dateColumn);
        
        // sum column
        TableColumn<Invoice, Double> sumColumn = getSumColumn();
        sumColumn.setCellValueFactory(new PropertyValueFactory<>("sum"));
        sumColumn.setCellFactory(new FormatFactory<>(InvoiceHelper.moneyFormat()::format));

        // INVOICE VIEWER
        InvoiceViewer.initViewer(invoiceView);
        invoiceView.getEngine().loadContent(getHtmlContent(Lang.getString("archives.no_selection")));

        // BUTTONS
        IntegerBinding selectionSize = Bindings.size(invoicesTable.getSelectionModel().getSelectedCells());
        openButton.disableProperty().bind(selectionSize.isNotEqualTo(1));
        editButton.disableProperty().bind(selectionSize.isNotEqualTo(1));
        removeButton.disableProperty().bind(selectionSize.isEqualTo(0));
    }

    private StringExpression clientNameExpression(Invoice invoice) {
        StringProperty clientName = new SimpleStringProperty(InvoiceHelper.formatClientName(invoice));


        invoice.getItems().addListener((ListChangeListener<ClientItem>) c -> clientName.setValue(InvoiceHelper.formatClientName(invoice)));
        invoice.getItems().forEach(i -> i.clientNameProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> clientName.setValue(InvoiceHelper.formatClientName(invoice))));

        return clientName;
    }



    private StringBinding dynamicSeparator(StringExpression s1, StringExpression s2) {
        return Bindings.when(s1.isEmpty()).then("").otherwise(", ");
    }

    @Override
    public void setArguments(Object... args) {
        this.manager = (ModelManager) args[0];

        filteredInvoices = new FilteredList<>(manager.get());
        SortedList<Invoice> sortedList = new SortedList<>(filteredInvoices);
        sortedList.comparatorProperty().bind(invoicesTable.comparatorProperty());
        invoicesTable.setItems(sortedList);
    }

    private String getHtmlContent(String body) {
        return "<html>\n" +
                "<head>\n" +
                "    <link rel=\"stylesheet\" href=\"" + InvoicesArchiveController.class.getResource("/html_model/PreviewStyle.css") + "\">\n" +
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
            Common.open(pdf);
        } else {
            LOG.warning("Missing PDF at " + pdf + ", regenerating...");
            TexCreatorHelper.createAsync(selectedInvoice, openButton);
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

        manager.get().removeAll(invoices);
        Config.saveSafe(manager);
    }

    public void openDirectory() {
        Common.open(getInvoiceDirectory());
    }

    private File getInvoiceDirectory() {
        return Config.localPreferences().getPathProperty(LocalProperty.INVOICE_DIRECTORY);
    }

    private File getFile(Invoice invoice) {
        String pdfName = invoice.getPdfFileName();
        if (pdfName == null) {
            return null;
        }
        return new File(getInvoiceDirectory(), pdfName);
    }


    @SuppressWarnings("unchecked")
    private TableColumn<Invoice, String> getClientsNameColumn() {
        return (TableColumn<Invoice, String>) invoicesTable.getColumns().get(0);
    }

    @SuppressWarnings("unchecked")
    private TableColumn<Invoice, LocalDate> getDateColumn() {
        return (TableColumn<Invoice, LocalDate>) invoicesTable.getColumns().get(1);
    }

    @SuppressWarnings("unchecked")
    private TableColumn<Invoice, Double> getSumColumn() {
        return (TableColumn<Invoice, Double>) invoicesTable.getColumns().get(2);
    }

}
