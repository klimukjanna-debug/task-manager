
package taskmanager.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import taskmanager.exception.InvalidStatusTransitionException;
import taskmanager.exception.StorageException;
import taskmanager.exception.TaskNotFoundException;
import taskmanager.model.Priority;
import taskmanager.model.Status;
import taskmanager.model.Task;
import taskmanager.model.TaskFactory;
import taskmanager.model.TaskType;
import taskmanager.repository.Repository;
import taskmanager.storage.LoadResult;
import taskmanager.storage.TaskStorage;

public class TaskService {

    private final Repository<Task> repository = new Repository<>();
    private final TaskStorage storage;
    private int nextId = 1;

    public TaskService(TaskStorage storage) {
        this.storage = storage;
    }

    public List<String> load() throws StorageException {
        LoadResult result = storage.load();
        List<String> warnings = new ArrayList<>(result.warnings());
        repository.clear();
        int maxId = 0;
        for (Task task : result.tasks()) {
            try {
                repository.add(task);
                maxId = Math.max(maxId, task.getId());
            } catch (IllegalArgumentException e) {
                warnings.add("задача #" + task.getId() + " пропущена: " + e.getMessage());
            }
        }
        nextId = maxId + 1;
        return warnings;
    }

    public Task createTask(TaskType type, String title, Priority priority, LocalDate deadline)
            throws StorageException {
        Task task = TaskFactory.create(type, nextId, title, null, priority, Status.NEW, deadline);
        repository.add(task);
        nextId++;
        persist();
        return task;
    }

    public Task getById(int id) throws TaskNotFoundException {
        return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    public void removeTask(int id) throws TaskNotFoundException, StorageException {
        if (!repository.remove(id)) {
            throw new TaskNotFoundException(id);
        }
        persist();
    }

    public void assignTask(int id, String assignee) throws TaskNotFoundException, StorageException {
        getById(id).assignTo(assignee);
        persist();
    }

    public void changeStatus(int id, Status newStatus)
            throws TaskNotFoundException, InvalidStatusTransitionException, StorageException {
        getById(id).changeStatus(newStatus);
        persist();
    }

    public List<Task> getAll() {
        return repository.findAll();
    }

    public List<Task> getAllSortedByScore() {
        List<Task> tasks = repository.findAll();
        tasks.sort(Comparator.comparingInt(Task::calculateScore).reversed()
                .thenComparingInt(Task::getId));
        return tasks;
    }

    public List<Task> find(String assignee) {
        return repository.find(task -> matchesAssignee(task, assignee));
    }

    public List<Task> find(Priority priority) {
        return repository.find(task -> task.getPriority() == priority);
    }

    public List<Task> find(String assignee, Priority priority) {
        return repository.find(task -> matchesAssignee(task, assignee)
                && task.getPriority() == priority);
    }

    private boolean matchesAssignee(Task task, String assignee) {
        if (assignee == null || assignee.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя исполнителя для поиска не задано");
        }
        return task.getAssignee() != null && task.getAssignee().equalsIgnoreCase(assignee.trim());
    }

    private void persist() throws StorageException {
        storage.save(repository.findAll());
    }
}
