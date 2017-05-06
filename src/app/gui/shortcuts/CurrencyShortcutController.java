package app.gui.shortcuts;

import app.Stages;
import app.config.Config;
import app.gui.config.currency.CurrencyPanelController;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.properties.PropertiesManager;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.util.ResourceBundle;

/**
 * Created on 04/11/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class CurrencyShortcutController implements StageController {

    @FXML
    private CurrencyPanelController currencyPanelController;

    public void close() {
        StageManager.close(Stages.CURRENCY_SHORTCUT);
    }

    @Override
    public void closing() {
        currencyPanelController.unbindVariables();
        Config.saveSharedPreferences();
    }

    @Override
    public void setContext(Stage stage) {
        currencyPanelController.setContext(stage);
    }
}
