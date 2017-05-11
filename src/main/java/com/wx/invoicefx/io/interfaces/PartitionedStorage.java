package com.wx.invoicefx.io.interfaces;


/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 16.06.16.
 */
public interface PartitionedStorage {

    DataFile getPartition(int partitionIndex);

    int getPartitionsCount();

}