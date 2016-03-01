package app.tex;

import app.cmd.CommandRunner;
import app.config.Config;
import app.util.helpers.InvoiceHelper;
import app.model.DateEnabled;
import app.model.invoice.InvoiceModel;
import app.model.item.ClientItem;
import app.model.item.ItemModel;
import app.util.gui.DesktopOpen;
import app.util.helpers.Common;
import app.util.helpers.PdfNameHelper;
import com.wx.io.Accessor;
import com.wx.io.file.FileUpdater;
import com.wx.io.file.FileUtil;
import com.wx.util.log.LogHelper;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import static app.config.preferences.properties.LocalProperty.INVOICE_DIRECTORY;
import static app.config.preferences.properties.LocalProperty.TEX_COMMAND;

/**
 * Created on 09/04/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class TexFileCreator {

    /*
    // TODO: 3/1/16 Add a LocalProperty to specify the template?
     */

    public static final String TEMPLATE_DIR = "template";
    private static final Logger LOG = LogHelper.getLogger(TexFileCreator.class);

    private final InvoiceModel invoice;
    private final boolean showCount;

    public TexFileCreator(InvoiceModel invoice, boolean showCount) {
        this.invoice = invoice;
        this.showCount = showCount;
    }

    public String create() throws IOException {
        File invoicesDir = initInvoicesDir();
        String fileName = PdfNameHelper.suggestFileName(invoice);

        File main = initMainTexFile();
        File pdf = runLaTex(main, 2);

        File newPdf = FileUtil.getFreshFile(invoicesDir, fileName, ".pdf");
        if (!pdf.renameTo(newPdf)) {
            throw new IOException("Cannot rename " + pdf.getAbsolutePath() + " to " + newPdf.getAbsolutePath());
        }
        LOG.info("PDF generated at:" + newPdf.getAbsolutePath());

        DesktopOpen.open(newPdf);

        return newPdf.getName();
    }

    private File initMainTexFile() throws IOException {
        File tmpDir = Config.getConfigFile("latex_build");
        FileUtil.autoCreateDirectory(tmpDir);

        File body = createCopyTo("invoice_body.tex", tmpDir);
        File main = createCopyTo("invoice.tex", tmpDir);

        update(body, invoice);
        return main;
    }

    private File createCopyTo(String name, File dir) throws IOException {
        File template = getTemplateFile(name);
        File copy = new File(dir, name);

        FileUtil.copyFile(template, copy);
        return copy;
    }

    private File initInvoicesDir() throws IOException {
        File invoiceDir = Config.localPreferences().getPathProperty(INVOICE_DIRECTORY);
        FileUtil.autoCreateDirectory(invoiceDir);

        return invoiceDir;
    }

    private File runLaTex(File mainTexFile, int pdfLatexPasses) throws IOException {
        File configDirectory = Config.getConfigDirectory();

        String cmd = Config.localPreferences().getProperty(TEX_COMMAND, mainTexFile.getAbsolutePath(), configDirectory.getAbsolutePath());

        CommandRunner runner = CommandRunner.getInstance(
                mainTexFile.getParentFile(),
                "pdflatex",
                cmd
        );
        for (int i = 0; i < pdfLatexPasses; i++) {
            runner.execute();
        }

        File pdfFile = new File(mainTexFile.getParentFile(), "invoice.pdf");
        if (!pdfFile.exists()) {
            runner.logOutput();
            throw new IOException("PDF not created, see logs.");
        }

        return pdfFile;
    }

    private File getTemplateFile(String name) throws IOException {
        File file = Config.getConfigFile(TEMPLATE_DIR + File.separator + name);
        if (!file.exists()) {
            FileUtil.autoCreateDirectory(file.getParentFile());
            extractResource(name, file);
        }

        return file;
    }

    private void extractResource(String name, File destination) throws IOException {
        try (Accessor accessor = new Accessor()
                .setOut(destination, false)
                .setIn(TexFileCreator.class.getResourceAsStream("/tex_template/" + name))) {
            accessor.pourInOut();
        }
    }


    private void update(File file, InvoiceModel invoice) throws IOException {
        StringConverter<LocalDate> dateConverter = InvoiceHelper.dateConverter();
        NumberFormat moneyFormat = InvoiceHelper.getFormat("#0.00");
        Map<String, String> updateMap = new HashMap<>();


        initDef(updateMap, "destination", invoice.getAddress());
        initDef(updateMap, "date", dateConverter.toString(invoice.getDate()));
        initDef(updateMap, "id", InvoiceHelper.idFormat().format(invoice.getId()));

        double chf_sum = invoice.sumProperty().get();
        double euro_sum = Common.computeEuro(chf_sum);
        initDef(updateMap, "totalChf", moneyFormat.format(chf_sum));
        initDef(updateMap, "totalEuro", moneyFormat.format(euro_sum));

        Map<Double, Double> vatTotals = new TreeMap<>();
        StringBuilder clientsString = new StringBuilder();

        for (ClientItem clientItem : invoice.getItems()) {
            ItemModel item = clientItem.getItem();

            if (!clientItem.getClientName().isEmpty()) {
                initCommand(clientsString, "client", clientItem.getClientName());
            }

            String itemName = item.getItemName();
            if (!itemName.isEmpty()) {
                if (showCount) {
                    itemName = clientItem.getItemCount() + " " + itemName;
                }

                initCommand(clientsString, "clientItem",
                        itemName,
                        moneyFormat.format(clientItem.sumProperty().get()));
            }

            if (clientItem.getDateEnabled() != DateEnabled.NONE) {
                initCommand(clientsString, "fromDate", dateConverter.toString(clientItem.getFromDate()));

                if (clientItem.getDateEnabled() == DateEnabled.BOTH) {
                    initCommand(clientsString, "toDate", dateConverter.toString(clientItem.getToDate()));
                }
            }

            initCommand(clientsString, "itemSep");

            double vat = item.getVat();
            double sum = clientItem.sumProperty().get();
            Double oldSum = vatTotals.get(vat);
            if (oldSum != null) {
                sum += oldSum;
            }
            vatTotals.put(vat, sum);

        }
        updateMap.put("% clients", clientsString.toString());


        // VAT
        StringBuilder vatString = new StringBuilder();
        NumberFormat vatFormat = InvoiceHelper.getFormat("#0.#");

        int vatIndex = 0;
        for (Map.Entry<Double, Double> entry : vatTotals.entrySet()) {
            double vat = entry.getKey();
            double vatSum = entry.getValue();
            double vatShare = Common.computeVatShare(vat, vatSum);

            initCommand(vatString, "vat",
                    vatFormat.format(vat),
                    moneyFormat.format(vatShare),
                    String.valueOf(vatIndex));
            vatIndex++;
        }

        updateMap.put("% vat", vatString.toString());

        new FileUpdater((s, i) -> updateMap.getOrDefault(s, s)).update(file);
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

        Common.replaceAll(builder, "\\", "\\textbackslash ");
        Common.replaceAll(builder, "\n", "\\\\");
        Common.replaceAll(builder, "&", "\\&");
        Common.replaceAll(builder, "{", "\\{");
        Common.replaceAll(builder, "}", "\\}");
        Common.replaceAll(builder, "$", "\\$");
        Common.replaceAll(builder, "#", "\\#");
        Common.replaceAll(builder, "^", "\\textasciicircum{}");
        Common.replaceAll(builder, "_", "\\_");
        Common.replaceAll(builder, "~", "\\textasciitilde{}");
        Common.replaceAll(builder, "%", "\\%");
        Common.replaceAll(builder, "<", "\\textless{}");
        Common.replaceAll(builder, ">", "\\textgreater{}");
        Common.replaceAll(builder, "|", "\\textbar{}");
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
