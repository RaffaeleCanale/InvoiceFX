package app.gui.overview.editor;

import app.Stages;
import app.config.Config;
import app.config.manager.ManagerInterface;
import app.legacy.config.manager.ModelManager;
import app.model.item.Item;
import app.util.bindings.AddedRemovedListener;
import app.util.bindings.FormElement;
import app.util.gui.components.AlternateColorPanel;
import app.util.helpers.Common;
import app.util.helpers.InvoiceHelper;
import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.fx.preferences.properties.SharedProperty;
import com.wx.util.pair.Pair;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created on 07/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class ItemEditorController implements StageController {

    private static final int DEFAULT_ITEMS_PER_PAGE = 100;

    private final ObservableList<Item> copies = FXCollections.observableArrayList();

    private final Set<FormElement> forms = new HashSet<>();


    @FXML
    private Pane itemPanes;

    public void initialize() {
        AlternateColorPanel.bind(itemPanes);

//        copies.addListener(new AddedRemovedListener<Item>() {
//            @Override
//            public void added(Item item, int index) {
//                addItemPanel(item);
//            }
//
//            @Override
//            public void removed(Item item, int index) {
//                itemPanes.getChildren().remove(index);
//            }
//        });

        ManagerInterface manager = ModelManager.instance();

        Stream<Item> itemStream = Common.safeStream(manager.getAllItems());


        copies.addAll(
                itemStream.limit(DEFAULT_ITEMS_PER_PAGE).collect(Collectors.toList())
        );

//
//        Config.itemsManager().get().stream().sorted((o1, o2) -> Double.compare(o1.getTva(), o2.getTva()))
//                .map(Item::copyOf)
//                .forEach(copies::add);
    }

//    public void addNewItem() {
//        Item item = InvoiceHelper.createDefaultItem(Config.sharedPreferences().getDoubleArrayProperty(SharedProperty.VAT)[0]);
//        copies.add(item);
//    }

    private void addItemPanel(Item item) {
        FXMLLoader loader = new FXMLLoader(
                ItemEditorController.class.getResource("/app/gui/overview/editor/ItemEditorPanel.fxml"),
                Lang.getBundle());
        try {
            Pane pane = loader.load();
            ItemSubPanelController controller = loader.getController();
            Set<FormElement> typeForms = controller.bind(item);
            forms.addAll(typeForms);

            HBox.setHgrow(pane, Priority.ALWAYS);

            itemPanes.getChildren().add(pane);

            controller.setOnClose(e -> {
                forms.removeAll(typeForms);
                copies.remove(item);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        StageManager.close(Stages.ITEM_EDITOR);
    }

    public void save() {
        if (checkForms()) {
            ModelManager<Item> itemsManager = Config.itemsManager();



            itemsManager.get().setAll(distinct(copies));
            Config.saveSafe(itemsManager);

            StageManager.close(Stages.ITEM_EDITOR);
        }
    }

    private List<Item> distinct(List<Item> list) {
        Set<Pair<Double, String>> identifiers = new HashSet<>();


        return list.stream()
                .filter(item -> identifiers.add(new Pair<>(item.getTva(), item.getItemName())))
                .collect(Collectors.toList());
    }

    private boolean checkForms() {
        return forms.stream().map(FormElement::animateIfInvalid).reduce(true, (v1, v2) -> v1 && v2);
    }
}