package name.remal.gradle_plugins.toolkit.testkit.internal;

import static java.lang.String.format;

import name.remal.gradle_plugins.toolkit.Version;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.VisibleForTesting;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

@Internal
abstract class AbstractTestableVersionExtension implements ExecutionCondition {

    @VisibleForTesting
    static final Namespace NAMESPACE = Namespace.create(AbstractTestableVersionExtension.class);

    protected static Version getModuleVersion(ExtensionContext context, String module) {
        var moduleVersionStringRetriever = context.getStore(NAMESPACE).getOrComputeIfAbsent(
            ModuleVersionStringRetriever.class,
            __ -> new SystemPropertiesModuleVersionStringRetriever(),
            ModuleVersionStringRetriever.class
        );

        var moduleVersionString = moduleVersionStringRetriever.getModuleVersionString(module);
        var moduleVersion = Version.parse(moduleVersionString);
        return moduleVersion;
    }


    @FunctionalInterface
    @VisibleForTesting
    interface ModuleVersionStringRetriever {
        String getModuleVersionString(String module);
    }

    private static class SystemPropertiesModuleVersionStringRetriever implements ModuleVersionStringRetriever {
        @Override
        public String getModuleVersionString(String module) {
            var modulePropertyName = format("%s.module-version", module);
            var moduleVersionString = System.getProperty(modulePropertyName);
            if (moduleVersionString == null) {
                throw new IllegalStateException(format(
                    "Fail to evaluate min/max supported version condition: %s system property is not set",
                    modulePropertyName
                ));
            } else if (moduleVersionString.isEmpty()) {
                throw new IllegalStateException(format(
                    "Fail to evaluate min/max supported version condition: %s system property is empty",
                    modulePropertyName
                ));
            }
            return moduleVersionString;
        }
    }

}
