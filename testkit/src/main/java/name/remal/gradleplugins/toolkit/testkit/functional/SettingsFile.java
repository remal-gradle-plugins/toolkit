package name.remal.gradleplugins.toolkit.testkit.functional;

import static name.remal.gradleplugins.toolkit.StringUtils.escapeGroovy;

import java.io.File;

public class SettingsFile extends AbstractGradleFile<SettingsFile> {

    SettingsFile(File projectDir) {
        super(new File(projectDir, "settings.gradle"));
        append("rootProject.name = '" + escapeGroovy(projectDir.getName()) + "'");
    }

}
