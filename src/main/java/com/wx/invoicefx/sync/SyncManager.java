package com.wx.invoicefx.sync;

import com.wx.invoicefx.sync.index.FileInfo;
import com.wx.invoicefx.sync.index.Index;
import com.wx.invoicefx.sync.repo.Local;
import com.wx.invoicefx.sync.repo.Remote;
import com.wx.invoicefx.util.InvalidDataException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public class SyncManager {


    public enum Status {
        NEED_PULL,
        NEED_PUSH,
        VERSION_CONFLICT,
        IS_UP_TO_DATE,
        REMOTE_UNREACHABLE
    }


    private final Local local;
    private final Remote remote;


    public SyncManager(Local local, Remote remote) throws IOException {
        this.local = local;
        this.remote = remote;

        if (local.getIndex().isEmpty()) {
            local.createIndex();
        }

//        if (remote == null) {
//            currentState = State.NO_REMOTE;
//        } else if (remote.isReachable()) {
//            currentState = State.REMOTE_CONNECTED;
//        } else {
//            currentState = State.REMOTE_DISCONNECTED;
//        }

//        if (remote.isReachable() && !remote.getIndex().isEmpty()) {
//            if (local.getIndex().getBranchVersion() > remote.getIndex().getVersion()) {
//                throw new InvalidDataException();
//            }
//        }
    }

    public Status getStatus() throws InvalidDataException {
        if (!remote.isReachable()) {
            return Status.REMOTE_UNREACHABLE;
        }

        Index localIndex = local.getIndex();
        Index remoteIndex = remote.getIndex();

        localIndex.testIntegrity();
        remoteIndex.testIntegrity();

        if (remoteIndex.isEmpty() && localIndex.isEmpty()) return Status.IS_UP_TO_DATE;
        if (remoteIndex.isEmpty() && !localIndex.isEmpty()) return Status.NEED_PUSH;
        if (!remoteIndex.isEmpty() && localIndex.isEmpty()) return Status.NEED_PULL;

        ensureVersionsAreValid();

        if (!local.hasUncommittedChanges() && localIndex.getBranchVersion() == remoteIndex.getVersion()) return Status.IS_UP_TO_DATE;
        if (local.hasUncommittedChanges() && localIndex.getBranchVersion() == remoteIndex.getVersion()) return Status.NEED_PUSH;
        if (!local.hasUncommittedChanges() && localIndex.getBranchVersion() < remoteIndex.getVersion()) return Status.NEED_PULL;

        return Status.VERSION_CONFLICT;
    }

    private void ensureVersionsAreValid() throws InvalidDataException {
        if (!remote.getIndex().isEmpty() && local.getIndex().getBranchVersion() > remote.getIndex().getVersion()) {
            throw new InvalidDataException("TODO"); // TODO: 15.05.17 Error message
        }
    }


    public int pull() throws IOException, InvalidDataException {
        if (!remote.isReachable()) {
            throw new IOException("Remote is unreachable");
        } else if (remote.getIndex().isEmpty()) {
            return 0;
        }

        ensureVersionsAreValid();

        Index localIndex = local.getIndex();
        Index remoteIndex = remote.getIndex();


        Diff diff = computeDiff(remoteIndex, localIndex);
        int changesCount = diff.changesCount();

//        if (changesCount == 0) {
//            return 0;
//        }

        for (String filename : diff.filesToCopy) {
            File destination = local.getFile(filename);

            remote.downloadFile(filename, destination);
        }

        for (String filename : diff.filesToUpdate) {
            File destination = local.getFile(filename);

            remote.downloadFile(filename, destination);
        }

        for (String filename : diff.filesToRemove) {
            local.getFile(filename).delete();
        }

        remoteIndex.copyTo(localIndex);
        localIndex.setBranchVersion(remoteIndex.getVersion());

        localIndex.save();

        return changesCount;
    }

    public int push() throws IOException, InvalidDataException {
        return push(false);
    }

    public int pushForce() throws IOException, InvalidDataException {
        return push(true);
    }

    private int push(boolean force) throws IOException, InvalidDataException {
        if (!remote.isReachable()) {
            throw new IOException("Remote is unreachable");
        } else if (local.getIndex().isEmpty()) {
            return 0;
        }

        ensureVersionsAreValid();

        Index localIndex = local.getIndex();
        Index remoteIndex = remote.getIndex();

        if (!remoteIndex.isEmpty() && remoteIndex.getVersion() > localIndex.getBranchVersion()) {
            if (force) {
                if (localIndex.getVersion() <= remoteIndex.getVersion()) {
                    localIndex.setVersion(remoteIndex.getVersion());
                    localIndex.incrementVersion();
                }

                localIndex.setBranchVersion(remoteIndex.getVersion());
            } else {
                throw new IllegalArgumentException("Must pull first");
            }
        }


        Diff diff = computeDiff(localIndex, remoteIndex);

        for (String filename : diff.filesToCopy) {
            File target = local.getFile(filename);

            remote.uploadFile(filename, target);
        }

        for (String filename : diff.filesToUpdate) {
            File target = local.getFile(filename);

            remote.uploadFile(filename, target);
        }

        for (String filename : diff.filesToRemove) {
            remote.removeFile(filename);
        }

        localIndex.copyTo(remoteIndex);
        localIndex.setBranchVersion(localIndex.getVersion());

        localIndex.save();
        remoteIndex.save();

        return diff.changesCount();
    }

    private static Diff computeDiff(Index head, Index target) {
        Diff diff = new Diff();

        if (head.isEmpty() && target.isEmpty()) {
            return diff;
        } else if (head.isEmpty()) {
            diff.filesToRemove.addAll(target.getAllFilenames());
            return diff;

        } else if (target.isEmpty()) {
            diff.filesToCopy.addAll(head.getAllFilenames());

            return diff;
        }

        int filesCount = head.getFilesCount();

        for (int i = 0; i < filesCount; i++) {
            FileInfo headFile = head.getFileInfo(i);
            FileInfo targetFile = target.getFileInfo(headFile.getFilename());

            if (targetFile == null) {
                diff.filesToCopy.add(headFile.getFilename());

            } else if (Arrays.equals(headFile.getCheckSum(), targetFile.getCheckSum())) {
                diff.ignoredFiles.add(headFile.getFilename());

            } else {
                diff.filesToUpdate.add(headFile.getFilename());
            }
        }

        int targetFilesCount = target.getFilesCount();
        for (int i = 0; i < targetFilesCount; i++) {
            String targetFilename = target.getFileInfo(i).getFilename();
            if (!diff.filesToCopy.contains(targetFilename) &&
                    !diff.filesToUpdate.contains(targetFilename) &&
                    !diff.ignoredFiles.contains(targetFilename)) {

                diff.filesToRemove.add(targetFilename);
            }
        }

        return diff;
    }


    private static class Diff {
        private final Set<String> filesToRemove = new HashSet<>();
        private final Set<String> filesToUpdate = new HashSet<>();
        private final Set<String> filesToCopy = new HashSet<>();
        private final Set<String> ignoredFiles = new HashSet<>();

        public int changesCount() {
            return filesToCopy.size() + filesToRemove.size() + filesToUpdate.size();
        }
    }
}
