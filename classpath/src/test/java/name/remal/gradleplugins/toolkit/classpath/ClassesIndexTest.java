package name.remal.gradleplugins.toolkit.classpath;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.val;
import org.junit.jupiter.api.Test;

class ClassesIndexTest {

    @Test
    void singleMapExpand() {
        val classesIndex = new ClassesIndex(Map.of(
            "1", Set.of("2", "3"),
            "2", Set.of("4"),
            "4", Set.of("5")
        ));

        assertThat(classesIndex.parentClassNames)
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                "1", Set.of("2", "3", "4", "5"),
                "2", Set.of("4", "5"),
                "4", Set.of("5")
            ));
    }

    @Test
    void singleMapExpand_with_system_classes() {
        val classesIndex = new ClassesIndex(Map.of(
            "1", Set.of("java.util.Collection")
        ));

        assertThat(classesIndex.parentClassNames)
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                "1", Set.of("java.util.Collection", "java.lang.Iterable")
            ));
    }

    @Test
    void multipleMapExpand() {
        val classesIndex1 = new ClassesIndex(Map.of(
            "1", Set.of("2", "3"),
            "3", Set.of("4")
        ));
        val classesIndex2 = new ClassesIndex(Map.of(
            "2", Set.of("java.util.Collection")
        ));
        val classesIndex = new ClassesIndex(List.of(
            classesIndex1,
            classesIndex2
        ));

        assertThat(classesIndex.parentClassNames)
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                "1", Set.of("2", "3", "4", "java.util.Collection", "java.lang.Iterable"),
                "3", Set.of("4"),
                "2", Set.of("java.util.Collection", "java.lang.Iterable")
            ));
    }

}
