package name.remal.gradleplugins.testkit.internal;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.val;
import name.remal.gradleplugins.testkit.MinSupportedVersion;
import name.remal.gradleplugins.toolkit.Version;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

public class MinSupportedVersionExtension extends AbstractSupportedVersionExtension {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        val annotations = AnnotationUtils.findRepeatableAnnotations(context.getElement(), MinSupportedVersion.class);
        if (annotations.isEmpty()) {
            return enabled(format("@%s is not present", MinSupportedVersion.class.getSimpleName()));
        }

        Collection<String> enabledReasons = new LinkedHashSet<>();
        Collection<String> disabledReasons = new LinkedHashSet<>();
        for (val annotation : annotations) {
            val module = annotation.module();
            val moduleVersion = getModuleVersion(context, module);

            val minVersion = Version.parse(annotation.version());
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
            return disabled(String.join("\n", disabledReasons));
        } else {
            return enabled(String.join("\n", enabledReasons));
        }
    }

}
