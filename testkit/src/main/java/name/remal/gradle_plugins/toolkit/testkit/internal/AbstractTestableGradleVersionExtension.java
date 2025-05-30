package name.remal.gradle_plugins.toolkit.testkit.internal;

import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.VisibleForTesting;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

@Internal
abstract class AbstractTestableGradleVersionExtension implements ExecutionCondition {

    @VisibleForTesting
    static final Namespace NAMESPACE = Namespace.create(AbstractTestableGradleVersionExtension.class);

    protected static GradleVersion getCurrentGradleVersion(ExtensionContext context) {
        var currentGradleVersionRetriever = context.getStore(NAMESPACE).getOrComputeIfAbsent(
            CurrentGradleVersionRetriever.class,
            __ -> new DefaultCurrentGradleVersionRetriever(),
            CurrentGradleVersionRetriever.class
        );

        var gradleVersion = currentGradleVersionRetriever.getCurrentGradleVersion();
        return gradleVersion;
    }


    @FunctionalInterface
    @VisibleForTesting
    interface CurrentGradleVersionRetriever {
        GradleVersion getCurrentGradleVersion();
    }

    private static class DefaultCurrentGradleVersionRetriever implements CurrentGradleVersionRetriever {
        @Override
        public GradleVersion getCurrentGradleVersion() {
            return GradleVersion.current().getBaseVersion();
        }
    }

}
