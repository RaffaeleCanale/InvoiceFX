package app.config.manager.datafile;

import app.config.manager.storage.PartitionedStorage;
import app.util.UpperBoundBinarySearch;
import com.sun.istack.internal.NotNull;
import com.wx.util.future.IoIterator;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import static com.wx.util.collections.CollectionsUtil.emptyIterator;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.06.16.
 */
public class ClusteredIndex {

    private final PartitionedStorage storage;
    private final int maxPartitionSize;
    private final int sortKey;
//    private final Property<Long> lastId;

//    private IOException readException;

    private final Map<Integer, List<Object[]>> partitionsBuffer = new HashMap<>();


    public ClusteredIndex(PartitionedStorage storage, int maxPartitionSize, int sortKey) {
        this.storage = storage;
        this.maxPartitionSize = maxPartitionSize;
        this.sortKey = sortKey;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void repartition() throws IOException {
        List<Object[]> allData = new ArrayList<>();
        for (int i = 0; i < storage.getPartitionsCount(); i++) {
            allData.addAll(getPartition(i));
        }

        Collections.sort(allData, Comparator.comparingLong(r -> (long) r[sortKey]));

        int numPartitions = -Math.floorDiv(-allData.size(), maxPartitionSize);

        int cursor = 0;
        for (int i = 0; i < numPartitions; i++) {
            int end = Math.min(cursor + maxPartitionSize, allData.size());
            storage.getPartition(i).write(allData.subList(cursor, end));

            cursor = end;
        }

        int actualCount = storage.getPartitionsCount();
        for (int i = numPartitions; i < actualCount; i++) {
            storage.removePartition(i);
        }
    }

    public IoIterator<Object[]> iterator() throws IOException {
        return new ReversedIterator(storage.getPartitionsCount());
    }

//    public Stream<Object[]> streamm() {
//        return StreamSupport.stream(spliterator(), false);
//    }

    public IoIterator<Object[]> queryIndex(long value) throws IOException {
        int partitionIndex = searchPartitionFor(value);
        if (partitionIndex < 0) {
            return emptyIterator();
        }

        List<Object[]> partition = getPartition(partitionIndex);
        int rowIndex = binarySearch(partition, value);

        if (rowIndex < 0) {
            return emptyIterator();
        } else {
            return new LimitReversedIterator(rowIndex, partitionIndex, value);
        }
    }

    public Optional<Object[]> queryFirst(Predicate<Object[]> query) throws IOException {
        IoIterator<Object[]> it = iterator();

        while (it.hasNext()) {
            Object[] next = it.next();
            if (query.test(next)) {
                return Optional.of(next);
            }
        }

        return Optional.empty();
    }

    public Optional<Object[]> queryIndexFirst(long value) throws IOException {
        IoIterator<Object[]> it = queryIndex(value);
        return it.hasNext() ? Optional.of(it.next()) : Optional.empty();
//        int partitionIndex = searchPartitionFor(value);
//        if (partitionIndex < 0) {
//            return Optional.empty();
//        }
//
//        List<Object[]> partition = getPartition(partitionIndex);
//        int rowIndex = binarySearch(partition, value);
//
//        return rowIndex < 0 ? Optional.empty() : Optional.of(partition.get(rowIndex));
    }

    public void insert(Object[] row) throws IOException {
        if (row[sortKey] == null) {
            throw new IllegalArgumentException("Sort column cannot be null!");
        }

        long rowValue = (long) row[sortKey];

        int partitionIndex = searchPartitionFor(rowValue);
        if (partitionIndex < 0) { // NOT within bounds
            partitionIndex = -partitionIndex - 1;

            if (partitionIndex == 0) {
                insertAtBeginning(0, row);
            } else {
                insertAtEnd(partitionIndex - 1, row);
            }
        } else {
            insertAtMiddle(partitionIndex, row);
        }
    }

    private void insertAtBeginning(int partitionIndex, Object[] row) throws IOException {
        List<Object[]> partition = getPartition(partitionIndex);
        if (partition.size() < maxPartitionSize) {
            partition.add(0, row);
            storage.getPartition(partitionIndex).write(partition);

        } else {
            partition.add(0, row);
            Object[] replaceRow = partition.remove(partition.size() - 1);

            storage.getPartition(partitionIndex).write(partition);
            insertAtBeginning(partitionIndex + 1, replaceRow);
        }
    }

    private void insertAtEnd(int partitionIndex, Object[] row) throws IOException {
        List<Object[]> partition = getPartition(partitionIndex);
        if (partition.size() < maxPartitionSize) {
            storage.getPartition(partitionIndex).append(partition, row);

        } else {
            insertAtBeginning(partitionIndex + 1, row);
        }
    }

    private void insertAtMiddle(int partitionIndex, Object[] row) throws IOException {
        List<Object[]> partition = getPartition(partitionIndex);

        int insertIndex = binarySearch(partition, row);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }

        if (insertIndex == 0) {
            insertAtBeginning(partitionIndex, row);
        } else if (insertIndex == partition.size()) {
            insertAtEnd(partitionIndex, row);

        } else {
            if (partition.size() < maxPartitionSize) {
                partition.add(insertIndex, row);
                storage.getPartition(partitionIndex).write(partition);

            } else {
                partition.add(insertIndex, row);
                Object[] replaceRow = partition.remove(partition.size() - 1);

                storage.getPartition(partitionIndex).write(partition);
                insertAtBeginning(partitionIndex + 1, replaceRow);
            }
        }
    }

