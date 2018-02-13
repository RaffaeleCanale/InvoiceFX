package com.wx.invoicefx.ui.views.settings.cards;

import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.ui.components.settings.ExpandPane;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.invoicefx.util.view.FormattedListFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.Locale;

import static com.wx.invoicefx.config.preferences.local.LocalProperty.LANGUAGE;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class LanguageChooserController {

    private static final int ROW_HEIGHT = 40;

    @FXML
    private ListView<Locale> languagesListView;
    @FXML
    private ExpandPane expandPane;

    @FXML
    private void initialize() {
        languagesListView.setCellFactory(new FormattedListFactory<>(Locale::getDisplayName));
        languagesListView.setItems(FXCollections.observableArrayList(AppResources.supportedLanguages()));
        languagesListView.setPrefHeight(languagesListView.getItems().size() * ROW_HEIGHT + 2);
        languagesListView.getSelectionModel().select(Locale.getDefault());

        expandPane.setSubLabel(Locale.getDefault().getDisplayLanguage());

        languagesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Locale.setDefault(newValue);
            AppResources.localPreferences().setProperty(LANGUAGE, newValue.toLanguageTag());

            StageManager.closeAll();
            StageManager.show(Stages.SETTINGS);
        });
    }
}
