package org.anchor.engine.common.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {

    public static <T> List<T> rearrange(List<T> items, T input) {
        int index = items.indexOf(input);
        List<T> copy;
        if (index >= 0) {
            copy = new ArrayList<T>(items.size());
            copy.add(items.get(index));
            copy.addAll(items.subList(0, index));
            copy.addAll(items.subList(index + 1, items.size()));
        } else {
            return items;
        }
        return copy;
    }

    public static <T> int getIndex(List<T> items, T input) {
        int i = 0;
        for (T item : items) {
            if (input == item || input.equals(item))
                break;

            i++;
        }

        return i;
    }

    public static byte[] randomBytes(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);

        return bytes;
    }

}
