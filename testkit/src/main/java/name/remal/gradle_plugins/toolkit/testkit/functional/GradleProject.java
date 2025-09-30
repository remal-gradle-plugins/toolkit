package name.remal.gradle_plugins.toolkit.testkit.functional;

import static name.remal.gradle_plugins.toolkit.GradleVersionUtils.isCurrentGradleVersionGreaterThanOrEqualTo;

import java.io.File;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import name.remal.gradle_plugins.generate_sources.generators.java_like.groovy.GroovyContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.groovy.GradleBuildFileContentGroovy;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.groovy.GradleBuildFileContentGroovyDefault;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.groovy.GradleSettingsFileContentGroovy;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.groovy.GradleSettingsFileContentGroovyDefault;
import org.gradle.api.Action;

public class GradleProject
    extends AbstractGradleProject<
    GroovyContent,
    GradleBuildFileContentGroovy,
    GradleSettingsFileContentGroovy,
    GradleChildProject
    > {

    public GradleProject(File projectDir) {
        super(projectDir);
    }

    @Override
    protected final String getBuildFileName() {
        return "build.gradle";
    }

    @Override
    protected final GradleBuildFileContentGroovy createBuildFileContent() {
        return new GradleBuildFileContentGroovyDefault();
    }

    @Override
    protected final String getSettingsFileName() {
        return "settings.gradle";
    }

    @Override
    protected final GradleSettingsFileContentGroovy createSettingsFileContent() {
        return new GradleSettingsFileContentGroovyDefault();
    }

    @Override
    protected final GradleChildProject createChildProject(File childProjectDir) {
        return new GradleChildProject(childProjectDir);
    }

    @Override
    protected final void injectJacocoDumperImpl() {
        settingsFile.addImport(MBeanServer.class);
        settingsFile.addImport(ManagementFactory.class);
        settingsFile.addImport(ObjectName.class);

        settingsFile.line();
        settingsFile.line("/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */");
        settingsFile.line("// Jacoco dumper logic:");

        Action<GroovyContent> mainAction = block -> {
            block.line("MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer()");
            block.line("ObjectName jacocoObjectName = ObjectName.getInstance(\"org.jacoco:type=Runtime\")");
            block.block("if (mbeanServer.isRegistered(jacocoObjectName))", ifBlock -> {
                //ifBlock.line("println '!!! dumping jacoco data !!!'");
                ifBlock.line("mbeanServer.invoke(");
                ifBlock.indent(inner -> {
                    inner.line("jacocoObjectName,");
                    inner.line("\"dump\",");
                    inner.line("[true].toArray(new Object[0]),");
                    inner.line("[\"boolean\"].toArray(new String[0])");
                });
                ifBlock.line(")");
                //ifBlock.line("println '!!! dumped jacoco data !!!'");
            });
        };

        if (isCurrentGradleVersionGreaterThanOrEqualTo("6.1")) {
            settingsFile.addImport("org.gradle.api.services.BuildService");
            settingsFile.addImport("org.gradle.api.services.BuildServiceParameters");

            settingsFile.block(
                "abstract class JacocoDumper implements BuildService<BuildServiceParameters.None>, AutoCloseable",
                classBlock -> {
                    classBlock.block("void close()", mainAction);
                }
            );

            settingsFile.line("gradle.sharedServices.registerIfAbsent(");
            settingsFile.indent(inner -> {
                inner.line("\"%s\",", settingsFile.escapeString(GradleKtsProject.class.getName() + ":jacocoDumper"));
                inner.line("JacocoDumper,");
                inner.line("{ }");
            });
            settingsFile.line(").get()");

        } else {
            settingsFile.block("gradle.buildFinished", mainAction);
        }
    }

}
