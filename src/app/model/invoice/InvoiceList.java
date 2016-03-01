package app.model.invoice;

import app.util.interfaces.ListWrapper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created on 02/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
@XmlRootElement
public class InvoiceList implements ListWrapper<InvoiceModel> {

    private List<InvoiceModel> invoices;

    @Override
    @XmlElement(name = "invoice")
    public void setElements(List<InvoiceModel> invoices) {
        this.invoices = invoices;
    }

    @Override
    public List<InvoiceModel> getElements() {
        return invoices;
    }

}
