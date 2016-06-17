package app.util;

import java.util.Comparator;
import java.util.List;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class UpperBoundBinarySearch {

    public static <T> int search(List<T> list, T key, Comparator<T> comparator) {
        return search(list, 0, list.size(), key, comparator);
    }

    private static <T> int search(List<T> list, int fromIndex, int toIndex, T key, Comparator<T> comparator) {
        int low = fromIndex;
        int high = toIndex - 1;
        int found = -1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            T midVal = list.get(mid);

            if (comparator.compare(midVal, key) < 0) {
                low = mid + 1;
            } else if (comparator.compare(midVal, key) > 0) {
                high = mid - 1;
            } else {
                found = mid;
                // For last occurrence:
                low = mid + 1;
                // For first occurrence:
                // high = mid - 1;
            }
        }

        return found < 0 ? -low-1 : found;
    }

}
