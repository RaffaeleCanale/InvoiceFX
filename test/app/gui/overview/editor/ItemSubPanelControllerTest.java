package app.gui.overview.editor;

import app.util.bindings.FormElement;
import com.wx.fx.Lang;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 30.07.16.
 */
public class ItemSubPanelControllerTest extends GuiTest {

    @Test
    public void test1() {

    }

    @Override
    protected Parent getRootNode() {


        FXMLLoader loader = new FXMLLoader(
                ItemEditorController.class.getResource("/app/gui/overview/editor/ItemEditorPanel.fxml"),
                Lang.getBundle());
        try {
            Pane pane = loader.load();
            ItemSubPanelController controller = loader.getController();
//            Set<FormElement> typeForms = controller.bind(item);

            return pane;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}