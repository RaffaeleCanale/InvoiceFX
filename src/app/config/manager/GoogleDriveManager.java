package app.config.manager;

import app.google.DriveConfigHelper;
import app.util.ExceptionLogger;
import app.util.backup.BackUpManager;
import app.model_legacy.ListContainer;

import java.io.IOException;

/**
 * This {@link ModelManager} based on the {@link DriveConfigHelper} will automatically synchronize with a Google Drive:
 * <ul>
 *     <li>In case of get: If a more recent version is available on Drive, it is used to replace the current version and then loaded.</li>
 *     <li>In case of save: The Drive version is automatically updated.</li>
 * </ul>
 * <p>
 * It also provides automated backups.
 * <p>
 * Created on 14/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class GoogleDriveManager<E, L extends ListContainer<E>> extends DefaultModelManager<E, L> {

    public GoogleDriveManager(Class<L> listClass, java.io.File file) {
        super(listClass, file);
    }

    @Override
    public void load() throws IOException {
        try {
            BackUpManager.backUp(getFile());
        } catch (IOException e) {
            ExceptionLogger.logException(e);
        }

        try {
            Thread updateThread = DriveConfigHelper.performAction(DriveConfigHelper.Action.UPDATE, null, getFile());
            if (updateThread != null) {
                updateThread.join();
            }
        } catch (InterruptedException e) {
            // Ignored
        }
        super.load();
    }

    @Override
    public void save() throws IOException {
        super.save();
        DriveConfigHelper.performAction(DriveConfigHelper.Action.INSERT, null, getFile());
    }

}
