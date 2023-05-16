package name.remal.gradle_plugins.toolkit.testkit.functional;

import static name.remal.gradle_plugins.toolkit.StringUtils.escapeGroovy;

import java.io.File;

public class SettingsFile extends AbstractGradleFile<SettingsFile> {

    SettingsFile(File projectDir) {
        super(new File(projectDir, "settings.gradle"));
        append("rootProject.name = '" + escapeGroovy(projectDir.getName().replace('.', '_')) + "'");
    }

}
