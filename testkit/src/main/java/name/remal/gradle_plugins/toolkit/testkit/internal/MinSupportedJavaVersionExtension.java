package name.remal.gradle_plugins.toolkit.testkit.internal;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.MinSupportedJavaVersion;
import org.gradle.api.JavaVersion;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

@Internal
public class MinSupportedJavaVersionExtension extends AbstractSupportedJavaVersionExtension {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        val annotation = AnnotationUtils.findAnnotation(context.getElement(), MinSupportedJavaVersion.class)
            .orElse(null);
        if (annotation == null) {
            return enabled(format("@%s is not present", MinSupportedJavaVersion.class.getSimpleName()));
        }

        val minJavaVersion = JavaVersion.toVersion(annotation.value());
        val currentJavaVersion = getCurrentJavaVersion(context);
        if (currentJavaVersion.compareTo(minJavaVersion) >= 0) {
            return enabled(format(
                "Current Java version %s is greater or equal to min supported version %s",
                currentJavaVersion.getMajorVersion(),
                minJavaVersion.getMajorVersion()
            ));
        } else {
            return disabled(format(
                "Current Java version %s is less than min supported version %s",
                currentJavaVersion.getMajorVersion(),
                minJavaVersion.getMajorVersion()
            ));
        }
    }

}