    private int searchPartitionFor(long value) throws IOException {
        int partitionsCount = storage.getPartitionsCount();
        return searchFromRight(value, partitionsCount - 1);
    }

    private int searchFromRight(long value, int partitionIndex) throws IOException {
        if (partitionIndex < 0) {
            return -1;
        }

        List<Object[]> partition = getPartition(partitionIndex);

        if (partition.isEmpty()) {
            return searchFromRight(value, partitionIndex - 1);
        }

        long partitionStart = (long) partition.get(0)[sortKey];
        long partitionEnd = (long) partition.get(partition.size() - 1)[sortKey];

        if (value >= partitionStart && value <= partitionEnd) {
            return partitionIndex;
        }
        if (value > partitionEnd) {
            return -partitionIndex - 2;
        } else { // value < partitionStart
            return searchFromRight(value, partitionIndex - 1);
        }
    }

    private int binarySearch(List<Object[]> partition, long value) {
        Object[] query = new Object[sortKey + 1];
        query[sortKey] = value;

        return binarySearch(partition, query);
    }

    private int binarySearch(List<Object[]> partition, Object[] row) {
        Comparator<Object[]> comp = Comparator.comparingLong(r -> (long) r[sortKey]);
        return UpperBoundBinarySearch.search(partition, row, comp);
    }

    @NotNull
    private List<Object[]> getPartition(int partitionIndex) throws IOException {
        if (partitionIndex < 0) {
            throw new IndexOutOfBoundsException("Invalid partition index " + partitionIndex);
        }
        List<Object[]> partition = partitionsBuffer.get(partitionIndex);
        if (partition == null) {
            partition = storage.getPartition(partitionIndex).read();
            partitionsBuffer.put(partitionIndex, partition);
        }

        return partition;
    }


    private class ReversedIterator implements IoIterator<Object[]> {

        int nextRowIndex;
        int currentPartitionIndex;

        private ReversedIterator(int nextRowIndex, int currentPartitionIndex) {
            this.nextRowIndex = nextRowIndex;
            this.currentPartitionIndex = currentPartitionIndex;
        }

        ReversedIterator(int partitionsCount) throws IOException {
            currentPartitionIndex = partitionsCount;
            nextPartition();
        }

        @Override
        public boolean hasNext() {
            return currentPartitionIndex >= 0 && nextRowIndex >= 0;
        }

        @Override
        public Object[] next() throws IOException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Object[] next = getPartition(currentPartitionIndex).get(nextRowIndex);

            if (nextRowIndex == 0) {
                nextPartition();

            } else {
                nextRowIndex--;
            }

            return next;
        }

        private void nextPartition() throws IOException {
            do {
                currentPartitionIndex--;
            } while (currentPartitionIndex >= 0 && getPartition(currentPartitionIndex).isEmpty());

            nextRowIndex = currentPartitionIndex >= 0 ?
                    getPartition(currentPartitionIndex).size() - 1 : -1;
        }
    }

    private class LimitReversedIterator extends ReversedIterator {

        private final long acceptValue;

        public LimitReversedIterator(int nextRowIndex, int currentPartitionIndex, long acceptValue) {
            super(nextRowIndex, currentPartitionIndex);
            this.acceptValue = acceptValue;
        }

        @Override
        public boolean hasNext() {
            return super.hasNext() &&
                    acceptValue == (long) partitionsBuffer.get(currentPartitionIndex).get(nextRowIndex)[sortKey];
        }
    }

}
