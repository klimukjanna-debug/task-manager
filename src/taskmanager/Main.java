package taskmanager;

import java.nio.file.Path;
import java.util.Scanner;

import taskmanager.model.Priority;
import taskmanager.model.Status;
import taskmanager.model.Task;
import taskmanager.model.TaskType;
import taskmanager.service.ReportService;
import taskmanager.service.TaskService;
import taskmanager.storage.FileTaskStorage;
import taskmanager.storage.TaskStorage;
import taskmanager.ui.ConsoleInput;
import taskmanager.ui.ConsoleMenu;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--test")) {
            SelfTest.run();
            return;
        }
        TaskStorage storage = new FileTaskStorage(Path.of("tasks.txt"));
        TaskService service = new TaskService(storage);
        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleMenu(service, new ReportService(), new ConsoleInput(scanner)).run();
        }
    }
}
