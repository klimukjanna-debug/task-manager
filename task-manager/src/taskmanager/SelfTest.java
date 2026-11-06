package taskmanager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import taskmanager.exception.InvalidStatusTransitionException;
import taskmanager.exception.TaskNotFoundException;
import taskmanager.model.Priority;
import taskmanager.model.Status;
import taskmanager.model.Task;
import taskmanager.model.TaskType;
import taskmanager.service.ReportService;
import taskmanager.service.TaskService;
import taskmanager.storage.FileTaskStorage;

public final class SelfTest {

    private static int passed;
    private static int failed;

    private SelfTest() { }

    private interface Action {
        void run() throws Exception;
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[FAIL] ") + name);
        if (ok) {
            passed++;
        } else {
            failed++;
        }
    }

    private static void expect(String name, Class<? extends Exception> expected, Action action) {
        try {
            action.run();
            check(name + " (исключение не выброшено!)", false);
        } catch (Exception e) {
            check(name + " -> " + e.getClass().getSimpleName(), expected.isInstance(e));
        }
    }

    public static boolean run() {
        passed = 0;
        failed = 0;
        System.out.println("\nАВТОТЕСТ");
        Path file = null;
        try {
            file = Files.createTempFile("tasks-selftest", ".txt");
            Files.delete(file);
            runAll(file);
        } catch (Exception e) {
            check("Непредвиденная ошибка: " + e, false);
        } finally {
            if (file != null) {
                try {
                    Files.deleteIfExists(file);
                    Files.deleteIfExists(Path.of(file + ".tmp"));
                } catch (IOException ignored) {

                }
            }
        }
        System.out.println("Итого: пройдено " + passed + " из " + (passed + failed));
        return failed == 0;
    }

    private static void runAll(Path file) throws Exception {
        final TaskService s = new TaskService(new FileTaskStorage(file));

        check("Файла нет: загрузка даёт 0 задач без ошибки",
                s.load().isEmpty() && s.getAll().isEmpty());

        Task t1 = s.createTask(TaskType.REGULAR, "Подготовить ТЗ", Priority.MEDIUM, null);
        Task t2 = s.createTask(TaskType.URGENT, "Исправить критический баг", Priority.LOW,
                LocalDate.of(2026, 1, 1));
        check("ID выдаются по порядку: 1 и 2", t1.getId() == 1 && t2.getId() == 2);
        check("Новая задача: статус NEW, исполнитель не назначен",
                t1.getStatus() == Status.NEW && t1.getAssignee() == null);
        check("Полиморфизм: срочная (LOW) важнее обычной (MEDIUM)",
                t2.calculateScore() > t1.calculateScore());
        check("Переопределение: у срочной в строке отчёта есть «СРОЧНО!»",
                t2.toReportLine().startsWith("СРОЧНО!") && !t1.toReportLine().startsWith("СРОЧНО!"));

        expect("Пустое название", IllegalArgumentException.class,
                () -> s.createTask(TaskType.REGULAR, "   ", Priority.LOW, null));
        expect("Символ ';' в названии", IllegalArgumentException.class,
                () -> s.createTask(TaskType.REGULAR, "a;b", Priority.LOW, null));
        expect("Срочная задача без срока", IllegalArgumentException.class,
                () -> s.createTask(TaskType.URGENT, "Без срока", Priority.HIGH, null));
        Task t3 = s.createTask(TaskType.REGULAR, "Написать тесты", Priority.HIGH,
                LocalDate.of(2026, 12, 31));
        check("Неудачные попытки не расходуют ID: следующий - 3", t3.getId() == 3);
        expect("setTitle с пустым значением", IllegalArgumentException.class,
                () -> t3.setTitle(" "));

        s.assignTask(1, "Иван");
        s.assignTask(2, "Иван");
        s.assignTask(3, "Анна");
        check("find(String): задач Ивана 2 (регистр не важен)", s.find("иван").size() == 2);
        check("find(Priority): HIGH - 1", s.find(Priority.HIGH).size() == 1);
        check("find(String, Priority): Иван + LOW - 1", s.find("Иван", Priority.LOW).size() == 1);
        expect("Пустой исполнитель при назначении", IllegalArgumentException.class,
                () -> s.assignTask(1, ""));

        s.changeStatus(1, Status.IN_PROGRESS);
        expect("NEW -> DONE недопустим", InvalidStatusTransitionException.class,
                () -> s.changeStatus(3, Status.DONE));
        s.changeStatus(1, Status.DONE);
        check("IN_PROGRESS -> DONE разрешён", s.getById(1).getStatus() == Status.DONE);
        expect("Из закрытой задачи выходить нельзя", InvalidStatusTransitionException.class,
                () -> s.changeStatus(1, Status.IN_PROGRESS));
        expect("Исполнителя закрытой задаче назначить нельзя", IllegalStateException.class,
                () -> s.assignTask(1, "Пётр"));
        expect("Несуществующий ID", TaskNotFoundException.class,
                () -> s.changeStatus(99, Status.DONE));

        String report = new ReportService().buildProjectReport(s.getAll(), LocalDate.of(2026, 6, 1));
        check("Отчёт: выполнение 33.3% (1 из 3)", report.contains("33.3%"));
        check("Отчёт: есть просроченная задача (срок 2026-01-01)",
                report.contains("Просроченные задачи (на 2026-06-01): 1"));

        TaskService s2 = new TaskService(new FileTaskStorage(file));
        List<String> warnings = s2.load();
        check("Перезапуск: загружено 3 задачи без предупреждений",
                s2.getAll().size() == 3 && warnings.isEmpty());
        check("Перезапуск: статус, исполнитель и вид сохранены",
                s2.getById(1).getStatus() == Status.DONE
                        && "Иван".equals(s2.getById(2).getAssignee())
                        && s2.getById(2).getType() == TaskType.URGENT);
        check("Кириллица читается без искажений (UTF-8)",
                s2.getById(2).getTitle().equals("Исправить критический баг"));
        Task t4 = s2.createTask(TaskType.REGULAR, "Новая", Priority.LOW, null);
        check("Нумерация продолжается: новый ID - 4", t4.getId() == 4);

        s2.removeTask(4);
        check("Удаление: осталось 3 задачи", s2.getAll().size() == 3);
        expect("Повторное удаление", TaskNotFoundException.class, () -> s2.removeTask(4));

        Files.writeString(file, String.join("\n",
                "# заголовок",
                "REGULAR;1;Задача А;;LOW;NEW;",
                "мусор",
                "REGULAR;2;Задача Б;;SUPER;NEW;",
                "URGENT;3;Срочная без срока;;HIGH;NEW;",
                "REGULAR;1;Дубликат;;LOW;NEW;"), StandardCharsets.UTF_8);
        TaskService s3 = new TaskService(new FileTaskStorage(file));
        List<String> w3 = s3.load();
        check("Битые строки пропущены: 1 задача, 4 предупреждения",
                s3.getAll().size() == 1 && w3.size() == 4);

        Files.writeString(file, "", StandardCharsets.UTF_8);
        TaskService s4 = new TaskService(new FileTaskStorage(file));
        check("Пустой файл: 0 задач, 0 предупреждений",
                s4.load().isEmpty() && s4.getAll().isEmpty());
        check("Отчёт по пустому списку не падает",
                new ReportService().buildProjectReport(s4.getAll(), LocalDate.now()).contains("0.0%"));
    }
}
