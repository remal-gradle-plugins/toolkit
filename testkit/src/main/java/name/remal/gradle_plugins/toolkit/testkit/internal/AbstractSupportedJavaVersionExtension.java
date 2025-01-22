package name.remal.gradle_plugins.toolkit.testkit.internal;

import org.gradle.api.JavaVersion;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.VisibleForTesting;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

@Internal
abstract class AbstractSupportedJavaVersionExtension implements ExecutionCondition {

    @VisibleForTesting
    static final Namespace NAMESPACE = Namespace.create(AbstractSupportedJavaVersionExtension.class);

    protected static JavaVersion getCurrentJavaVersion(ExtensionContext context) {
        var currentJavaVersionRetriever = context.getStore(NAMESPACE).getOrComputeIfAbsent(
            CurrentJavaVersionRetriever.class,
            __ -> new DefaultCurrentJavaVersionRetriever(),
            CurrentJavaVersionRetriever.class
        );

        var javaVersion = currentJavaVersionRetriever.getCurrentJavaVersion();
        return javaVersion;
    }


    @FunctionalInterface
    @VisibleForTesting
    interface CurrentJavaVersionRetriever {
        JavaVersion getCurrentJavaVersion();
    }

    private static class DefaultCurrentJavaVersionRetriever implements CurrentJavaVersionRetriever {
        @Override
        public JavaVersion getCurrentJavaVersion() {
            return JavaVersion.current();
        }
    }

}
