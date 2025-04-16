package name.remal.gradle_plugins.toolkit.testkit.internal;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import java.util.Collection;
import java.util.LinkedHashSet;
import name.remal.gradle_plugins.toolkit.Version;
import name.remal.gradle_plugins.toolkit.testkit.MaxTestableVersion;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

@Internal
public class MaxTestableVersionExtension extends AbstractTestableVersionExtension {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        var annotations = AnnotationUtils.findRepeatableAnnotations(context.getElement(), MaxTestableVersion.class);
        if (annotations.isEmpty()) {
            return enabled(format("@%s is not present", MaxTestableVersion.class.getSimpleName()));
        }

        Collection<String> enabledReasons = new LinkedHashSet<>();
        Collection<String> disabledReasons = new LinkedHashSet<>();
        for (var annotation : annotations) {
            var module = annotation.module();
            var moduleVersion = getModuleVersion(context, module);

            var maxVersion = Version.parse(annotation.version());
            if (moduleVersion.compareTo(maxVersion) <= 0) {
                enabledReasons.add(format(
                    "Module %s version %s is less or equal to max supported version %s",
                    module,
                    moduleVersion,
                    maxVersion
                ));
            } else {
                disabledReasons.add(format(
                    "Module %s version %s is greater than max supported version %s",
                    module,
                    moduleVersion,
                    maxVersion
                ));
            }
        }

        if (!disabledReasons.isEmpty()) {
            return disabled(String.join("\n", disabledReasons));
        } else {
            return enabled(String.join("\n", enabledReasons));
        }
    }

}
