package com.wx.invoicefx.legacy.config.manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Created on 15/03/2016
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class RuntimeModelManager<E> implements ModelManager<E> {

    private final ObservableList<E> models = FXCollections.observableArrayList();

    @Override
    public void load() throws IOException {}

    @Override
    public void save() throws IOException {}

    @Override
    public ObservableList<E> get() {
        return models;
    }
}
