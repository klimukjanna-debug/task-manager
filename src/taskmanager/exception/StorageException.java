package taskmanager.exception;

/** Проверяемое исключение: ошибка работы с файлом данных (обёртка над IOException). */
public class StorageException extends Exception {
    private static final long serialVersionUID = 1L; // версия для сериализации (убирает предупреждение IDE)

    public StorageException(String message, Throwable cause) {
        super(message, cause); // cause - исходная причина (например IOException) сохраняется
    }
}
