package com.wx.invoicefx.ui.views.settings.cards;

import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.sync.SyncManager;
import com.wx.invoicefx.ui.components.RemoveableComponent;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.util.concurrent.Callback;
import com.wx.util.concurrent.ConcurrentUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.17.
 */
public class SyncErrorController implements RemoveableComponent {


    private final ChangeListener<SyncManager.StateContainer> stateListener = (o, oldValue, newValue) -> this.reload(newValue);

    @FXML
    private Label exceptionLabel;
    @FXML
    private Pane contentPane;

    @FXML
    private void initialize() {
        ObjectProperty<SyncManager.StateContainer> state = AppResources.getSyncManager().currentStateProperty();
        state.addListener(stateListener);

        reload(state.get());
    }

    private void reload(SyncManager.StateContainer state) {
        contentPane.setVisible(false);

        if (state.getState() == SyncManager.State.FAILED) {
            Throwable ex = state.getException();
            if (ex != null) {
                loadException(ex);
            }
        }
    }

//    private Throwable getRemoteException(SyncManager syncManager) {
//        return syncManager.getRemote().flatMap(r -> Optional.ofNullable(r.getException())).orElse(null);
//    }
//
//    private Throwable getSyncManagerException(SyncManager syncManager) {
//        SyncManager.StateContainer state = syncManager.currentStateProperty().get();
//        return state.getState() == SyncManager.State.FAILED ? state.getException() : null;
//    }

    private void loadException(Throwable e) {
        contentPane.setVisible(true);
        exceptionLabel.setText("[" + e.getClass().getSimpleName() + "] " + e.getMessage());
    }

    @Override
    public void onRemove() {
        AppResources.getSyncManager().currentStateProperty().removeListener(stateListener);
    }

    @FXML
    private void retry() {
        AppResources.getSyncManager().disableSync();
        AppResources.getSyncManager().enableSync();

        AppResources.triggerSync();
    }

    @FXML
    private void reset() {
        int choice = AlertBuilder.confirmation()
                .key("stage.settings.card.syncError.reset")
                .show();

        if (choice == 0) {
            ConcurrentUtil.executeAsync(AppResources.getSyncManager()::resetRemote, new Callback<Object>() {
                @Override
                public Void success(Object o) {
                    return null;
                }

                @Override
                public Void failure(Throwable ex) {
                    ExceptionLogger.logException(ex);
                    return null;
                }
            });
        }
    }
}
