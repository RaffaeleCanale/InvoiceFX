package app.config.manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import app.model_legacy.ListContainer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;

/**
 * A simple {@link ModelManager} that uses a single file to store the models. <br> The {@code JAXB} libraries are used
 * to store and get models to the file.
 * <p>
 * Due to some {@code JAXB} limitations, a class that wraps a list of model must be provided (see {@link ListContainer}).
 * <p>
 * <p>
 * <p>
 * Created on 09/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class DefaultModelManager<E, L extends ListContainer<E>> implements ModelManager<E> {

    static Marshaller getMarshaller(Class... modelClass) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(modelClass);

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }

    static Unmarshaller getUnmarshaller(Class... modelClass) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(modelClass);

        return context.createUnmarshaller();
    }

    private final ObservableList<E> storedElements = FXCollections.observableArrayList();
    private final Class<L> listClass;
    private final File file;

    /**
     * Creates a new instance of the {@code DefaultModelManager}.
     * <p>
     * A class that wraps a list of models must be provided (see {@link ListContainer}.
     *
     * @param listClass The list wrapper class (must implement {@link ListContainer})
     * @param file      The file that will contain the models
     */
    public DefaultModelManager(Class<L> listClass, File file) {
        this.listClass = listClass;
        this.file = file;
    }

    /**
     * Get the file containing in which the models are stored.
     *
     * @return The file that contains the models
     */
    protected File getFile() {
        return file;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load() throws IOException {
        if (!file.exists()) {
            return;
        }
        try {
            Unmarshaller unmarshaller = getUnmarshaller(listClass);
            L list = (L) unmarshaller.unmarshal(file);

            if (list.getElements() != null) {
                storedElements.setAll(list.getElements());
            }
        } catch (JAXBException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void save() throws IOException {
        try {
            Marshaller marshaller = getMarshaller(listClass);

            L list = listClass.newInstance();
            list.setElements(storedElements);

            marshaller.marshal(list, file);
        } catch (JAXBException | InstantiationException | IllegalAccessException e) {
            throw new IOException(e);
        }
    }

    @Override
    public ObservableList<E> get() {
        return storedElements;
    }
}
