package taskmanager.model;

public enum TaskType {
    REGULAR("Обычная"),
    URGENT("Срочная");

    private final String label;

    TaskType(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
