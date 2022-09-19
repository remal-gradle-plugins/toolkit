package name.remal.gradleplugins.toolkit;

public class CrossCompileServiceLoadingException extends IllegalStateException {

    CrossCompileServiceLoadingException(String message) {
        super(message);
    }

    CrossCompileServiceLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    CrossCompileServiceLoadingException(Throwable cause) {
        super(cause);
    }

}
