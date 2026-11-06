package taskmanager.util;

import taskmanager.model.Priority;
import taskmanager.model.Status;
import taskmanager.model.Task;
import taskmanager.model.TaskType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
public final class Stats {

    private Stats() { }

    public static <T, K> Map<K, Long> countBy(Collection<T> items, Function<T, K> classifier) {
        Map<K, Long> result = new LinkedHashMap<>();
        for (T item : items) {
            K key = classifier.apply(item);
            result.merge(key, 1L, Long::sum);
        }
        return result;
    }
}
