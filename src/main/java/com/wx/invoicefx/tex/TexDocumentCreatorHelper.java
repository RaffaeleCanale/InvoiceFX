package com.wx.invoicefx.tex;

import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Vats;
import com.wx.io.file.FileUtil;
import com.wx.util.log.LogHelper;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static com.wx.invoicefx.config.Places.Dirs.TEX_BUILD_DIR;
import static com.wx.invoicefx.config.preferences.shared.SharedProperty.SHOW_ITEM_COUNT;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 10.06.17.
 */
public class TexDocumentCreatorHelper {

    private static final Logger LOG = LogHelper.getLogger(TexDocumentCreatorHelper.class);
    private static final int NUMBER_OF_PASSES = 2;

    private static TexDocumentCreatorBuilder instance(Invoice invoice) throws IOException {
        Vats vats = AppResources.getAllVats();

        return new TexDocumentCreatorBuilder()
                .setAllVats(vats)
                .setPdfLatexPasses(NUMBER_OF_PASSES)
                .setShowCount(AppResources.sharedPreferences().getBoolean(SHOW_ITEM_COUNT))
                .setBuildDirectory(Places.getDir(TEX_BUILD_DIR))
                .setTemplateFile(AppResources.getTexTemplate())
                .setInvoice(invoice);
    }

    public static File createTmpDocument(Invoice invoice, File template) throws IOException {
        return instance(invoice)
                .setTemplateFile(template)
                .create();
    }

    public static File createTmpDocument(Invoice invoice) throws IOException {
        return instance(invoice).create();
    }

    public static File createDocument(Invoice invoice, File outputFile) throws IOException {
        File documentFile = instance(invoice).create();


        FileUtil.copyFile(documentFile, outputFile);

        LOG.info("PDF generated at " + outputFile.getAbsolutePath());


        return outputFile;
    }


    private TexDocumentCreatorHelper() {}

}
