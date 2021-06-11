package name.remal.gradleplugins.toolkit.testkit.internal;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.val;
import name.remal.gradleplugins.toolkit.Version;
import name.remal.gradleplugins.toolkit.testkit.MaxSupportedVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

public class MaxSupportedVersionExtension extends AbstractSupportedVersionExtension {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        val annotations = AnnotationUtils.findRepeatableAnnotations(context.getElement(), MaxSupportedVersion.class);
        if (annotations.isEmpty()) {
            return enabled(format("@%s is not present", MaxSupportedVersion.class.getSimpleName()));
        }

        Collection<String> enabledReasons = new LinkedHashSet<>();
        Collection<String> disabledReasons = new LinkedHashSet<>();
        for (val annotation : annotations) {
            val module = annotation.module();
            val moduleVersion = getModuleVersion(context, module);

            val maxVersion = Version.parse(annotation.version());
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
