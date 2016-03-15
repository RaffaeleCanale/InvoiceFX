package app;

import com.wx.fx.gui.window.StageInfo;

/**
 * Enumeration of all the stages of this app
 *
 * Created on 18/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public enum Stages implements StageInfo {
    OVERVIEW("/app/gui/overview/Overview.fxml", false, 0),
    ITEM_EDITOR("/app/gui/overview/editor/ItemEditor.fxml", true, 0),
    PREFS_EDITOR("/app/gui/config/properties/PropertiesViewer.fxml", false, 2),
    INVOICES_ARCHIVE("/app/gui/archives/InvoicesArchive.fxml", false, 1),
    SETTINGS("/app/gui/config/settings/SettingsEditor.fxml", false, 0),
    CURRENCY_SHORTCUT("/app/gui/shortcuts/CurrencyShortcut.fxml", false, 3);

    private final String location;
    private final boolean isModal;
    private final int group;

    Stages(String location, boolean isModal, int group) {
        this.location = location;
        this.isModal = isModal;
        this.group = group;
    }

    @Override
    public String location() {
        return location;
    }

    @Override
    public boolean isModal() {
        return isModal;
    }

    @Override
    public int stageGroup() {
        return group;
    }

}
