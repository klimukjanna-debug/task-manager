package taskmanager.model;

import java.time.LocalDate;

public class RegularTask extends Task {

    public RegularTask(int id, String title, String assignee, Priority priority,
                       Status status, LocalDate deadline) {
        super(id, title, assignee, priority, status, deadline);
    }

    @Override
    public TaskType getType() {
        return TaskType.REGULAR;
    }

    @Override
    public int calculateScore() {
        return getPriority().getWeight();
    }
}
