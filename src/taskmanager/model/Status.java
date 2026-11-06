package taskmanager.model;

public enum Status {
    NEW("Новая"),
    IN_PROGRESS("В работе"),
    DONE("Выполнена"),
    CANCELLED("Отменена");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }


    public boolean canMoveTo(Status next) {
        switch (this) {
            case NEW:
                return next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS:
                return next == DONE || next == CANCELLED;
            default: // DONE и CANCELLED
                return false;
        }
    }

    public boolean isFinal() {
        return this == DONE || this == CANCELLED;
    }
}
