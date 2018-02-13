package com.wx.invoicefx.tex;

import com.wx.util.log.LogHelper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class ThumbnailBuilder {

    private static final Logger LOG = LogHelper.getLogger(ThumbnailBuilder.class);

    public static ThumbnailBuilder instance() {
        return new ThumbnailBuilder();
    }

    private File pdfFile;
    private Function<Integer, File> outputFileSupplier;
    private int limit = Integer.MAX_VALUE;

    public ThumbnailBuilder setInputFile(File pdfFile) {
        this.pdfFile = pdfFile;

        return this;
    }

    public ThumbnailBuilder setOutputSupplier(Function<Integer, File> outputFileSupplier) {
        this.outputFileSupplier = outputFileSupplier;

        return this;
    }

    public ThumbnailBuilder setLimit(int limit) {
        this.limit = limit;

        return this;
    }

    public void create() throws IOException {
        if (pdfFile == null) throw new IllegalStateException("Must set an input file");
        if (outputFileSupplier == null) throw new IllegalStateException("Must set an output supplier");

        LOG.finer("Rendering PDF: " + pdfFile);

        PDDocument document = PDDocument.load(pdfFile);
        PDFRenderer pdfRenderer = new PDFRenderer(document);


        for (int page = 0; page < document.getNumberOfPages() && page < limit; page++) {
            File outputFile = Objects.requireNonNull(outputFileSupplier.apply(page));
            LOG.finest("Creating thumbnail: " + outputFile);

            BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

            ImageIO.write(image, "png", outputFile);
        }

        document.close();
    }
}
