package name.remal.gradle_plugins.toolkit.testkit.internal;

import static java.lang.String.format;
import static java.lang.String.join;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import java.util.Collection;
import java.util.LinkedHashSet;
import name.remal.gradle_plugins.toolkit.Version;
import name.remal.gradle_plugins.toolkit.testkit.MinTestableVersion;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

@Internal
public class MinTestableVersionExtension extends AbstractTestableVersionExtension {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        var annotations = AnnotationUtils.findRepeatableAnnotations(context.getElement(), MinTestableVersion.class);
        if (annotations.isEmpty()) {
            return enabled(format("@%s is not present", MinTestableVersion.class.getSimpleName()));
        }

        Collection<String> enabledReasons = new LinkedHashSet<>();
        Collection<String> disabledReasons = new LinkedHashSet<>();
        for (var annotation : annotations) {
            var module = annotation.module();
            var moduleVersion = getModuleVersion(context, module);

            var minVersion = Version.parse(annotation.version());
            if (moduleVersion.compareTo(minVersion) >= 0) {
                enabledReasons.add(format(
                    "Module %s version %s is greater or equal to min supported version %s",
                    module,
                    moduleVersion,
                    minVersion
                ));
            } else {
                disabledReasons.add(format(
                    "Module %s version %s is less than min supported version %s",
                    module,
                    moduleVersion,
                    minVersion
                ));
            }
        }

        if (!disabledReasons.isEmpty()) {
            return disabled(join("\n", disabledReasons));
        } else {
            return enabled(join("\n", enabledReasons));
        }
    }

}
