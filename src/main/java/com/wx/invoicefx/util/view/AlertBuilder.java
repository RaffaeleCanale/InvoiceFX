package com.wx.invoicefx.util.view;

import com.sun.javafx.tk.Toolkit;
import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.config.ExceptionLogger;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * Quickly create alert dialogs.
 * <p>
 * Created on 09/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class AlertBuilder {

    /**
     * Show an error dialog. Because the content is directly specified, this dialog does not require to access the
     * language resources.
     * <p>
     * This method is intended to be used for fatal errors before the language resources are loaded.
     *
     * @param header  Header of the alert
     * @param content Content of the alert
     */
    public static void showFatalError(String header, String content) {
        showFatalError(header, content, null);
    }

    /**
     * Show an error dialog. Because the content is directly specified, this dialog does not require to access the
     * language resources.
     * <p>
     * This method is intended to be used for fatal errors before the language resources are loaded.
     *
     * @param header  Header of the alert
     * @param content Content of the alert
     * @param ex      Exception that caused the error
     */
    public static void showFatalError(String header, String content, Throwable ex) {

        AlertBuilder alert = error().button(ButtonType.OK);

        alert.title = "Error";
        alert.header = header;
        alert.content = content;

        if (ex != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            TextArea textArea = new TextArea(sw.toString());
            textArea.setEditable(false);
            alert.expandableContent = textArea;
        }

        alert.show();
    }

    /**
     * Start building a new info dialog.
     *
     * @return A builder for an info dialog
     */
    public static AlertBuilder info() {
        return new AlertBuilder();
    }

    /**
     * Start building a new error dialog.
     *
     * @return A builder for an error dialog
     */
    public static AlertBuilder error() {
        return new AlertBuilder().alertType(Alert.AlertType.ERROR);
    }

    /**
     * Start building a new error dialog.
     *
     * @param ex Exception that will be shown as details for the error
     *
     * @return A builder for an error dialog
     */
    public static AlertBuilder error(Throwable ex) {
        return error().expandableContent(ex);
    }

    /**
     * Start building a new confirmation dialog.
     *
     * @return A builder for an confirmation dialog
     */
    public static AlertBuilder confirmation() {
        return new AlertBuilder().alertType(Alert.AlertType.CONFIRMATION);
    }

    /**
     * Start building a new warning dialog.
     *
     * @return A builder for an warning dialog
     */
    public static AlertBuilder warning() {
        return new AlertBuilder().alertType(Alert.AlertType.WARNING);
    }

    private Alert.AlertType alertType;
    private List<ButtonType> buttons = new LinkedList<>();
    private String title;
    private String header;
    private String content;
    private Node contentNode;
    private Node expandableContent;

    /**
     * Initialize an new alert builder
     */
    public AlertBuilder() {
    }

    /**
     * Set the type of the dialog.
     *
     * @param type Type of the dialog
     *
     * @return {@code this} (for chained calls)
     *
     * @see javafx.scene.control.Alert.AlertType
     */
    public AlertBuilder alertType(Alert.AlertType type) {
        this.alertType = type;

        return this;
    }

    /**
     * Set the content key. This key will be used to fetch the content to display in the language resources.
     * <p>
     * More specifically, it will get the following properties from the language resources:
     * <p>
     * <lu> <li>{key}.title</li> <li>{key}.header</li> <li>{key}.content</li> </lu>
     *
     * @param key    Key associated with the dialog message in the language resources
     * @param params Substitution parameters to use in the resources
     *
     * @return {@code this} (for chained calls)
     */
    public AlertBuilder key(String key, Object... params) {
        title = Lang.getOptionalString(key + ".title", params).orElse(null);
        header = Lang.getString(key + ".header", params);
        content = Lang.getOptionalString(key + ".content", params).orElse(null);

        return this;
    }

    public AlertBuilder setHeader(String header) {
        this.header = header;

        return this;
    }

    public AlertBuilder setContent(Node content) {
        contentNode = content;

        return this;
    }

    /**
     * Add a button key. The button text will be set to the property in the language resources associated with the given
     * key.
     *
     * @param key    Key (of the language resource) whose value to set for the button text
     * @param params Substitution parameters to use in the resources
     *
     * @return {@code this} (for chained calls)
     */
    public AlertBuilder button(String key, Object... params) {
        buttons.add(new ButtonType(Lang.getString(key, params)));

        return this;
    }

    /**
     * Add buttons.
     *
     * @param types Type of the buttons
     *
     * @return {@code this} (for chained calls)
     */
    public AlertBuilder button(ButtonType... types) {
        Collections.addAll(buttons, types);

        return this;
    }

    /**
     * Add a component as an expandable content.
     *
     * @param content Component to add
     *
     * @return {@code this} (for chained calls)
     */
    public AlertBuilder expandableContent(Node content) {
        expandableContent = content;

        return this;
    }

    /**
     * Add an exception description as an expandable content.
     *
     * @param ex Exception to show
     *
     * @return {@code this} (for chained calls)
     */
    public AlertBuilder expandableContent(Throwable ex) {
        Label label = new Label("[" + ex.getClass().getSimpleName() + "] " + ex.getMessage());
        label.getStyleClass().setAll("text-error");
        return expandableContent(label);
    }

    /**
     * Show the dialog. This blocking operation waits until the dialog is closed (even not called from the FX-User
     * thread).
     * <p>
     * The returned index is set according to the order in which button have been added.
     *
     * @return The index of the pressed button or -1 if the user closed the dialog
     */
    public int show() {
        if (buttons.isEmpty()) {
            setDefaultButtons();
        }
        if (title == null || title.isEmpty()) {
            setDefaultTitle();
        }

        if (Toolkit.getToolkit().isFxUserThread()) {
            return createAlertAndShow();
        }

        try {
            BlockingQueue<Integer> result = new ArrayBlockingQueue<>(1);
            Platform.runLater(() -> result.add(createAlertAndShow()));
            return result.take();
        } catch (InterruptedException e) {
            // Should not happen....
            ExceptionLogger.logException(e);
            return -1;
        }
    }

    private int createAlertAndShow() {
        if (header == null) {
            throw new IllegalArgumentException("No header set");
        }

        Alert alert = new Alert(alertType);
        alert.getDialogPane().setUserData(alert);
        alert.getDialogPane().getStyleClass().add("custom-dialog-pane");

        alert.setResizable(true);
        alert.getDialogPane().getStylesheets().add(StageManager.getStyleSheet());
        alert.getButtonTypes().setAll(buttons);

        alert.setTitle(title);
        alert.setHeaderText(header);

        if (content != null) {
            alert.setContentText(content);
        } else if (contentNode != null) {
            alert.getDialogPane().setContent(contentNode);
        }
        if (expandableContent != null) {
            alert.getDialogPane().setExpandableContent(expandableContent);
        }

        return alert.showAndWait()
                .map(i -> buttons.indexOf(i))
                .orElse(-1);
    }

    private void setDefaultTitle() {
        title = Lang.getString("alert.titles." + alertType.name().toLowerCase());
    }

    private void setDefaultButtons() {
        switch (alertType) {
            case CONFIRMATION:
                button(ButtonType.YES, ButtonType.NO);
                break;
            default:
                button(ButtonType.OK);
                break;
        }
    }

}
