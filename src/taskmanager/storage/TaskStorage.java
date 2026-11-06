package taskmanager.storage;

import java.util.Collection;
import taskmanager.exception.StorageException;
import taskmanager.model.Task;

public interface TaskStorage {
    void save(Collection<Task> tasks) throws StorageException;
    LoadResult load() throws StorageException;
}
