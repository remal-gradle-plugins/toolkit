package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.CrossCompileVersionComparisonResult.DEPENDENCY_EQUALS_TO_CURRENT;
import static name.remal.gradle_plugins.toolkit.CrossCompileVersionComparisonResult.DEPENDENCY_GREATER_THAN_CURRENT;
import static name.remal.gradle_plugins.toolkit.CrossCompileVersionComparisonResult.DEPENDENCY_LESS_THAN_CURRENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import lombok.val;
import org.junit.jupiter.api.Test;

class CrossCompileVersionComparatorTest {

    @Test
    void standardVersionCrossCompileVersionComparator() throws Throwable {
        val comparator = CrossCompileVersionComparator.standardVersionCrossCompileVersionComparator(
            "test",
            "2.1"
        );

        assertNull(comparator.compareDependencyVersionToCurrentVersion(
            "other",
            "999"
        ));

        assertEquals(
            DEPENDENCY_GREATER_THAN_CURRENT,
            comparator.compareDependencyVersionToCurrentVersion(
                "test",
                "2.1.1"
            )
        );
        assertEquals(
            DEPENDENCY_GREATER_THAN_CURRENT,
            comparator.compareDependencyVersionToCurrentVersion(
                "test",
                "2.2"
            )
        );
        assertEquals(
            DEPENDENCY_GREATER_THAN_CURRENT,
            comparator.compareDependencyVersionToCurrentVersion(
                "test",
                "3"
            )
        );

        assertEquals(
            DEPENDENCY_EQUALS_TO_CURRENT,
            comparator.compareDependencyVersionToCurrentVersion(
                "test",
                "2.1.0"
            )
        );
        assertEquals(
            DEPENDENCY_EQUALS_TO_CURRENT,
            comparator.compareDependencyVersionToCurrentVersion(
                "test",
                "2.1"
            )
        );
        assertEquals(
            DEPENDENCY_EQUALS_TO_CURRENT,
            comparator.compareDependencyVersionToCurrentVersion(
                "test",
                "2"
            )
        );

        assertEquals(
            DEPENDENCY_LESS_THAN_CURRENT,
            comparator.compareDependencyVersionToCurrentVersion(
                "test",
                "2.0"
            )
        );
        assertEquals(
            DEPENDENCY_LESS_THAN_CURRENT,
            comparator.compareDependencyVersionToCurrentVersion(
                "test",
                "1.9"
            )
        );
        assertEquals(
            DEPENDENCY_LESS_THAN_CURRENT,
            comparator.compareDependencyVersionToCurrentVersion(
                "test",
                "1"
            )
        );
    }

}
