package app.util.backup;

import app.config.Config;
import com.wx.fx.preferences.properties.LocalProperty;
import com.google.common.io.Files;
import com.wx.io.file.FileUtil;
import com.wx.util.log.LogHelper;

import java.io.*;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created on 05/11/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class BackUpManager {

    private static final Logger LOG = LogHelper.getLogger(BackUpManager.class);

    private static final String BACKUP_DIR = "Backup";
    private static final String BACKUP_INDEX_FILE = "Backup.properties";
    private static final String BACKUP_SUFFIX = "bu";

    public static void backUp(File file) throws IOException {

        if (!slowBackUp(file)) {
            fastBackUp(file);
        }

    }

    public static List<BackUpFile> getAllBackUpFiles() throws IOException {
        return Stream.concat(
                getBackUpFiles("slow"),
                getBackUpFiles("fast")
        ).collect(Collectors.toList());
    }

    private static Stream<BackUpFile> getBackUpFiles(String type) throws IOException {
        File backUpDir = new File(getMainBackUpDir(), type);

        BackUpManager manager = new BackUpManager(backUpDir, 0, 0);
        manager.initIndex();
        manager.loadBackupFiles();

        return manager.backUpFiles.values()
                .stream().flatMap(Collection::stream);
    }



    private static File getMainBackUpDir() throws IOException {
        File backUpDir = Config.getConfigFile(BACKUP_DIR);
        FileUtil.autoCreateDirectory(backUpDir);
        return backUpDir;
    }

    public static boolean slowBackUp(File file) throws IOException {
        int daysInterval = Config.localPreferences().getIntProperty(LocalProperty.BACKUP_INTERVAL_DAYS);
        int maxFilesCount = Config.localPreferences().getIntProperty(LocalProperty.BACKUP_LENGTH);
        File backUpDir = new File(getMainBackUpDir(), "slow");

        if (maxFilesCount <= 0) {
            return false;
        }

        BackUpManager manager = new BackUpManager(backUpDir, daysInterval, maxFilesCount);
        manager.initIndex();
        manager.cleanUnIndexedFiles();
        manager.loadBackupFiles();

        return manager.performBackUp(file);
    }

    public static boolean fastBackUp(File file) throws IOException {
        int daysInterval = -1;
        int maxFilesCount = Config.localPreferences().getIntProperty(LocalProperty.FAST_BACKUP_LENGTH);
        File backUpDir = new File(getMainBackUpDir(), "fast");

        if (maxFilesCount <= 0) {
            return false;
        }

        BackUpManager manager = new BackUpManager(backUpDir, daysInterval, maxFilesCount);
        manager.initIndex();
        manager.cleanUnIndexedFiles();
        manager.loadBackupFiles();

        return manager.performBackUp(file);
    }

    private final Map<String, TreeSet<BackUpFile>> backUpFiles = new HashMap<>();
    private final Properties index = new Properties();
    private final File backUpDir;
    private final int daysInterval;
    private final int maxFilesCount;

    public BackUpManager(File backUpDir, int daysInterval, int maxFilesCount) throws IOException {
        this.backUpDir = backUpDir;
        this.daysInterval = daysInterval;
        this.maxFilesCount = maxFilesCount;

        FileUtil.autoCreateDirectory(backUpDir);
    }

    private void initIndex() throws IOException {
        File indexFile = new File(backUpDir, BACKUP_INDEX_FILE);
        if (indexFile.exists()) {
            index.load(new FileInputStream(indexFile));
        }
    }

    private void loadBackupFiles() throws IOException {
        for (Map.Entry<Object, Object> entry : index.entrySet()) {
            File file = getBackUpFile((String) entry.getKey());
            LocalDateTime date = parseSafely((String) entry.getValue());

            if (file.exists()) {
                BackUpFile backUpFile = new BackUpFile(file, date);
                filesFor(backUpFile.getBaseName()).add(backUpFile);
            }
        }
    }

    private TreeSet<BackUpFile> filesFor(String baseName) {
        TreeSet<BackUpFile> result = this.backUpFiles.get(baseName);
        if (result == null) {
            result = new TreeSet<>();
            backUpFiles.put(baseName, result);
        }

        return result;
    }

    private void cleanUnIndexedFiles() throws IOException {
        // REMOVE FILES NOT IN INDEX
        File[] files = backUpDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && !file.getName().equals(BACKUP_INDEX_FILE) && !index.containsKey(file.getName())) {
                    LOG.warning("[BACK UP] Deleting un-indexed file (" + backUpDir.getName() + "/" + file.getName() + ")");
                    file.delete();
                }
            }
        }

        // CLEAN ENTRIES NOT IN FILES
        Set<Object> keysToRemove = new HashSet<>();
        for (Object key : index.keySet()) {
            File file = getBackUpFile((String) key);

            if (!file.exists()) {
                keysToRemove.add(key);
            }
        }
        if (!keysToRemove.isEmpty()) {
            keysToRemove.forEach(index::remove);
            saveIndex();
        }
    }

    private File getBackUpFile(String key) {
        return new File(backUpDir, key);
    }


    private LocalDateTime parseSafely(String value) throws IOException {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IOException("Failed to parse date: " + value);
        }
    }

    private LocalDateTime nextUpdateDate(LocalDateTime date, int days) throws IOException {
        try {
            return date.plusDays(days);
        } catch (DateTimeException e) {
            throw new IOException("Failed to compute next back up date: (" + date + " + " + days + "d)");
        }
    }

    private boolean performBackUp(File f) throws IOException {
        TreeSet<BackUpFile> f_backups = filesFor(f.getName());

        LocalDateTime lastBackUp = f_backups.isEmpty() ?
                null :
                f_backups.last().getBackUpDate();


        if (lastBackUp == null || LocalDateTime.now().isAfter(nextUpdateDate(lastBackUp, daysInterval))) {

            while (f_backups.size() >= maxFilesCount) {
                BackUpFile oldestFile = f_backups.pollFirst();

                LOG.fine("[BACK UP] Removing oldest entry (" + backUpDir.getName() + "/" + oldestFile.getFile().getName() + ")");
                index.remove(oldestFile.getFile().getName());
                oldestFile.getFile().delete();
            }

            BackUpFile newBackUp = getFreshBackUpFile(f.getName());
            LOG.info("[BACK UP] Creating back up entry (" + backUpDir.getName() + "/" + newBackUp.getFile().getName() + ")");
            Files.copy(f, newBackUp.getFile());

            index.put(newBackUp.getFile().getName(), newBackUp.getBackUpDate().toString());
            saveIndex();

            return true;
        }

        return false;
    }

    private void saveIndex() throws IOException {
        File indexFile = new File(backUpDir, BACKUP_INDEX_FILE);
        index.store(new FileOutputStream(indexFile), null);
    }

    private BackUpFile getFreshBackUpFile(String baseName) {
        int i = 1;
        File file;
        do {
            file = new File(backUpDir, baseName + "_" + i + "." + BACKUP_SUFFIX);
            i++;
        } while (file.exists());

        return new BackUpFile(file, LocalDateTime.now());
    }


}
