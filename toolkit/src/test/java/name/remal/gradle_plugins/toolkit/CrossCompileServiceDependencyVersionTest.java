package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Test;

class CrossCompileServiceDependencyVersionTest {

    @Test
    void intersectsWith() {
        assertIntersects(
            "1", false, true, false,
            "1", false, true, false
        );
        assertDoesNotIntersect(
            "1", false, true, false,
            "1", false, false, false
        );
        assertDoesNotIntersect(
            "1", false, true, false,
            "2", false, true, false
        );

        assertDoesNotIntersect(
            "1", true, false, false,
            "2", true, false, false
        );

        assertDoesNotIntersect(
            "1", false, false, true,
            "2", false, false, true
        );

        assertDoesNotIntersect(
            "1", true, false, false,
            "2", false, false, true
        );
        assertIntersects(
            "1", false, false, true,
            "2", true, false, false
        );
    }

    private static void assertIntersects(
        String version1, boolean earlierIncluded1, boolean selfIncluded1, boolean laterIncluded1,
        String version2, boolean earlierIncluded2, boolean selfIncluded2, boolean laterIncluded2
    ) {
        val depVer1 = CrossCompileServiceDependencyVersion.builder()
            .version(Version.parse(version1))
            .earlierIncluded(earlierIncluded1)
            .selfIncluded(selfIncluded1)
            .laterIncluded(laterIncluded1)
            .build();
        val depVer2 = CrossCompileServiceDependencyVersion.builder()
            .version(Version.parse(version2))
            .earlierIncluded(earlierIncluded2)
            .selfIncluded(selfIncluded2)
            .laterIncluded(laterIncluded2)
            .build();
        assertTrue(depVer1.intersectsWith(depVer2));
        assertTrue(depVer2.intersectsWith(depVer1));
    }

    private static void assertDoesNotIntersect(
        String version1, boolean earlierIncluded1, boolean selfIncluded1, boolean laterIncluded1,
        String version2, boolean earlierIncluded2, boolean selfIncluded2, boolean laterIncluded2
    ) {
        val depVer1 = CrossCompileServiceDependencyVersion.builder()
            .version(Version.parse(version1))
            .earlierIncluded(earlierIncluded1)
            .selfIncluded(selfIncluded1)
            .laterIncluded(laterIncluded1)
            .build();
        val depVer2 = CrossCompileServiceDependencyVersion.builder()
            .version(Version.parse(version2))
            .earlierIncluded(earlierIncluded2)
            .selfIncluded(selfIncluded2)
            .laterIncluded(laterIncluded2)
            .build();
        assertFalse(depVer1.intersectsWith(depVer2));
        assertFalse(depVer2.intersectsWith(depVer1));
    }

}
