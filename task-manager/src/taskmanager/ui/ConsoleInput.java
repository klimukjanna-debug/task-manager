package taskmanager.ui;

import java.time.LocalDate;
import java.util.Scanner;
import java.util.function.Function;
public class ConsoleInput {

    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt) {
        return Integer.parseInt(readLine(prompt));
    }

    public LocalDate readDate(String prompt) {
        return LocalDate.parse(readLine(prompt));
    }

    public LocalDate readOptionalDate(String prompt) {
        String line = readLine(prompt);
        return line.isEmpty() ? null : LocalDate.parse(line);
    }

    public <E> E readEnum(String title, E[] values, Function<E, String> labeler) {
        System.out.println(title + ":");
        for (int i = 0; i < values.length; i++) {
            System.out.println("  " + (i + 1) + " - " + labeler.apply(values[i]));
        }
        int number = readInt("Ваш выбор: ");
        if (number < 1 || number > values.length) {
            throw new IllegalArgumentException("Нужно выбрать число от 1 до " + values.length);
        }
        return values[number - 1];
    }
}
