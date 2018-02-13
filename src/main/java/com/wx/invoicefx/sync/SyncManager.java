package com.wx.invoicefx.sync;

import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.google.DriveManager;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.util.concurrent.Callback;
import com.wx.util.log.LogHelper;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import static com.wx.invoicefx.sync.SyncManager.State.*;
import static com.wx.invoicefx.ui.views.sync.DataSetChooserController.DialogType.SOLVE_CONFLICT;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 18.06.17.
 */
public abstract class SyncManager {

    public enum State {
        OFF,
        PENDING,
        UP_TO_DATE,
        PULL,
        PUSH,
        PROCESSING_CONFLICT,
        PROCESSING_OTHER,
        CONFLICTED,
        FAILED
    }

    private final static Logger LOG = LogHelper.getLogger(SyncManager.class);


    private final ObjectProperty<StateContainer> currentState = new SimpleObjectProperty<>(new StateContainer(OFF));
    private final DataSet local;
    private DataSet remote;

    protected SyncManager(DataSet local) {
        this.local = local;
    }

    protected abstract DataSet initRemote() throws IOException;

    public ObjectProperty<StateContainer> currentStateProperty() {
        return currentState;
    }

    public boolean isEnabled() {
        return currentState.get().getState() != OFF;
    }

    public void disableSync() {
        setCurrentState(OFF);
        remote = null;
    }

    public boolean enableSync() {
        if (!DriveManager.isUserRegistered()) {
            return false;
        }

        try {
            setCurrentState(PENDING);
            if (remote == null) {
                remote = initRemote();
            }

            if (remote.isCorrupted()) {
                ExceptionLogger.logException(remote.getException(), "Remote failed");
                setCurrentState(FAILED, remote.getException());
                return false;
            }

            return true;
        } catch (IOException e) {
            ExceptionLogger.logException(e, "Remote failed");
            setCurrentState(FAILED, e);
            return false;
        }
    }

    public Optional<DataSet> getRemote() {
        return Optional.ofNullable(remote);
    }

    public synchronized void synchronizeWithRemote(Callback<?> callback) {
        LOG.info("Synchronizing with remote");

        State currentState = this.currentState.get().getState();
        if (currentState == OFF ||
                currentState == FAILED ||
                currentState == PUSH ||
                currentState == PULL ||
                currentState == PROCESSING_CONFLICT ||
                currentState == PROCESSING_OTHER) {
            LOG.fine("Synchronize aborted because of state: " + currentState);
            callback.failure(null);
            return;

        }
        setCurrentState(PROCESSING_OTHER);

        try {
            PushPullSync pushPullSync = new PushPullSync(local, remote);


            switch (pushPullSync.getStatus()) {
                case IS_UP_TO_DATE:
                    LOG.finer("Sync up-to-date");
                    setCurrentState(UP_TO_DATE);
                    callback.success(null);
                    return;

                case NEED_PULL:
                    setCurrentState(PULL, pushPullSync);
                    LOG.finer("Sync PULL");
                    pushPullSync.pull();

                    setCurrentState(UP_TO_DATE);
                    callback.success(null);
                    return;

                case NEED_PUSH:
                    setCurrentState(PUSH, pushPullSync);
                    LOG.finer("Sync PUSH");
                    pushPullSync.push();

                    setCurrentState(UP_TO_DATE);
                    callback.success(null);
                    return;

                case LOCAL_UNREACHABLE:
                    throw new IOException("Local unreachable");
                case REMOTE_UNREACHABLE:
                    throw new IOException("Remote unreachable");

                case VERSION_CONFLICT:
                    if (pushPullSync.getChangesCount() == 0) {
                        if (local.getIndex().getVersion() > remote.getIndex().getVersion()) {
                            LOG.finer("Sync CONFLICT - push force");
                            pushPullSync.pushForce();
                        } else {
                            LOG.finer("Sync CONFLICT - pull force");
                            pushPullSync.pullForce();
                        }

                        setCurrentState(UP_TO_DATE);
                        callback.success(null);
                        return;
                    }

                    LOG.fine("Sync CONFLICT - requires user intervention");
                    solveConflict(callback);
                    return;

                default:
                    throw new AssertionError();
            }
        } catch (IOException e) {
            fail(callback, e);
        }
    }

    public void resetRemote(Callback<?> callback) {
        LOG.info("Resetting remote");


        State currentState = this.currentState.get().getState();
        if (currentState != FAILED) {
            LOG.fine("Synchronize aborted because of state: " + currentState);
            callback.failure(null);
            return;
        }

        if (remote == null) {
            callback.failure(null);
            return;
        }

        try {
            remote.clear();
        } catch (IOException e) {
            callback.failure(e);
            return;
        }

        try {
            PushPullSync sync = new PushPullSync(local, remote);
            setCurrentState(PUSH, sync);
            sync.pushForce();
            setCurrentState(UP_TO_DATE, sync);
        } catch (IOException e) {
            fail(callback, e);
        }
    }

    private Void fail(Callback<?> callback, Throwable e) {
        setCurrentState(FAILED, e);

        return callback.failure(e);
    }

    private void solveConflict(Callback<?> callback) {
        setCurrentState(PROCESSING_CONFLICT);

        Callback<Object> resolveCallback = new Callback<Object>() {
            @Override
            public Void success(Object o) {
                setCurrentState(UP_TO_DATE);
                return callback.success(null);
            }

            @Override
            public Void failure(Throwable ex) {
                setCurrentState(FAILED, ex);
                return callback.failure(ex);
            }

            @Override
            public Void cancelled() {
                setCurrentState(CONFLICTED);
                return callback.cancelled();
            }
        };

        Platform.runLater(() -> StageManager.show(Stages.DATA_SET_CHOOSER, SOLVE_CONFLICT, resolveCallback));
    }

    private void setCurrentState(State state) {
        currentState.set(new StateContainer(state));
    }

    private void setCurrentState(State state, Throwable e) {
        currentState.set(new StateContainer(state, e));
    }

    private void setCurrentState(State state, PushPullSync syncManager) {
        currentState.set(new StateContainer(state, syncManager.progressProperty()));
    }

    public static class StateContainer {
        private final State state;
        private final DoubleProperty progress;
        private final Throwable exception;

        public StateContainer(State state) {
            this.state = state;
            this.progress = null;
            this.exception = null;
        }

        public StateContainer(State state, DoubleProperty progress) {
            this.state = state;
            this.progress = progress;
            this.exception = null;

        }

        public StateContainer(State state, Throwable exception) {
            this.state = state;
            this.progress = null;
            this.exception = exception;
        }

        public State getState() {
            return state;
        }

        public DoubleProperty progressProperty() {
            return progress;
        }

        public Throwable getException() {
            return exception;
        }
    }
}
