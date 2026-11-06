package taskmanager.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import taskmanager.exception.StorageException;
import taskmanager.model.Priority;
import taskmanager.model.Status;
import taskmanager.model.Task;
import taskmanager.model.TaskFactory;
import taskmanager.model.TaskType;

public class FileTaskStorage implements TaskStorage {
    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;
    private final Path path;

    public FileTaskStorage(Path path) {
        this.path = path;
    }

    @Override
    public void save(Collection<Task> tasks) throws StorageException {
        Path temp = Path.of(path + ".tmp");
        try {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(temp.toFile()), StandardCharsets.UTF_8))) {
                writer.write("# тип;id;название;исполнитель;приоритет;статус;срок");
                writer.newLine();
                for (Task task : tasks) {
                    writer.write(serialize(task));
                    writer.newLine();
                }
            }
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Не удалось сохранить данные в файл " + path + ": " + e.getMessage(), e);
        }
    }

    @Override
    public LoadResult load() throws StorageException {
        List<Task> tasks = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        if (!Files.exists(path)) {
            return new LoadResult(tasks, warnings);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(path.toFile()), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                try {
                    tasks.add(parse(line));
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    warnings.add("строка " + lineNumber + " пропущена (некорректные данные): " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new StorageException("Не удалось прочитать файл " + path + ": " + e.getMessage(), e);
        }
        return new LoadResult(tasks, warnings);
    }

    private String serialize(Task task) {
        return task.getType().name() + SEPARATOR + task.getId() + SEPARATOR + task.getTitle() + SEPARATOR + (task.getAssignee() == null ? "" : task.getAssignee()) + SEPARATOR + task.getPriority().name() + SEPARATOR + task.getStatus().name() + SEPARATOR + (task.getDeadline() == null ? "" : task.getDeadline().toString());
    }

    private Task parse(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length != FIELD_COUNT) {
            throw new IllegalArgumentException("ожидается " + FIELD_COUNT + " полей через ';', найдено " + parts.length);
        }
        TaskType type = TaskType.valueOf(parts[0].trim());
        int id = Integer.parseInt(parts[1].trim());
        String title = parts[2];
        String assignee = parts[3].trim().isEmpty() ? null : parts[3];
        Priority priority = Priority.valueOf(parts[4].trim());
        Status status = Status.valueOf(parts[5].trim());
        LocalDate deadline = parts[6].trim().isEmpty() ? null : LocalDate.parse(parts[6].trim());
        return TaskFactory.create(type, id, title, assignee, priority, status, deadline);
    }
}
