package com.wx.invoicefx.ui.views.settings.cards;

import com.wx.fx.Lang;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.ui.components.settings.PropertyTogglePane;
import com.wx.util.concurrent.ConcurrentUtil;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;

import static com.wx.invoicefx.config.preferences.local.LocalProperty.DRIVE_CURRENT_USER;
import static com.wx.invoicefx.config.preferences.local.LocalProperty.ENABLE_SYNC;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class EnableSyncToggle extends PropertyTogglePane {

    private final BooleanProperty enableSync = AppResources.localPreferences().booleanProperty(ENABLE_SYNC);
    private final StringProperty currentDriveUser = AppResources.localPreferences().stringProperty(DRIVE_CURRENT_USER);
    private final ChangeListener<Boolean> enableSyncListener = (observable, oldValue, newValue) -> onEnableSyncChanged(newValue);



    public EnableSyncToggle(@NamedArg("text") String text) {
        super(text);
        init();
    }

    private void init() {
        bindWith(enableSync);
        enableSync.addListener(enableSyncListener);

        getSwitchComponent().stickyProperty().bind(currentDriveUser.isEmpty());
        getSwitchComponent().setOnStickyPrevent(e -> {
            Tooltip tooltip = new Tooltip(Lang.getString("stage.settings.card.sync_enabled.tooltip"));

            tooltip.setAutoHide(true);

            Point2D p = this.localToScene(0.0, 0.0);
            tooltip.show(this.getScene().getWindow(),
                    p.getX() + this.getScene().getX() + this.getScene().getWindow().getX()
                            + 15,
                    p.getY() + this.getScene().getY() + this.getScene().getWindow().getY() + this.getHeight()
                            - 15
            );
        });
    }

    @Override
    public void onRemove() {
        super.onRemove();

        enableSync.removeListener(enableSyncListener);
        getSwitchComponent().stickyProperty().unbind();
    }

    private void onEnableSyncChanged(Boolean newValue) {
        if (newValue) {
            AppResources.getSyncManager().enableSync();
            ConcurrentUtil.executeAsync(AppResources.getSyncManager()::synchronizeWithRemote, ConcurrentUtil.NO_OP);

        } else {
            AppResources.getSyncManager().disableSync();
        }
    }
}
