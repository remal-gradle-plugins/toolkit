package name.remal.gradle_plugins.toolkit.tests_functional;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class TestsFunctionalPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        // no-op: applying this plugin only makes this module's classes resolvable
        // in generated functional-test builds, via the TestKit plugin classpath
    }

}
