package com.wx.invoicefx.tex;

import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Vats;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class TexDocumentCreatorBuilder {

    private Invoice invoice;
    private boolean showCount;
    private int pdfLatexPasses;
    private File buildDirectory;
    private File templateFile;
    private Vats allVats;

    public TexDocumentCreatorBuilder setInvoice(Invoice invoice) {
        this.invoice = invoice;
        return this;
    }

    public TexDocumentCreatorBuilder setShowCount(boolean showCount) {
        this.showCount = showCount;
        return this;
    }

    public TexDocumentCreatorBuilder setPdfLatexPasses(int pdfLatexPasses) {
        this.pdfLatexPasses = pdfLatexPasses;
        return this;
    }

    public TexDocumentCreatorBuilder setBuildDirectory(File buildDirectory) {
        this.buildDirectory = buildDirectory;
        return this;
    }

    public TexDocumentCreatorBuilder setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
        return this;
    }

    public TexDocumentCreatorBuilder setAllVats(Vats allVats) {
        this.allVats = allVats;
        return this;
    }

    public File create() throws IOException {
        requireNonNull(invoice);
        requireNonNull(showCount);
        requireNonNull(pdfLatexPasses);
        requireNonNull(buildDirectory);
        requireNonNull(templateFile);
        requireNonNull(allVats);

        return new TexDocumentCreator(invoice, showCount, pdfLatexPasses, buildDirectory, templateFile, allVats).create();
    }
}