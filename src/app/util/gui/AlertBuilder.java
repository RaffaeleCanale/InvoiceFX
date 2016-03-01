package app.util.gui;

import app.App;
import com.sun.javafx.tk.Toolkit;
import com.wx.fx.gui.window.StageManager;
import com.wx.properties.PropertiesManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created on 09/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class AlertBuilder {

    public static void showFatalError(String header, String content) {
        showFatalError(header, content, null);
    }

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

    public static AlertBuilder info() {
        return new AlertBuilder();
    }

    public static AlertBuilder error() {
        return new AlertBuilder().alertType(Alert.AlertType.ERROR);
    }
    public static AlertBuilder error(Throwable ex) {
        return error().exception(ex);
    }
    public static AlertBuilder confirmation() {
        return new AlertBuilder().alertType(Alert.AlertType.CONFIRMATION);
    }
    public static AlertBuilder warning() {
        return new AlertBuilder().alertType(Alert.AlertType.WARNING);
    }

    private final PropertiesManager lang = App.getLang();

    private Alert.AlertType alertType;
    private List<ButtonType> buttons = new LinkedList<>();
    private String title;
    private String header;
    private String content;
    private Node expandableContent;

    public AlertBuilder() {}

    public AlertBuilder alertType(Alert.AlertType type) {
        this.alertType = type;

        return this;
    }

//    public AlertBuilder title(String key, Object... params) {
//        alert.setTitle(Lang.get(key, params));
//
//        return this;
//    }
//    public AlertBuilder header(String key, Object... params) {
//        alert.setHeaderText(Lang.get(key, params));
//
//        return this;
//    }
//    public AlertBuilder content(String key, Object... params) {
//        alert.setContentText(Lang.get(key, params));
//
//        return this;
//    }

    public AlertBuilder key(String key, Object... params) {
        title = lang.getString(key + ".title", params);
        header = lang.getString(key + ".header", params);
        content = lang.getString(key + ".content", params);

        return this;
    }

    public AlertBuilder plainContent(String value) {
        content = value;

        return this;
    }

    public AlertBuilder button(String key, Object... params) {
        buttons.add(new ButtonType(lang.getString(key, params)));

        return this;
    }

    public AlertBuilder button(ButtonType... types) {
        Collections.addAll(buttons, types);

        return this;
    }

    public AlertBuilder expandableContent(Node content) {
        expandableContent = content;

        return this;
    }

    public AlertBuilder exception(Throwable ex) {
        Label label = new Label("[" + ex.getClass().getSimpleName() + "] " + ex.getMessage());
        label.setId("error");
        return expandableContent(label);
    }

    public boolean showYesNo() {
        return show() == 0;
    }

    public int show() {
        if (buttons.isEmpty()) {
            setDefaultButtons();
        }
        if (title == null || title.isEmpty()) {
            setDefaultTitle();
        }

        BlockingQueue<Integer> result = new ArrayBlockingQueue<>(1);

        if (Toolkit.getToolkit().isFxUserThread()) {
            return createAlertAndShow();
        }

        try {
            Platform.runLater(() -> result.add(createAlertAndShow()));
            return result.take();
        } catch (InterruptedException e) {
            return -1;
        }
    }

    private int createAlertAndShow() {
        Alert alert = new Alert(alertType);
        alert.setResizable(true);
        alert.getDialogPane().getStylesheets().add(StageManager.getStyleSheet());
        alert.getButtonTypes().setAll(buttons);

        alert.setTitle(title);
        if (header != null) {
            alert.setHeaderText(header);
        }
        if (content != null) {
            alert.setContentText(content);
        }
        if (expandableContent != null) {
            alert.getDialogPane().setExpandableContent(expandableContent);
        }

        ButtonType button = alert.showAndWait().orElse(null);
        if (button == null) {
            return -1;
        } else {
            return alert.getButtonTypes().indexOf(button);
        }
    }

    private void setDefaultTitle() {
        title = lang.getString("dialog." + alertType.name().toLowerCase() + "_title");
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
