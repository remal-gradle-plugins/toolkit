package name.remal.gradle_plugins.toolkit;

import static org.assertj.core.api.Assertions.assertThat;

import groovy.lang.Closure;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ClosureUtilsTest {

    final Project project;

    @Test
    void configureWith() {
        ClosureUtils.configureWith(project, new Closure<Object>(this) {
            @Override
            @Nullable
            public Object call() {
                assertThat(this.getDelegate()).isSameAs(project);
                project.setVersion("test");
                return null;
            }
        });
        assertThat(project.getVersion()).hasToString("test");
    }

}
