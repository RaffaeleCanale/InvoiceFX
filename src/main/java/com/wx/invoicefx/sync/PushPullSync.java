package com.wx.invoicefx.sync;

import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.sync.index.FileInfo;
import com.wx.invoicefx.sync.index.Index;
import com.wx.invoicefx.util.io.InvalidDataException;
import com.wx.io.ProgressInputStream;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public class PushPullSync {

    private static final int PROGRESS_INTERVAL = 1024;


    public enum Status {
        NEED_PULL,
        NEED_PUSH,
        VERSION_CONFLICT,
        IS_UP_TO_DATE,
        REMOTE_UNREACHABLE,
        LOCAL_UNREACHABLE
    }


    private final DataSet local;
    private final DataSet remote;

    private final DoubleProperty progress = new SimpleDoubleProperty(1.0);

    private void setProgress(double progress) {
        Platform.runLater(() -> this.progress.set(progress));
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public PushPullSync(DataSet local, DataSet remote) {
        this.local = local;
        this.remote = remote;
    }

    private boolean hasUncommittedChanges() {
        return local.getIndex().getVersion() != local.getIndex().getBaseVersion();
    }

    public Status getStatus() throws IOException {
        if (!remote.isReachable()) {
            return Status.REMOTE_UNREACHABLE;
        } else if (!local.isReachable()) {
            return Status.LOCAL_UNREACHABLE;
        }

//        local.testIndexIntegrity();
//        remote.testIndexIntegrity();

        Index localIndex = local.getIndex();
        Index remoteIndex = remote.getIndex();

        if (remoteIndex.isEmpty() && localIndex.isEmpty()) return Status.IS_UP_TO_DATE;
        if (remoteIndex.isEmpty() && !localIndex.isEmpty()) return Status.NEED_PUSH;
        if (!remoteIndex.isEmpty() && localIndex.isEmpty()) return Status.NEED_PULL;

        ensureVersionsAreValid();

        if (!hasUncommittedChanges() && localIndex.getBaseVersion() == remoteIndex.getVersion())
            return Status.IS_UP_TO_DATE;
        if (hasUncommittedChanges() && localIndex.getBaseVersion() == remoteIndex.getVersion()) return Status.NEED_PUSH;
        if (!hasUncommittedChanges() && localIndex.getBaseVersion() < remoteIndex.getVersion()) return Status.NEED_PULL;

        return Status.VERSION_CONFLICT;
    }

    public int getChangesCount() {
        return computeDiff(local.getIndex(), remote.getIndex()).changesCount();
    }

    private void ensureVersionsAreValid() throws IOException {
        if (!remote.getIndex().isEmpty() && local.getIndex().getBaseVersion() > remote.getIndex().getVersion()) {
            throw new IOException("Base version (" + local.getIndex().getBaseVersion() +
                    ") is larger than remote version (" + remote.getIndex().getVersion());
        }
    }

    public int pullForce() throws IOException {
        local.getIndex().setBaseVersion(0.0);
        return pull();
    }

    public int pull() throws IOException {
        return pull(false);
    }

    public int pullIgnoreBaseVersion() throws IOException {
        return pull(true);
    }

    private int pull(boolean ignoreBaseVersion) throws IOException {
        setProgress(0);

        if (!remote.isReachable()) {
            // TODO: 24.06.17 Useful?
            throw new IOException("Remote is unreachable");
        } else if (remote.getIndex().isEmpty()) {
            return 0;
        }

        ensureVersionsAreValid();

        Index localIndex = local.getIndex();
        Index remoteIndex = remote.getIndex();


        int changesCount = executeDiff(remote, local);

        double previousBaseVersion = localIndex.getBaseVersion();

        remoteIndex.copyTo(localIndex);

        localIndex.setBaseVersion(ignoreBaseVersion ? previousBaseVersion : remoteIndex.getVersion());

//        try {
//            local.testDataSetContent();
//        } catch (InvalidDataException e) {
//            throw new IOException(e);
//        }

        local.getIndex().save();

        setProgress(-1.0);

        return changesCount;
    }

    public int push() throws IOException {
        return push(false);
    }

    public int pushForce() throws IOException {
        return push(true);
    }

    private int push(boolean force) throws IOException {
        setProgress(0.0);

        if (!remote.isReachable()) {
            throw new IOException("Remote is unreachable");
//        } else if (local.getIndex().isEmpty()) {
//            return 0;
        }

        ensureVersionsAreValid();

        Index localIndex = local.getIndex();
        Index remoteIndex = remote.getIndex();

        if (!remoteIndex.isEmpty() && remoteIndex.getVersion() > localIndex.getBaseVersion()) {
            if (force) {
                if (localIndex.getVersion() <= remoteIndex.getVersion()) {
                    localIndex.setVersion(remoteIndex.getVersion());
                    localIndex.incrementVersion();
                }

                localIndex.setBaseVersion(remoteIndex.getVersion());
            } else {
                throw new IllegalArgumentException("Must pull first");
            }
        }


        int changesCount = executeDiff(local, remote);

        localIndex.copyTo(remoteIndex);
        localIndex.setBaseVersion(localIndex.getVersion());

//        try {
//            remote.testDataSetContent();
//        } catch (InvalidDataException e) {
//            throw new IOException(e);
//        }

        local.getIndex().save();
        remote.getIndex().save();

        setProgress(-1.0);

        return changesCount;
    }

    private int executeDiff(DataSet head, DataSet target) throws IOException {
        Diff diff = computeDiff(head.getIndex(), target.getIndex());
        int changesCount = diff.changesCount();
//        long totalSize = diff.totalSize();

        ProgressConsumer progress = new ProgressConsumer(diff.totalSize());

        try (AbstractFileSystem headFs = head.accessFileSystem();
             AbstractFileSystem targetFs = target.accessFileSystem()) {



            for (FileInfo fileInfo : diff.filesToCopy.values()) {
                String filename = fileInfo.getFilename();

                InputStream in = new ProgressInputStream(headFs.read(filename), progress, PROGRESS_INTERVAL);

                targetFs.write(filename, in);

                progress.increment(fileInfo.getFileSize());
            }

            for (FileInfo fileInfo : diff.filesToUpdate.values()) {
                String filename = fileInfo.getFilename();

                InputStream in = new ProgressInputStream(headFs.read(filename), progress, PROGRESS_INTERVAL);

                targetFs.write(filename, in);

                progress.increment(fileInfo.getFileSize());
            }

            for (FileInfo fileInfo : diff.filesToRemove.values()) {
                String filename = fileInfo.getFilename();

                targetFs.remove(filename);

                progress.increment(fileInfo.getFileSize());
            }
            return changesCount;
        }
    }

    private class ProgressConsumer implements Consumer<Long> {

        private final long totalSize;
        private long progress = 0L;

        private ProgressConsumer(long totalSize) {
            this.totalSize = totalSize;
        }

        @Override
        public void accept(Long size) {
            setProgress((double) (progress + size) / (double) totalSize);
        }

        void increment(long size) {
            progress += size;
            setProgress((double) progress / (double) totalSize);
        }

    }




    private static Diff computeDiff(Index head, Index target) {
        Diff diff = new Diff();

        if (head.isEmpty() && target.isEmpty()) {
            return diff;
        } else if (head.isEmpty()) {
            diff.filesToRemove.putAll(target.getAllFiles());
            return diff;

        } else if (target.isEmpty()) {
            diff.filesToCopy.putAll(head.getAllFiles());

            return diff;
        }

        int filesCount = head.getFilesCount();

        for (int i = 0; i < filesCount; i++) {
            FileInfo headFile = head.getFileInfo(i);
            FileInfo targetFile = target.getFileInfo(headFile.getFilename());

            if (targetFile == null) {
                diff.filesToCopy.put(headFile.getFilename(), headFile);

            } else if (Arrays.equals(headFile.getCheckSum(), targetFile.getCheckSum())) {
                diff.ignoredFiles.put(headFile.getFilename(), headFile);

            } else {
                diff.filesToUpdate.put(headFile.getFilename(), headFile);
            }
        }

        int targetFilesCount = target.getFilesCount();
        for (int i = 0; i < targetFilesCount; i++) {
            FileInfo targetFile = target.getFileInfo(i);
            String targetFilename = targetFile.getFilename();


            if (!diff.filesToCopy.containsKey(targetFilename) &&
                    !diff.filesToUpdate.containsKey(targetFilename) &&
                    !diff.ignoredFiles.containsKey(targetFilename)) {

                diff.filesToRemove.put(targetFilename, targetFile);
            }
        }

        return diff;
    }


    private static class Diff {
        private final Map<String, FileInfo> filesToRemove = new HashMap<>();
        private final Map<String, FileInfo> filesToUpdate = new HashMap<>();
        private final Map<String, FileInfo> filesToCopy = new HashMap<>();
        private final Map<String, FileInfo> ignoredFiles = new HashMap<>();

        public int changesCount() {
            return filesToCopy.size() + filesToRemove.size() + filesToUpdate.size();
        }

        public long totalSize() {
            return sizeOf(filesToCopy) + sizeOf(filesToRemove) + sizeOf(filesToUpdate);
        }

        private long sizeOf(Map<String, FileInfo> files) {
            return files.values().stream().mapToLong(FileInfo::getFileSize).sum();
        }

        @Override
        public String toString() {
            return "Diff{" +
                    "filesToRemove=" + filesToRemove +
                    ", filesToUpdate=" + filesToUpdate +
                    ", filesToCopy=" + filesToCopy +
                    '}';
        }
    }


}
