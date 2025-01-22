package name.remal.gradle_plugins.toolkit;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static javax.annotation.meta.When.UNKNOWN;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import com.google.errorprone.annotations.CheckReturnValue;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Duration;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.internal.classloader.ClassLoaderHierarchy;
import org.gradle.internal.classloader.ClassLoaderSpec;
import org.gradle.internal.classloader.ClassLoaderVisitor;
import org.jetbrains.annotations.Contract;

@CustomLog
@NoArgsConstructor(access = PRIVATE)
public abstract class DebugUtils {

    private static final boolean IS_DEBUG_ENABLED = getRuntimeMXBean().getInputArguments().stream()
        .anyMatch(arg -> arg.startsWith("-agentlib:jdwp="));

    @FunctionalInterface
    public interface IfDebugEnabled {
        void execute() throws Throwable;
    }

    @Contract(pure = true)
    public static boolean isDebugEnabled() {
        return IS_DEBUG_ENABLED;
    }

    @SneakyThrows
    public static void ifDebugEnabled(IfDebugEnabled action) {
        if (isDebugEnabled()) {
            action.execute();
        }
    }


    public static void dumpClassLoaderToLog(@Nullable ClassLoader classLoader) {
        logger.quiet(dumpClassLoader(classLoader));
    }

    @CheckReturnValue
    @ReliesOnInternalGradleApi
    @SuppressWarnings("java:S3776")
    public static String dumpClassLoader(@Nullable ClassLoader classLoader) {
        var message = new StringBuilder();

        while (true) {
            if (message.length() > 0) {
                message.append('\n');
            }

            message.append(classLoader);

            if (classLoader == null) {
                break;
            }

            if (classLoader instanceof ClassLoaderHierarchy) {
                ((ClassLoaderHierarchy) classLoader).visit(new ClassLoaderVisitor() {
                    @Override
                    public void visitSpec(ClassLoaderSpec spec) {
                        message.append("\n  Spec: ").append(spec);
                    }

                    @Override
                    public void visitClassPath(URL[] urls) {
                        if (isEmpty(urls)) {
                            message.append("\n  Empty classpath");
                        } else {
                            message.append("\n  Classpath:");
                            for (var url : urls) {
                                message.append("\n    ").append(url);
                            }
                        }
                    }

                    @Override
                    public void visitParent(ClassLoader parentClassLoader) {
                        // do nothing
                    }
                });

            } else if (classLoader instanceof URLClassLoader) {
                var urls = ((URLClassLoader) classLoader).getURLs();
                if (isEmpty(urls)) {
                    message.append("\n  Empty classpath");
                } else {
                    message.append("\n  Classpath:");
                    for (var url : urls) {
                        message.append("\n    ").append(url);
                    }
                }
            }


            var parentClassLoader = classLoader.getParent();
            if (parentClassLoader != null) {
                classLoader = parentClassLoader;
            } else {
                break;
            }
        }

        return message.toString();
    }


    private static final long MAX_NANOS_TO_DISPLAY_IN_NANOS = Duration.ofMillis(1).toNanos();
    private static final long MAX_NANOS_TO_DISPLAY_IN_MILLIS = Duration.ofMinutes(1).toNanos();

    @Nonnull(when = UNKNOWN)
    @SneakyThrows
    public static <T> T logTiming(String timerName, Callable<T> action) {
        var startNanos = System.nanoTime();
        try {
            return action.call();

        } finally {
            var durationNanos = System.nanoTime() - startNanos;
            if (durationNanos <= MAX_NANOS_TO_DISPLAY_IN_NANOS) {
                logger.quiet("{} took {} nanos", timerName, durationNanos);
            } else if (durationNanos <= MAX_NANOS_TO_DISPLAY_IN_MILLIS) {
                logger.quiet("{} took {} millis", timerName, NANOSECONDS.toMillis(durationNanos));
            } else {
                logger.quiet("{} took {} seconds", timerName, NANOSECONDS.toSeconds(durationNanos));
            }
        }
    }

    public static void logTiming(String timerName, Runnable action) {
        logTiming(timerName, () -> {
            action.run();
            return null;
        });
    }

}
