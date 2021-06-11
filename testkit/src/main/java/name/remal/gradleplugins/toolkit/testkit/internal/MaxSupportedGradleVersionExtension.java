package name.remal.gradleplugins.toolkit.testkit.internal;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import lombok.val;
import name.remal.gradleplugins.toolkit.testkit.MaxSupportedGradleVersion;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

public class MaxSupportedGradleVersionExtension extends AbstractSupportedGradleVersionExtension {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        val annotation = AnnotationUtils.findAnnotation(context.getElement(), MaxSupportedGradleVersion.class)
            .orElse(null);
        if (annotation == null) {
            return enabled(format("@%s is not present", MaxSupportedGradleVersion.class.getSimpleName()));
        }

        val maxGradleVersion = GradleVersion.version(annotation.value());
        val currentGradleVersion = getCurrentGradleVersion(context);
        if (currentGradleVersion.compareTo(maxGradleVersion) <= 0) {
            return enabled(format(
                "Current Gradle version %s is less or equal to max supported version %s",
                currentGradleVersion.getVersion(),
                maxGradleVersion.getVersion()
            ));
        } else {
            return disabled(format(
                "Current Gradle version %s is greater than max supported version %s",
                currentGradleVersion.getVersion(),
                maxGradleVersion.getVersion()
            ));
        }
    }

}
