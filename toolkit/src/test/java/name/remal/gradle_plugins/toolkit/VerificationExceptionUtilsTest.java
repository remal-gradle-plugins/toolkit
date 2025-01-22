package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.build_time_constants.api.BuildTimeConstants.getClassName;
import static name.remal.gradle_plugins.toolkit.GradleVersionUtils.isCurrentGradleVersionGreaterThanOrEqualTo;
import static name.remal.gradle_plugins.toolkit.VerificationExceptionUtils.newVerificationException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.VerificationException;
import org.junit.jupiter.api.Test;

class VerificationExceptionUtilsTest {

    @Test
    void messageOnly() {
        var exception = newVerificationException("message");
        assertInstanceOf(GradleException.class, exception);

        var exceptionClassName = exception.getClass().getName();
        if (isCurrentGradleVersionGreaterThanOrEqualTo("7.4")) {
            assertEquals(
                getClassName(VerificationException.class),
                exceptionClassName
            );

        } else {
            assertEquals(
                getClassName(GradleException.class),
                exceptionClassName
            );
        }
    }

    @Test
    void messageAndCause() {
        var cause = new Throwable("cause");

        var exception = newVerificationException("message", cause);
        assertInstanceOf(GradleException.class, exception);

        var exceptionClassName = exception.getClass().getName();
        if (isCurrentGradleVersionGreaterThanOrEqualTo("8.2")) {
            assertEquals(
                getClassName(VerificationException.class),
                exceptionClassName
            );

        } else {
            assertEquals(
                getClassName(GradleException.class),
                exceptionClassName
            );
        }
    }

}
