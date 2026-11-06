package taskmanager.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import taskmanager.model.Status;
import taskmanager.model.Task;
import taskmanager.model.TaskType;
import taskmanager.util.Stats;

public class ReportService {

    public String buildProjectReport(Collection<Task> tasks, LocalDate today) {
        StringBuilder sb = new StringBuilder();
        int total = tasks.size();

        // Используем собственный обобщённый метод Stats.countBy
        Map<Status, Long> byStatus = Stats.countBy(tasks, Task::getStatus);
        Map<TaskType, Long> byType = Stats.countBy(tasks, Task::getType);
        Map<String, Long> byAssignee = Stats.countBy(tasks, Task::getAssigneeLabel);

        long done = byStatus.getOrDefault(Status.DONE, 0L);
        long cancelled = byStatus.getOrDefault(Status.CANCELLED, 0L);
        long planned = total - cancelled; // отменённые задачи в прогресс не входят
        double percent = (planned == 0) ? 0.0 : done * 100.0 / planned;

        sb.append("ОТЧЁТ О ВЫПОЛНЕНИИ ПРОЕКТА\n");
        sb.append("Всего задач: ").append(total).append("\n");
        sb.append("Выполнение проекта: ").append(String.format(Locale.US, "%.1f", percent))
          .append("% (выполнено ").append(done).append(" из ").append(planned)
          .append(", отменённые не учитываются)\n");

        sb.append("\nПо статусам:\n");
        for (Status status : Status.values()) {
            sb.append("  ").append(status.getLabel()).append(": ")
              .append(byStatus.getOrDefault(status, 0L)).append("\n");
        }

        sb.append("\nПо видам:\n");
        for (TaskType type : TaskType.values()) {
            sb.append("  ").append(type.getLabel()).append(": ")
              .append(byType.getOrDefault(type, 0L)).append("\n");
        }

        sb.append("\nПо исполнителям:\n");
        if (byAssignee.isEmpty()) {
            sb.append("  (нет задач)\n");
        }
        for (Map.Entry<String, Long> entry : byAssignee.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        List<Task> overdue = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isOverdue(today)) {
                overdue.add(task);
            }
        }
        sb.append("\nПросроченные задачи (на ").append(today).append("): ").append(overdue.size()).append("\n");
        for (Task task : overdue) {
            sb.append("  ").append(task.toReportLine()).append("\n");
        }

        List<Task> open = new ArrayList<>();
        for (Task task : tasks) {
            if (!task.getStatus().isFinal()) {
                open.add(task);
            }
        }
        open.sort(Comparator.comparingInt(Task::calculateScore).reversed()
                .thenComparingInt(Task::getId));
        sb.append("\nОткрытые задачи по важности:\n");
        if (open.isEmpty()) {
            sb.append("  (нет открытых задач)\n");
        }
        for (Task task : open) {
            sb.append("  ").append(task.toReportLine()).append("\n");
        }
        return sb.toString();
    }
}
