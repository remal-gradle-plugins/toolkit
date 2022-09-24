package name.remal.gradleplugins.toolkit;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class PluginManagerUtilsTest {

    private final Project project;

    @Test
    void withAnyOfPlugins_first() {
        val pluginManager = project.getPluginManager();
        val executionsCounter = new AtomicInteger(0);
        PluginManagerUtils.withAnyOfPlugins(pluginManager, asList("java-library", "groovy", "groovy"), __ ->
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
        val pluginManager = project.getPluginManager();
        val executionsCounter = new AtomicInteger(0);
        PluginManagerUtils.withAnyOfPlugins(pluginManager, asList("java-library", "groovy", "groovy"), __ ->
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
        val pluginManager = project.getPluginManager();
        val executionsCounter = new AtomicInteger(0);
        PluginManagerUtils.withAllPlugins(pluginManager, asList("java-library", "groovy", "groovy"), __ ->
            executionsCounter.incrementAndGet()
        );
        assertEquals(0, executionsCounter.get());

        pluginManager.apply("java-library");
        assertEquals(0, executionsCounter.get());

        pluginManager.apply("groovy");
        assertEquals(1, executionsCounter.get());
    }

}
