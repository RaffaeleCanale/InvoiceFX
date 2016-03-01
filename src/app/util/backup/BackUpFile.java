package app.util.backup;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created on 07/11/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class BackUpFile implements Comparable<BackUpFile> {

    private final String baseName;
    private final File file;
    private final LocalDateTime backUpDate;

    public BackUpFile(File file, LocalDateTime backUpDate) {
        this.file = file;
        this.backUpDate = backUpDate;

        int suffixStartIndex = file.getName().indexOf('_');
        if (suffixStartIndex < 0) {
            throw new RuntimeException(); // Shouldn't happen
        }

        this.baseName = file.getName().substring(0,suffixStartIndex);
    }

    public LocalDateTime getBackUpDate() {
        return backUpDate;
    }

    public String getBaseName() {
        return baseName;
    }

    public File getFile() {
        return file;
    }

    @Override
    public int compareTo(BackUpFile o) {
        int dateComp = backUpDate.compareTo(o.backUpDate);

        return dateComp == 0 ?
                file.compareTo(o.file) :
                dateComp;
    }

    @Override
    public String toString() {
        return "BackUpFile{" +
                "file=" + file +
                ", backUpDate=" + backUpDate +
                '}';
    }
}
