package com.wx.invoicefx.model.entities.client;

/**
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class Client {

    public static final Client EMPTY_CLIENT = new Client().setEmptyName().setId(Long.MAX_VALUE);

    private long id;
    private String name;

    public long getId() {
        return id;
    }

    public Client setId(long id) {
        this.id = id;

        return this;
    }

    public String getName() {
        return name;
    }

    public Client setName(String name) {
        this.name = name.trim();

        return this;
    }

    private Client setEmptyName() {
        this.name = "    ";
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(getId());
    }
}
