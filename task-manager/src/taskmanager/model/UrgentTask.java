package taskmanager.model;

import java.time.LocalDate;

public class UrgentTask extends Task {

    public UrgentTask(int id, String title, String assignee, Priority priority,
                      Status status, LocalDate deadline) {
        super(id, title, assignee, priority, status, requireDeadline(deadline));
    }

    private static LocalDate requireDeadline(LocalDate deadline) {
        if (deadline == null) {
            throw new IllegalArgumentException("Срочная задача обязана иметь срок выполнения");
        }
        return deadline;
    }

    @Override
    public TaskType getType() {
        return TaskType.URGENT;
    }

    @Override
    public int calculateScore() {
        return getPriority().getWeight() * 10;
    }

    @Override
    public String toReportLine() {
        return "СРОЧНО! " + super.toReportLine();
    }
}
