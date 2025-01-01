package name.remal.gradle_plugins.toolkit;

public class TaskPropertiesException extends RuntimeException {

    TaskPropertiesException(String message) {
        super(message);
    }

    TaskPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }

}
