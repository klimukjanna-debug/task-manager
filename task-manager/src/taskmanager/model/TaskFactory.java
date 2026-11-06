package taskmanager.model;

import java.time.LocalDate;

public final class TaskFactory {

    private TaskFactory() { }

    public static Task create(TaskType type, int id, String title, String assignee,
                              Priority priority, Status status, LocalDate deadline) {
        if (type == null) {
            throw new IllegalArgumentException("Тип задачи не задан");
        }
        switch (type) {
            case REGULAR:
                return new RegularTask(id, title, assignee, priority, status, deadline);
            case URGENT:
                return new UrgentTask(id, title, assignee, priority, status, deadline);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}
