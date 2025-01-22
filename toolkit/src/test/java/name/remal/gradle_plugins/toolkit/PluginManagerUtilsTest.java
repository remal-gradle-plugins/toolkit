package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class PluginManagerUtilsTest {

    private final Project project;

    @Test
    void withAnyOfPlugins_first() {
        var pluginManager = project.getPluginManager();
        var executionsCounter = new AtomicInteger(0);
        PluginManagerUtils.withAnyOfPlugins(pluginManager, List.of("java-library", "groovy", "groovy"), __ ->
            executionsCounter.incrementAndGet()
        );
        assertEquals(0, executionsCounter.get());

        pluginManager.apply("java-library");
        assertEquals(1, executionsCounter.get());

        pluginManager.apply("groovy");
        assertEquals(1, executionsCounter.get());
    }

    @Test
    void withAnyOfPlugins_second() {
        var pluginManager = project.getPluginManager();
        var executionsCounter = new AtomicInteger(0);
        PluginManagerUtils.withAnyOfPlugins(pluginManager, List.of("java-library", "groovy", "groovy"), __ ->
            executionsCounter.incrementAndGet()
        );
        assertEquals(0, executionsCounter.get());

        pluginManager.apply("groovy");
        assertEquals(1, executionsCounter.get());

        pluginManager.apply("java-library");
        assertEquals(1, executionsCounter.get());
    }

    @Test
    void withAllPlugins() {
        var pluginManager = project.getPluginManager();
        var executionsCounter = new AtomicInteger(0);
        PluginManagerUtils.withAllPlugins(pluginManager, List.of("java-library", "groovy", "groovy"), __ ->
            executionsCounter.incrementAndGet()
        );
        assertEquals(0, executionsCounter.get());

        pluginManager.apply("java-library");
        assertEquals(0, executionsCounter.get());

        pluginManager.apply("groovy");
        assertEquals(1, executionsCounter.get());
    }

}
