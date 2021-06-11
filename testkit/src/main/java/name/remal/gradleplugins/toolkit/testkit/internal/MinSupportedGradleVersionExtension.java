package name.remal.gradleplugins.toolkit.testkit.internal;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import lombok.val;
import name.remal.gradleplugins.toolkit.testkit.MinSupportedGradleVersion;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

public class MinSupportedGradleVersionExtension extends AbstractSupportedGradleVersionExtension {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        val annotation = AnnotationUtils.findAnnotation(context.getElement(), MinSupportedGradleVersion.class)
            .orElse(null);
        if (annotation == null) {
            return enabled(format("@%s is not present", MinSupportedGradleVersion.class.getSimpleName()));
        }

        val minGradleVersion = GradleVersion.version(annotation.value());
        val currentGradleVersion = getCurrentGradleVersion(context);
        if (currentGradleVersion.compareTo(minGradleVersion) >= 0) {
            return enabled(format(
                "Current Gradle version %s is greater or equal to min supported version %s",
                currentGradleVersion.getVersion(),
                minGradleVersion.getVersion()
            ));
        } else {
            return disabled(format(
                "Current Gradle version %s is less than min supported version %s",
                currentGradleVersion.getVersion(),
                minGradleVersion.getVersion()
            ));
        }
    }

}
