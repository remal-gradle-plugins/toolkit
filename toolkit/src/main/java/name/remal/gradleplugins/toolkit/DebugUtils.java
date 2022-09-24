package name.remal.gradleplugins.toolkit;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = PRIVATE)
public abstract class DebugUtils {

    private static final boolean IS_DEBUG_ENABLED = getRuntimeMXBean().getInputArguments().toString().contains("jdwp");

    @FunctionalInterface
    public interface IfDebugEnabled {
        void execute() throws Throwable;
    }

    @SneakyThrows
    public static void ifDebugEnabled(IfDebugEnabled action) {
        if (IS_DEBUG_ENABLED) {
            action.execute();
        }
    }

}
