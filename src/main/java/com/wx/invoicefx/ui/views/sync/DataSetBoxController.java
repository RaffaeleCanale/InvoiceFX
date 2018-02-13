package com.wx.invoicefx.ui.views.sync;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.sync.index.Index;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.properties.page.ResourcePage;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.wx.invoicefx.model.save.ModelSaver.MetadataKeys.INVOICES_COUNT;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 26.06.17.
 */
public class DataSetBoxController {

    @FXML
    private ChoiceBox<DataSet> versionsChoiceBox;
    @FXML
    private Button chooseButton;
    @FXML
    private Label versionLabel;
    @FXML
    private VBox contentPane;
    @FXML
    private Button openButton;
    @FXML
    private Label titleLabel;
    @FXML
    private Label invoicesCountLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label authorLabel;

    private DataSet dataSet;

    @FXML
    private void initialize() {
        versionsChoiceBox.setConverter(new StringConverter<DataSet>() {
            @Override
            public String toString(DataSet object) {
                return String.valueOf(object.getIndex().getVersion());
            }

            @Override
            public DataSet fromString(String string) {
                throw new UnsupportedOperationException();
            }
        });
        versionsChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setDataSet(newValue);
        });
    }

    public void setOnChooseHandler(EventHandler<ActionEvent> onChooseHandler) {
        chooseButton.setOnAction(onChooseHandler);
    }

    public void setDataSets(List<DataSet> dataSets) {
        if (dataSets.size() > 1) {
            versionLabel.setVisible(false);
            versionsChoiceBox.setVisible(true);
            versionsChoiceBox.setItems(FXCollections.observableArrayList(dataSets));
            versionsChoiceBox.getSelectionModel().select(0);
        } else {
            setDataSet(dataSets.get(0));
        }
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;

        setType(dataSet.getProperty("type").orElseThrow(() -> new RuntimeException("No type set")));

        if (dataSet.isCorrupted()) {
            setCorrupted();
            return;
        }


        Optional<String> invoicesCountProperty = dataSet.getProperty(INVOICES_COUNT.key());

        if (invoicesCountProperty.isPresent()) {
            invoicesCountLabel.setText(Lang.getString("stage.repository_chooser.box.label.invoices_count", invoicesCountProperty.get()));
            setVersionAndAuthor();
        } else {
            setCorrupted();
        }
    }

    private void setVersionAndAuthor() {
        Index index = dataSet.getIndex();

        String lastModifiedAuthor = index.getLastModifiedAuthor();
        Date lastModifiedDate = index.getLastModifiedDate();
        double version = index.getVersion();

        versionLabel.setText(String.valueOf(version));
        if (lastModifiedAuthor != null && lastModifiedDate != null) {
            dateLabel.setText(InvoiceFormats.formatDateTime(lastModifiedDate));
            authorLabel.setText(lastModifiedAuthor);
        }
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setChooseButtonText(String label) {
        chooseButton.setText(label);
    }

    public VBox getContentPane() {
        return contentPane;
    }

    private void setType(String typeName) {
        titleLabel.setText(Lang.getString("stage.repository_chooser.box.label." + typeName));
        titleLabel.getStyleClass().setAll("sync-repo-title-" + typeName);
    }

    private void setCorrupted() {
        invoicesCountLabel.setText(Lang.getString("stage.repository_chooser.box.label.corrupted"));
        invoicesCountLabel.getStyleClass().setAll("sync-repo-error");
        contentPane.setDisable(true);
//        versionsChoiceBox.setDisable(false);

        if (dataSet.getIndex() != null) {
            setVersionAndAuthor();
        }
    }

    @FXML
    private void onOpen() {
        StageManager.show(Stages.INVOICES_ARCHIVE, dataSet);
    }
}
