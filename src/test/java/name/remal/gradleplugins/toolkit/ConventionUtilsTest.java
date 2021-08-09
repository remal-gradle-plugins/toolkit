package name.remal.gradleplugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ConventionUtilsTest {

    private final Project project;

    public static class ExamplePlugin {
        public static final String STRING = "example";

        public String getExampleString() {
            return STRING;
        }
    }


    @Test
    void getConvention() {
        assertNotNull(ConventionUtils.getConvention(project));
    }

    @Test
    void addConventionPlugin_named() {
        val groovyObject = (GroovyObject) project;
        assertThrows(MissingPropertyException.class, () -> groovyObject.getProperty("exampleString"));

        val plugin = new ExamplePlugin();
        ConventionUtils.addConventionPlugin(project, "examplePlugin", plugin);

        val convention = ConventionUtils.getConvention(project);
        assertSame(plugin, convention.getPlugin(ExamplePlugin.class));
        assertSame(plugin, convention.getPlugins().get("examplePlugin"));

        assertEquals(ExamplePlugin.STRING, groovyObject.getProperty("exampleString"));
    }

    @Test
    void addConventionPlugin_not_named() {
        val groovyObject = (GroovyObject) project;
        assertThrows(MissingPropertyException.class, () -> groovyObject.getProperty("exampleString"));

        val plugin = new ExamplePlugin();
        ConventionUtils.addConventionPlugin(project, plugin);

        val convention = ConventionUtils.getConvention(project);
        assertSame(plugin, convention.getPlugin(ExamplePlugin.class));
        assertSame(plugin, convention.getPlugins().get(ExamplePlugin.class.getName()));

        assertEquals(ExamplePlugin.STRING, groovyObject.getProperty("exampleString"));
    }

}
