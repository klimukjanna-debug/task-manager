package taskmanager.storage;

import java.util.List;
import taskmanager.model.Task;

public record LoadResult(List<Task> tasks, List<String> warnings) { }
