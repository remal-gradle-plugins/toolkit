package name.remal.gradle_plugins.toolkit;

import static org.assertj.core.api.Assertions.assertThat;

import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ClosureUtilsTest {

    final Project project;

    @Test
    void configureWith() {
        ClosureUtils.configureWith(project, new Closure<>(this) {
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

    @Test
    void configureUsing() {
        var action = ClosureUtils.configureUsing(new Closure<>(this) {
            @Override
            @Nullable
            public Object call() {
                assertThat(this.getDelegate()).isSameAs(project);
                project.setVersion("test");
                return null;
            }
        });
        action.execute(project);
        assertThat(project.getVersion()).hasToString("test");
    }

}
