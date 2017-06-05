package com.wx.invoicefx.view;

import com.wx.fx.gui.window.StageInfo;

/**
 * Enumeration of all the stages of this app
 *
 * Created on 18/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public enum Stages implements StageInfo {
    DEBUG_VIEW("/com/wx/invoicefx/view/archives/debug/DebugView.fxml", false, -1),
    OVERVIEW("/com/wx/invoicefx/view/overview/Overview.fxml", false, 0),
    ITEM_EDITOR("", true, 0),
    PREFS_EDITOR("", false, 2),
    INVOICES_ARCHIVE("/com/wx/invoicefx/view/archives/InvoicesArchive.fxml", false, 1),
    SETTINGS("", false, 0),
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
