package name.remal.gradle_plugins.toolkit.classpath;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.val;
import org.junit.jupiter.api.Test;

class ClassesIndexTest {

    @Test
    void singleMapExpand() {
        val classesIndex = new ClassesIndex(ImmutableMap.of(
            "1", ImmutableSet.of("2", "3"),
            "2", ImmutableSet.of("4"),
            "4", ImmutableSet.of("5")
        ));

        assertThat(classesIndex.parentClassNames)
            .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                "1", ImmutableSet.of("2", "3", "4", "5"),
                "2", ImmutableSet.of("4", "5"),
                "4", ImmutableSet.of("5")
            ));
    }

    @Test
    void singleMapExpand_with_system_classes() {
        val classesIndex = new ClassesIndex(ImmutableMap.of(
            "1", ImmutableSet.of("java.util.Collection")
        ));

        assertThat(classesIndex.parentClassNames)
            .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                "1", ImmutableSet.of("java.util.Collection", "java.lang.Iterable")
            ));
    }

    @Test
    void multipleMapExpand() {
        val classesIndex1 = new ClassesIndex(ImmutableMap.of(
            "1", ImmutableSet.of("2", "3"),
            "3", ImmutableSet.of("4")
        ));
        val classesIndex2 = new ClassesIndex(ImmutableMap.of(
            "2", ImmutableSet.of("java.util.Collection")
        ));
        val classesIndex = new ClassesIndex(ImmutableList.of(
            classesIndex1,
            classesIndex2
        ));

        assertThat(classesIndex.parentClassNames)
            .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                "1", ImmutableSet.of("2", "3", "4", "java.util.Collection", "java.lang.Iterable"),
                "3", ImmutableSet.of("4"),
                "2", ImmutableSet.of("java.util.Collection", "java.lang.Iterable")
            ));
    }

}
