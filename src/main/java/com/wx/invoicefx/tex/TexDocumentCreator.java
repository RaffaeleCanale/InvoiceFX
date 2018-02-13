package com.wx.invoicefx.tex;

import com.wx.invoicefx.command.CommandRunner;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Vats;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.util.math.CurrencyUtils;
import com.wx.invoicefx.util.string.SentenceItemsParser;
import com.wx.invoicefx.util.string.StringUtils;
import com.wx.io.TextAccessor;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wx.invoicefx.config.Places.Dirs.TEX_BINARY_DIR;
import static com.wx.invoicefx.config.preferences.local.LocalProperty.TEX_COMMAND;


/**
 * Created on 09/04/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
class TexDocumentCreator {

    private static final String DEFAULT_OUTPUT_NAME = "invoice.pdf";
    private static final String DEFAULT_NAME = "invoice.tex";

    private final Invoice invoice;

    private final boolean showCount;
    private final int pdfLatexPasses;


    private final File buildDirectory;

    private final File templateFile;

    private final Vats allVats;


    TexDocumentCreator(Invoice invoice, boolean showCount, int pdfLatexPasses, File buildDirectory, File templateFile, Vats allVats) {
        this.invoice = invoice;
        this.showCount = showCount;
        this.pdfLatexPasses = pdfLatexPasses;
        this.buildDirectory = buildDirectory;
        this.templateFile = templateFile;
        this.allVats = allVats;
    }

    public File create() throws IOException {
        File main = generateTexFile();

        return runLaTex(main);
    }


    private File generateTexFile() throws IOException {
        File body = new File(buildDirectory, DEFAULT_NAME);

        Map<String, String> updateMap = createBodyUpdateMap();

        try (TextAccessor accessor = new TextAccessor()
                .setIn(templateFile)
                .setOut(body, false)) {

            String line;
            while ((line = accessor.readLine()) != null) {
                accessor.write( updateMap.getOrDefault(line, line) );
            }
        }

        return body;
    }


    private File runLaTex(File mainTexFile) throws IOException {
        File texBinaryDir = Places.getDir(TEX_BINARY_DIR);


        String command = AppResources.localPreferences().getString(TEX_COMMAND,
                mainTexFile.getAbsolutePath(),
                texBinaryDir.getAbsolutePath());

        CommandRunner runner = CommandRunner.getInstance(
                mainTexFile.getParentFile(),
                "pdflatex",
                command
        );

        for (int i = 0; i < pdfLatexPasses; i++) {
            runner.execute();
        }

        File pdfFile = new File(mainTexFile.getParentFile(), DEFAULT_OUTPUT_NAME);
        if (!pdfFile.exists()) {
            runner.logOutput();
            throw new IOException("PDF not created, see logs.");
        }

        return pdfFile;
    }

    private Map<String, String> createBodyUpdateMap() {
        StringConverter<LocalDate> dateConverter = InvoiceFormats.dateConverter();
        NumberFormat moneyFormat = InvoiceFormats.getNumberFormat("#0.00");
        DecimalFormat idFormat = InvoiceFormats.idFormat();

        Map<String, String> updateMap = new HashMap<>();
        VatsSum vatsSum = new VatsSum();

        final double chf_sum = invoice.getSum();
        final double euro_sum = CurrencyUtils.computeEuro(chf_sum);


        initDef(updateMap, "destination", invoice.getAddress());
        initDef(updateMap, "date", dateConverter.toString(invoice.getDate()));
        initDef(updateMap, "id", idFormat.format(invoice.getId()));
        initDef(updateMap, "totalChf", moneyFormat.format(chf_sum));
        initDef(updateMap, "totalEuro", moneyFormat.format(euro_sum));


        StringBuilder clientsString = new StringBuilder();

        for (PurchaseGroup group : invoice.getPurchaseGroups()) {
            List<String> clientNames = group.getClients().stream().map(Client::getName).collect(Collectors.toList());

            String clientsField = SentenceItemsParser.rebuildSentence(clientNames, group.getStopWords());

            initCommand(clientsString, "client", clientsField);

            for (Purchase purchase : group.getPurchases()) {
                String itemName = purchase.getItem().getName();
                String sum = moneyFormat.format(purchase.getSum());

                if (showCount) {
                    itemName = purchase.getItemCount() + " " + itemName;
                }

                initCommand(clientsString, "clientItem", itemName, sum);

                if (purchase.getDateEnabled() != DateEnabled.NONE) {
                    String fromDate = dateConverter.toString(purchase.getFromDate());
                    initCommand(clientsString, "fromDate", fromDate);

                    if (purchase.getDateEnabled() == DateEnabled.BOTH) {
                        String toDate = dateConverter.toString(purchase.getToDate());
                        initCommand(clientsString, "toDate", toDate);
                    }
                }

                initCommand(clientsString, "itemSep");

                vatsSum.addFor(purchase.getItem().getVat(), purchase.getSum());
            }
        }
        updateMap.put("% clients", clientsString.toString());


        // VAT
        StringBuilder vatString = new StringBuilder();
        NumberFormat vatFormat = InvoiceFormats.getNumberFormat("#0.#");


        vatsSum.nonZeroVatSums().forEach(v -> {
            initCommand(vatString, "vat",
                    vatFormat.format(v.vat.getValue()),
                    moneyFormat.format(v.vatShare),
                    String.valueOf(v.vat.getCategory() - 1));
        });

        updateMap.put("% vat", vatString.toString());

        return updateMap;
    }

    private void initDef(Map<String, String> updateMap, String def, String value) {
        updateMap.put("\\def\\" + def + "{VALUE}", "\\def\\" + def + "{" + processLatexString(value) + "}");
    }

    private void initCommand(StringBuilder cmdBuilder, String command, String... args) {
        cmdBuilder.append("\\").append(command);

        for (String arg : args) {
            cmdBuilder.append("{")
                    .append(processLatexString(arg))
                    .append("}");
        }

        cmdBuilder.append("\n");
    }


    private String processLatexString(String s) {
        StringBuilder builder = new StringBuilder(s);

        StringUtils.replaceAll(builder, "\\", "\\textbackslash ");
        StringUtils.replaceAll(builder, "\n", "\\\\");
        StringUtils.replaceAll(builder, "&", "\\&");
        StringUtils.replaceAll(builder, "{", "\\{");
        StringUtils.replaceAll(builder, "}", "\\}");
        StringUtils.replaceAll(builder, "$", "\\$");
        StringUtils.replaceAll(builder, "#", "\\#");
        StringUtils.replaceAll(builder, "^", "\\textasciicircum{}");
        StringUtils.replaceAll(builder, "_", "\\_");
        StringUtils.replaceAll(builder, "~", "\\textasciitilde{}");
        StringUtils.replaceAll(builder, "%", "\\%");
        StringUtils.replaceAll(builder, "<", "\\textless{}");
        StringUtils.replaceAll(builder, ">", "\\textgreater{}");
        StringUtils.replaceAll(builder, "|", "\\textbar{}");
//        replaceAll(builder, "\"", "\\textquotedbl{}");
//        replaceAll(builder, "'", "\\textquotesingle{}");
//        replaceAll(builder, "`", "\\textasciigrave{}");

        return builder.toString();

//        \ → \textbackslash{} (note the empty group!)
//        { → \{
//        } → \}
//        $ → \$
//        & → \&
//        # → \#
//        ^ → \textasciicircum{} (requires the textcomp package)
//        _ → \_
//        ~ → \textasciitilde{}
//        % → \%
//        In addition, the following substitutions are useful at least when using the OT1 encoding (and harmless in any case):
//
//        < → \textless{}
//        > → \textgreater{}
//        | → \textbar{}
//        And these three disable the curly quotes:
//
//        " → \textquotedbl{}
//        ' → \textquotesingle{}
//        ` → \textasciigrave{}
    }


}
