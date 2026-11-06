package taskmanager.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import taskmanager.SelfTest;
import taskmanager.exception.InvalidStatusTransitionException;
import taskmanager.exception.StorageException;
import taskmanager.exception.TaskNotFoundException;
import taskmanager.model.Priority;
import taskmanager.model.Status;
import taskmanager.model.Task;
import taskmanager.model.TaskType;
import taskmanager.service.ReportService;
import taskmanager.service.TaskService;
public class ConsoleMenu {

    private final TaskService service;
    private final ReportService reportService;
    private final ConsoleInput input;

    public ConsoleMenu(TaskService service, ReportService reportService, ConsoleInput input) {
        this.service = service;
        this.reportService = reportService;
        this.input = input;
    }

    public void run() {
        loadData();
        boolean running = true;
        while (running) {
            printMenu();
            try {
                int choice = input.readInt("Ваш выбор: ");
                switch (choice) {
                    case 1: printTasks(service.getAll()); break;
                    case 2: createTask(); break;
                    case 3: assignTask(); break;
                    case 4: changeStatus(); break;
                    case 5: findByAssignee(); break;
                    case 6: findByPriority(); break;
                    case 7: findByAssigneeAndPriority(); break;
                    case 8: removeTask(); break;
                    case 9: showReport(); break;
                    case 10: SelfTest.run(); break;
                    case 0:
                        running = false;
                        System.out.println("Выход из программы. Данные сохранены.");
                        break;
                    default:
                        System.out.println("Такого пункта меню нет: " + choice);
                }
            }
            catch (NumberFormatException e) {
                System.out.println("Ошибка ввода: нужно ввести целое число.");
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка ввода: дата должна быть в формате гггг-мм-дд, например 2026-12-31.");
            } catch (TaskNotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (InvalidStatusTransitionException e) {
                System.out.println("Нарушено правило статусов: " + e.getMessage());
            } catch (StorageException e) {
                System.out.println("Ошибка файла: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Некорректные данные: " + e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println("Недопустимая операция: " + e.getMessage());
            } catch (NoSuchElementException e) {
                System.out.println("Ввод завершён, выхожу из программы.");
                running = false;
            } finally {
                System.out.println("[Операция завершена]"); // выполняется всегда
            }
        }
    }

    private void loadData() {
        try {
            List<String> warnings = service.load();
            for (String warning : warnings) {
                System.out.println("Предупреждение при загрузке: " + warning);
            }
            System.out.println("Загружено задач: " + service.getAll().size());
        } catch (StorageException e) {
            System.out.println("Не удалось загрузить данные: " + e.getMessage());
            System.out.println("Работа продолжится с пустым списком.");
        }
    }

    private void printMenu() {
        System.out.println("\n=== УПРАВЛЕНИЕ ЗАДАЧАМИ ПРОЕКТА ===");
        System.out.println("1  - Показать все задачи");
        System.out.println("2  - Создать задачу");
        System.out.println("3  - Назначить исполнителя");
        System.out.println("4  - Изменить статус");
        System.out.println("5  - Поиск по исполнителю");
        System.out.println("6  - Поиск по приоритету");
        System.out.println("7  - Поиск по исполнителю и приоритету");
        System.out.println("8  - Удалить задачу");
        System.out.println("9  - Отчёт о выполнении проекта");
        System.out.println("10 - Автотест");
        System.out.println("0  - Выход");
    }

    private void printTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("(задач нет)");
            return;
        }
        for (Task task : tasks) {
            System.out.println(task); // полиморфный вызов toReportLine()
        }
    }

    private void createTask() throws StorageException {
        TaskType type = input.readEnum("Вид задачи", TaskType.values(), TaskType::getLabel);
        String title = input.readLine("Название: ");
        Priority priority = input.readEnum("Приоритет", Priority.values(), Priority::getLabel);
        LocalDate deadline = input.readOptionalDate("Срок (гггг-мм-дд, Enter - без срока): ");
        Task task = service.createTask(type, title, priority, deadline);
        System.out.println("Создана задача: " + task);
    }

    private void assignTask() throws TaskNotFoundException, StorageException {
        int id = input.readInt("ID задачи: ");
        String assignee = input.readLine("Исполнитель: ");
        service.assignTask(id, assignee);
        System.out.println("Исполнитель назначен: " + service.getById(id));
    }

    private void changeStatus() throws TaskNotFoundException, InvalidStatusTransitionException,
            StorageException {
        int id = input.readInt("ID задачи: ");
        System.out.println("Текущая: " + service.getById(id));
        Status status = input.readEnum("Новый статус", Status.values(), Status::getLabel);
        service.changeStatus(id, status);
        System.out.println("Статус изменён: " + service.getById(id));
    }

    private void findByAssignee() {
        printTasks(service.find(input.readLine("Исполнитель: ")));
    }

    private void findByPriority() {
        Priority priority = input.readEnum("Приоритет", Priority.values(), Priority::getLabel);
        printTasks(service.find(priority));
    }

    private void findByAssigneeAndPriority() {
        String assignee = input.readLine("Исполнитель: ");
        Priority priority = input.readEnum("Приоритет", Priority.values(), Priority::getLabel);
        printTasks(service.find(assignee, priority));
    }

    private void removeTask() throws TaskNotFoundException, StorageException {
        int id = input.readInt("ID задачи для удаления: ");
        service.removeTask(id);
        System.out.println("Задача #" + id + " удалена.");
    }

    private void showReport() {
        System.out.println(reportService.buildProjectReport(service.getAll(), LocalDate.now()));
    }
}
