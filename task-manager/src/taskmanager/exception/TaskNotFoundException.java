package taskmanager.exception;

public class TaskNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public TaskNotFoundException(int id) {
        super("Задача с ID " + id + " не найдена");
    }
}
