package name.remal.gradle_plugins.toolkit.testkit.internal;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import name.remal.gradle_plugins.toolkit.testkit.MaxTestableGradleVersion;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

@Internal
public class MaxTestableGradleVersionExtension extends AbstractTestableGradleVersionExtension {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        var annotation = AnnotationUtils.findAnnotation(context.getElement(), MaxTestableGradleVersion.class)
            .orElse(null);
        if (annotation == null) {
            return enabled(format("@%s is not present", MaxTestableGradleVersion.class.getSimpleName()));
        }

        var maxGradleVersion = GradleVersion.version(annotation.value()).getBaseVersion();
        var currentGradleVersion = getCurrentGradleVersion(context).getBaseVersion();
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
