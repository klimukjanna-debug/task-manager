package taskmanager.model;


public enum Priority {

    LOW("Низкий", 1),
    MEDIUM("Средний", 2),
    HIGH("Высокий", 3);

    private final String label;
    private final int weight;


    Priority(String label, int weight) {
        this.label = label;
        this.weight = weight;
    }

    public String getLabel() { return label; }
    public int getWeight() { return weight; }
}
