package name.remal.gradle_plugins.toolkit.testkit.functional;

import static name.remal.gradle_plugins.toolkit.GradleVersionUtils.isCurrentGradleVersionGreaterThanOrEqualTo;

import java.io.File;
import java.lang.management.ManagementFactory;
import javax.management.ObjectName;
import name.remal.gradle_plugins.generate_sources.generators.java_like.kotlin.KotlinContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.kotlin.GradleBuildFileContentKotlin;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.kotlin.GradleBuildFileContentKotlinDefault;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.kotlin.GradleSettingsFileContentKotlin;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.kotlin.GradleSettingsFileContentKotlinDefault;
import org.gradle.api.Action;

public class GradleKtsProject
    extends AbstractGradleProject<
    KotlinContent,
    GradleBuildFileContentKotlin,
    GradleSettingsFileContentKotlin,
    GradleKtsChildProject
    > {

    public GradleKtsProject(File projectDir) {
        super(projectDir);
    }

    @Override
    protected final String getBuildFileName() {
        return "build.gradle.kts";
    }

    @Override
    protected final GradleBuildFileContentKotlin createBuildFileContent() {
        return new GradleBuildFileContentKotlinDefault();
    }

    @Override
    protected final String getSettingsFileName() {
        return "settings.gradle.kts";
    }

    @Override
    protected final GradleSettingsFileContentKotlin createSettingsFileContent() {
        return new GradleSettingsFileContentKotlinDefault();
    }

    @Override
    protected final GradleKtsChildProject createChildProject(File childProjectDir) {
        return new GradleKtsChildProject(childProjectDir);
    }

    @Override
    protected final void injectJacocoDumperImpl() {
        settingsFile.addImport(ManagementFactory.class);
        settingsFile.addImport(ObjectName.class);

        settingsFile.line();
        settingsFile.line("/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */");
        settingsFile.line("// Jacoco dumper logic:");
        settingsFile.line();

        Action<KotlinContent> mainAction = block -> {
            block.line("val mbeanServer = ManagementFactory.getPlatformMBeanServer()");
            block.line("val jacocoObjectName = ObjectName.getInstance(\"org.jacoco:type=Runtime\")");
            block.block("if (mbeanServer.isRegistered(jacocoObjectName))", ifBlock -> {
                //ifBlock.line("println '!!! dumping jacoco data !!!'");
                ifBlock.line("mbeanServer.invoke(");
                ifBlock.indent(inner -> {
                    inner.line("jacocoObjectName,");
                    inner.line("\"dump\",");
                    inner.line("arrayOf(true),");
                    inner.line("arrayOf(\"boolean\")");
                });
                ifBlock.line(")");
                //ifBlock.line("println '!!! dumped jacoco data !!!'");
            });
        };

        if (isCurrentGradleVersionGreaterThanOrEqualTo("6.1")) {
            settingsFile.addImport("org.gradle.api.services.BuildService");
            settingsFile.addImport("org.gradle.api.services.BuildServiceParameters");

            settingsFile.block(
                "abstract class JacocoDumper : BuildService<BuildServiceParameters.None>, AutoCloseable",
                classBlock -> {
                    classBlock.block("override fun close()", mainAction);
                }
            );

            settingsFile.line("gradle.sharedServices.registerIfAbsent(");
            settingsFile.indent(inner -> {
                inner.line("\"%s\",", settingsFile.escapeString(getClass().getName() + ":jacocoDumper"));
                inner.line("JacocoDumper::class,");
                inner.line("{ }");
            });
            settingsFile.line(").get()");

        } else {
            settingsFile.block("gradle.buildFinished", mainAction);
        }
    }

}
