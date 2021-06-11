package name.remal.gradleplugins.testkit.internal;

import static java.lang.String.format;

import lombok.val;
import name.remal.gradleplugins.toolkit.Version;
import org.jetbrains.annotations.VisibleForTesting;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

abstract class AbstractSupportedVersionExtension implements ExecutionCondition {

    @VisibleForTesting
    static final Namespace NAMESPACE = Namespace.create(AbstractSupportedVersionExtension.class);

    protected static Version getModuleVersion(ExtensionContext context, String module) {
        val moduleVersionStringRetriever = context.getStore(NAMESPACE).getOrComputeIfAbsent(
            ModuleVersionStringRetriever.class,
            __ -> new SystemPropertiesModuleVersionStringRetriever(),
            ModuleVersionStringRetriever.class
        );

        val moduleVersionString = moduleVersionStringRetriever.getModuleVersionString(module);
        val moduleVersion = Version.parse(moduleVersionString);
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
            val modulePropertyName = format("%s.module-version", module);
            val moduleVersionString = System.getProperty(modulePropertyName);
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
