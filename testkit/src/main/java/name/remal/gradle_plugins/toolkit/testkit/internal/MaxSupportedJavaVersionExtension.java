package name.remal.gradle_plugins.toolkit.testkit.internal;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import name.remal.gradle_plugins.toolkit.testkit.MaxSupportedJavaVersion;
import org.gradle.api.JavaVersion;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

@Internal
public class MaxSupportedJavaVersionExtension extends AbstractSupportedJavaVersionExtension {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        var annotation = AnnotationUtils.findAnnotation(context.getElement(), MaxSupportedJavaVersion.class)
            .orElse(null);
        if (annotation == null) {
            return enabled(format("@%s is not present", MaxSupportedJavaVersion.class.getSimpleName()));
        }

        var maxJavaVersion = JavaVersion.toVersion(annotation.value());
        var currentJavaVersion = getCurrentJavaVersion(context);
        if (currentJavaVersion.compareTo(maxJavaVersion) <= 0) {
            return enabled(format(
                "Current Java version %s is less or equal to max supported version %s",
                currentJavaVersion.getMajorVersion(),
                maxJavaVersion.getMajorVersion()
            ));
        } else {
            return disabled(format(
                "Current Java version %s is greater than max supported version %s",
                currentJavaVersion.getMajorVersion(),
                maxJavaVersion.getMajorVersion()
            ));
        }
    }

}
