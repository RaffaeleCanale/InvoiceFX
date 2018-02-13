package com.wx.invoicefx.ui.views;

import com.wx.fx.gui.window.StageInfo;

/**
 * Enumeration of all the stages of this app
 *
 * Created on 18/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public enum Stages implements StageInfo {
    BACK_UP("/com/wx/invoicefx/ui/views/backup/Backup.fxml", false, -1),
    DATA_SET_CHOOSER("/com/wx/invoicefx/ui/views/sync/DataSetChooser.fxml", true, -1),
    SYNC_BOOTSTRAP("/com/wx/invoicefx/ui/views/bootstrap/SyncBootstrap.fxml", true, -1),
    DEBUG_VIEW("/com/wx/invoicefx/ui/views/archives/debug/DebugView.fxml", false, -1),
    OVERVIEW("/com/wx/invoicefx/ui/views/overview/Overview.fxml", false, 0),
    ITEMS_EDITOR("/com/wx/invoicefx/ui/views/item/ItemsEditor.fxml", false, -1),
    INVOICES_ARCHIVE("/com/wx/invoicefx/ui/views/archives/InvoicesArchive.fxml", false, 1),
    SETTINGS("/com/wx/invoicefx/ui/views/settings/Settings.fxml", false, 0),
    INVOICE_VIEWER("/com/wx/invoicefx/ui/views/viewer/InvoiceViewer.fxml", true, -1),
    CURRENCY_SHORTCUT("", false, 3);

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
