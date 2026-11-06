package taskmanager.model;

import java.time.LocalDate;
import taskmanager.exception.InvalidStatusTransitionException;

public abstract class Task implements Identifiable, Reportable {
    private static final int MAX_TEXT_LENGTH = 100;

    private final int id;
    private String title;
    private String assignee;
    private Priority priority;
    private Status status;
    private final LocalDate deadline;

    protected Task(int id, String title, String assignee, Priority priority, Status status, LocalDate deadline) {
        if (id <= 0) {
            throw new IllegalArgumentException("Идентификатор должен быть положительным числом");
        }
        if (priority == null || status == null) {
            throw new IllegalArgumentException("Приоритет и статус обязательны");
        }
        this.id = id;
        this.title = requireSafeText(title, "Название");
        this.assignee = (assignee == null) ? null : requireSafeText(assignee, "Исполнитель");
        this.priority = priority;
        this.status = status;
        this.deadline = deadline;
    }

    public abstract TaskType getType();

    public abstract int calculateScore();

    @Override
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAssignee() {
        return assignee;
    }

    public Priority getPriority() {
        return priority;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public String getAssigneeLabel() {
        return (assignee == null) ? "не назначен" : assignee;
    }

    public void setTitle(String title) {
        checkNotClosed();
        this.title = requireSafeText(title, "Название");
    }

    public void setPriority(Priority priority) {
        checkNotClosed();
        if (priority == null) {
            throw new IllegalArgumentException("Приоритет обязателен");
        }
        this.priority = priority;
    }

    public void assignTo(String assignee) {
        checkNotClosed();
        this.assignee = requireSafeText(assignee, "Исполнитель");
    }

    public void changeStatus(Status newStatus) throws InvalidStatusTransitionException {
        if (newStatus == null) {
            throw new IllegalArgumentException("Новый статус не задан");
        }
        if (status.isFinal()) {
            throw new InvalidStatusTransitionException("Задача #" + id + " уже закрыта (статус «" + status.getLabel() + "»), менять статус нельзя");
        }
        if (!status.canMoveTo(newStatus)) {
            throw new InvalidStatusTransitionException("Задача #" + id + ": переход «" + status.getLabel() + "» -> «" + newStatus.getLabel() + "» недопустим");
        }
        this.status = newStatus;
    }

    public boolean isOverdue(LocalDate today) {
        return deadline != null && !status.isFinal() && deadline.isBefore(today);
    }

    @Override
    public String toReportLine() {
        return "#" + id + " [" + getType().getLabel() + "] " + title + " | исполнитель: " + getAssigneeLabel() + " | приоритет: " + priority.getLabel() + " | статус: " + status.getLabel() + " | срок: " + (deadline == null ? "не задан" : deadline);
    }

    @Override
    public String toString() {
        return toReportLine();
    }

    private void checkNotClosed() {
        if (status.isFinal()) {
            throw new IllegalStateException("Задача #" + id + " закрыта (статус «" + status.getLabel() + "»), изменять её нельзя");
        }
    }

    private static String requireSafeText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        String text = value.trim();
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(fieldName + " не должно быть длиннее " + MAX_TEXT_LENGTH + " символов");
        }
        if (text.indexOf(';') >= 0 || text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(fieldName + " не должно содержать символ ';' и переносы строк");
        }
        return text;
    }
}
