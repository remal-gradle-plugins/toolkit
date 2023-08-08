package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;
import static java.util.Collections.synchronizedSet;
import static name.remal.gradle_plugins.toolkit.ResolutionStrategyUtils.configureGlobalResolutionStrategy;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ResolutionStrategyUtilsTest {

    private final Project project;

    @BeforeEach
    void beforeEach() {
        project.getPluginManager().apply("java");
        //project.getRepositories().mavenCentral();
    }

    @Test
    void globalResolutionStrategyAppliedToAllConfigurations() {
        Set<String> requestedSelectors = synchronizedSet(new TreeSet<>());
        configureGlobalResolutionStrategy(project, resolutionStrategy ->
            resolutionStrategy.eachDependency(details ->
                requestedSelectors.add(format(
                    "%s:%s:%s",
                    details.getRequested().getGroup(),
                    details.getRequested().getName(),
                    details.getRequested().getVersion()
                ))
            )
        );

        val conf = project.getConfigurations().create("resolvableConfiguration");

        notTransitive(project.getDependencies().add(conf.getName(), "junit:junit:4.13.2"));

        try {
            conf.resolve();
        } catch (Throwable ignored) {
            // do nothing
        }

        assertThat(requestedSelectors)
            .containsExactlyInAnyOrder(
                "junit:junit:4.13.2"
            );
    }

    @Test
    void globalResolutionStrategyWorksWithSpringDependencyManagementGradlePlugin() {
        val globalResolutionStrategyCallCounter = new AtomicInteger();
        Set<String> requestedSelectors = synchronizedSet(new TreeSet<>());
        configureGlobalResolutionStrategy(project, resolutionStrategy ->
            resolutionStrategy.eachDependency(details -> {
                globalResolutionStrategyCallCounter.incrementAndGet();
                requestedSelectors.add(format(
                    "%s:%s:%s",
                    details.getRequested().getGroup(),
                    details.getRequested().getName(),
                    details.getRequested().getVersion()
                ));
            })
        );

        project.getPluginManager().apply("io.spring.dependency-management");

        val conf = project.getConfigurations().create("resolvableConfiguration");

        notTransitive(project.getDependencies().add(conf.getName(), "junit:junit:4.13.2"));

        try {
            conf.resolve();
        } catch (Throwable ignored) {
            // do nothing
        }

        assertThat(globalResolutionStrategyCallCounter.get())
            .isEqualTo(2);

        assertThat(requestedSelectors)
            .containsExactlyInAnyOrder(
                "junit:junit:4.13.2"
            );
    }


    @Nullable
    @Contract("_->param1")
    private static <T extends Dependency> T notTransitive(@Nullable T dependency) {
        if (dependency instanceof ModuleDependency) {
            ((ModuleDependency) dependency).setTransitive(false);
        }
        return dependency;
    }

}
