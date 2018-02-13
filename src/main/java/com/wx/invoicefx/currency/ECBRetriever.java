package com.wx.invoicefx.currency;

import com.wx.servercomm.http.AbstractHttpRequest;
import com.wx.servercomm.http.HttpRequest;
import com.wx.servercomm.http.HttpRequestBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created on 03/11/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
@SuppressWarnings("SpellCheckingInspection")
public class ECBRetriever implements CurrentRateRetriever {

    private static final String ECB_URL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    private static final long VALIDITY_PERIOD = 43200000; // 12h

    private static ECBRetriever singleton;

    public static void initialize(File cacheDirectory) {
        if (singleton != null) {
            throw new IllegalArgumentException("Already initialized");
        }

        AbstractHttpRequest httpGet = new HttpRequestBuilder(HttpRequest.createGET())
                .cachedSafe(cacheDirectory, VALIDITY_PERIOD)
                .buffered(VALIDITY_PERIOD)
                .get();

        singleton = new ECBRetriever(httpGet);
    }

    public static ECBRetriever instance() {
        if (singleton == null) {
            throw new IllegalArgumentException("Not initialized");
        }

        return singleton;
    }

    private final AbstractHttpRequest httpGet;

    private ECBRetriever(AbstractHttpRequest httpGet) {
        this.httpGet = httpGet;
    }

    @Override
    public Rates retrieveRates() throws IOException {
        LocalDate date;

        Document doc = fetchXML();


        Node node = getNode(doc, "gesmes:Envelope");
        node = getNode(node, "Cube");
        node = getNode(node, "Cube");


        try {
            date = LocalDate.from( DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(getAttribute(node,"time")) );
        } catch (IOException e) {
            throw new IOException("Malformed document, date cannot be parsed");
        }

        node = getNodeByAttributeValue(node, "currency", "CHF");

        try {
            double euroToChf = Double.parseDouble(getAttribute(node, "rate"));

            return new Rates(date, euroToChf);
        } catch (NumberFormatException e) {
            throw new IOException("Malformed document, rate cannot be parsed");
        }
    }

    private Node getNode(Node parent, String name) throws IOException {
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (name.equals(child.getNodeName())) {
                return child;
            }
        }

        throw new IOException("Malformed document, missing node " + name);
    }

    private Node getNodeByAttributeValue(Node parent, String attr, String value) throws IOException {
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.hasAttributes()) {
                Node attrNode = child.getAttributes().getNamedItem(attr);

                if (value.equals(attrNode.getNodeValue())) {
                    return child;
                }
            }
        }

        throw new IOException("Malformed document, missing node [" + attr + "=" + value + "]");
    }

    private String getAttribute(Node node, String attributeName) throws IOException {
        if (node.hasAttributes()) {
            Node attr = node.getAttributes().getNamedItem(attributeName);
            if (attr != null) {
                String nodeValue = attr.getNodeValue();

                if (nodeValue != null) {
                    return nodeValue;
                }
            }
        }

        throw new IOException("Malformed document, missing attribute " + attributeName);
    }

    private Document fetchXML() throws IOException {
        try (InputStream in = httpGet.executeAsStream(ECB_URL)) {

            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            return docBuilder.parse(in);

        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
    }
}
