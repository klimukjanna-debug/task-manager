package taskmanager.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import taskmanager.model.Identifiable;

public class Repository<T extends Identifiable> {

    private final Map<Integer, T> items = new LinkedHashMap<>();

    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Нельзя добавить пустой объект");
        }
        if (items.containsKey(item.getId())) {
            throw new IllegalArgumentException("Элемент с ID " + item.getId() + " уже существует");
        }
        items.put(item.getId(), item);
    }

    public Optional<T> findById(int id) {
        return Optional.ofNullable(items.get(id));
    }

    public boolean remove(int id) {
        return items.remove(id) != null;
    }

    public List<T> findAll() {
        return new ArrayList<>(items.values());
    }

    public List<T> find(Predicate<T> condition) {
        List<T> result = new ArrayList<>();
        for (T item : items.values()) {
            if (condition.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public int size() { return items.size(); }

    public void clear() { items.clear(); }
}
