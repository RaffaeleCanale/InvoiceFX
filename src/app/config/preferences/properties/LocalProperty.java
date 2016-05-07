package app.config.preferences.properties;

import app.cmd.CommandRunner.SupportedOs;
import app.config.Config;
import app.util.helpers.KeyWordHelper;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.time.LocalDate;

import static app.config.preferences.properties.PropertyCore.*;

/**
 * List of all properties that are NOT synchronized.
 * <p>
 * Created on 07/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public enum LocalProperty implements ConfigProperty {
    STORED_ITEMS_PATH(stringProperty("${config}types.jaxb")),
    STORED_INVOICES_PATH(stringProperty("${config}invoices.jaxb")),
    EXPORTED_PREFERENCES(stringProperty("${config}preferences.xml")),
    INVOICE_DIRECTORY(osProperty(ImmutableMap.of(
            SupportedOs.UNIX, "invoice",
            SupportedOs.WINDOWS, "${0}Documents" + File.separator + "Factures"
    ), KeyWordHelper.getDirectoryPath(System.getProperty("user.home")))),
    TEX_COMMAND(osProperty(ImmutableMap.of(
            SupportedOs.UNIX, "\"pdflatex\" -synctex=1 -interaction=nonstopmode \"${0}\"",
            SupportedOs.WINDOWS, "\"${1}\\miktex\\miktex\\bin\\pdflatex\" -synctex=1 -interaction=nonstopmode \"${0}\""
    ))),
    DEFAULT_APP_OPEN(osProperty(ImmutableMap.of(
            SupportedOs.UNIX, "xdg-open \"${0}\"",
            SupportedOs.WINDOWS, "start \"\" \"${0}\""
    ))),
    UPDATE_SCRIPT_NAME(osProperty(ImmutableMap.of(
            SupportedOs.UNIX, "update.sh",
            SupportedOs.WINDOWS, "update.bat"
    ))),
    DRIVE_CURRENT_USER(stringProperty("")),
    ARCHIVES_VIEW_SPLITTER(doubleProperty(0.5)),
    ARCHIVES_VIEW_ENABLED(booleanProperty(false)),
    BACKUP_INTERVAL_DAYS(intProperty(1)),
    BACKUP_LENGTH(intProperty(50)),
    FAST_BACKUP_LENGTH(intProperty(50));


    private final PropertyCore core;

    LocalProperty(PropertyCore core) {
        this.core = core;
    }

    @Override
    public PropertyCore core() {
        return core;
    }
}
