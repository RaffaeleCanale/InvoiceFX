package app.model_legacy.item;

import app.model_legacy.ListContainer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created on 08/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
@XmlRootElement
public class ItemsList implements ListContainer<ItemModel> {

    private List<ItemModel> item;

    public void setItem(List<ItemModel> item) {
        this.item = item;
    }

    public void setElements(List<ItemModel> item) {
        this.item = item;
    }

    @XmlElement(name = "item")
    public List<ItemModel> getElements() {
        return item;
    }

}
