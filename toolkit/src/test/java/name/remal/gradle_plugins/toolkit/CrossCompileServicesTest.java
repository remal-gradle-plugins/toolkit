package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.CrossCompileVersionComparisonResult.DEPENDENCY_EQUALS_TO_CURRENT;
import static name.remal.gradle_plugins.toolkit.CrossCompileVersionComparisonResult.DEPENDENCY_GREATER_THAN_CURRENT;
import static name.remal.gradle_plugins.toolkit.CrossCompileVersionComparisonResult.DEPENDENCY_LESS_THAN_CURRENT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Test;

class CrossCompileServicesTest {

    @Test
    void isActive() {
        val impl = CrossCompileServiceImpl.builder()
            .className("test")
            .dependencyVersion(CrossCompileServiceDependencyVersion.builder()
                .dependency("test")
                .version(Version.parse("2"))
                .earlierIncluded(true)
                .selfIncluded(true)
                .build()
            )
            .build();
        assertTrue(CrossCompileServices.isActive(impl, (__, ___) -> DEPENDENCY_GREATER_THAN_CURRENT));
        assertTrue(CrossCompileServices.isActive(impl, (__, ___) -> DEPENDENCY_EQUALS_TO_CURRENT));
        assertFalse(CrossCompileServices.isActive(impl, (__, ___) -> DEPENDENCY_LESS_THAN_CURRENT));
    }

}
