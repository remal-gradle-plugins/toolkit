package name.remal.gradleplugins.toolkit;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.net.URL;
import java.net.URLClassLoader;
import javax.annotation.Nullable;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.internal.classloader.ClassLoaderHierarchy;
import org.gradle.internal.classloader.ClassLoaderSpec;
import org.gradle.internal.classloader.ClassLoaderVisitor;

@CustomLog
@NoArgsConstructor(access = PRIVATE)
public abstract class DebugUtils {

    private static final boolean IS_DEBUG_ENABLED = getRuntimeMXBean().getInputArguments().toString().contains("jdwp");

    @FunctionalInterface
    public interface IfDebugEnabled {
        void execute() throws Throwable;
    }

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

    public static String dumpClassLoader(@Nullable ClassLoader classLoader) {
        val message = new StringBuilder();
        dumpClassLoader(message, classLoader);
        return message.toString();
    }

    @ReliesOnInternalGradleApi
    @SuppressWarnings("java:S3776")
    private static void dumpClassLoader(StringBuilder message, @Nullable ClassLoader classLoader) {
        if (message.length() > 0) {
            message.append('\n');
        }

        message.append(classLoader);

        if (classLoader == null) {
            return;
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
                        for (val url : urls) {
                            message.append("\n    ").append(url);
                        }
                    }
                }

                @Override
                public void visitParent(ClassLoader classLoader) {
                    // do nothing
                }
            });

        } else if (classLoader instanceof URLClassLoader) {
            val urls = ((URLClassLoader) classLoader).getURLs();
            if (isEmpty(urls)) {
                message.append("\n  Empty classpath");
            } else {
                message.append("\n  Classpath:");
                for (val url : urls) {
                    message.append("\n    ").append(url);
                }
            }
        }


        val parentClassLoader = classLoader.getParent();
        if (parentClassLoader != null) {
            dumpClassLoader(message, parentClassLoader);
        }
    }

}
