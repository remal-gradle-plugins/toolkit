package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.join;
import static lombok.AccessLevel.PROTECTED;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor(access = PROTECTED)
abstract class GradleProjectTestTasksTestBase<GradleProjectType extends AbstractGradleProject<?, ?, ?, ?>> {

    protected final GradleProjectType project;

    @BeforeEach
    @OverridingMethodsMustInvokeSuper
    protected void beforeEach() {
        project.withoutPluginClasspath();
        project.withoutConfigurationCache();
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void simple() {
        project.getBuildFile().applyPlugin("java");
        GradleProjectTestTasks.configureJunitTests((JavaLikeContent) project.getBuildFile());

        project.writeTextFile("src/test/java/SimpleTest.java", join("\n",
            "import static org.junit.jupiter.api.Assertions.*;",
            "import org.junit.jupiter.api.Test;",
            "class SimpleTest {",
            "    @Test",
            "    void test() {",
            "        assertEquals(2, 1 + 1);",
            "    }",
            "}"
        ));

        project.assertBuildSuccessfully("test");
    }

}
