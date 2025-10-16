package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.join;
import static name.remal.gradle_plugins.toolkit.StringUtils.normalizeString;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import name.remal.gradle_plugins.toolkit.testkit.MinTestableGradleVersion;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GradleKtsProjectTest extends GradleProjectTestBase<GradleKtsProject> {

    GradleKtsProjectTest(GradleKtsProject project) {
        super(project);
    }


    @Test
    @MinTestableGradleVersion("8.2")
    void kotlinDslWarningsAsErrorsByDefault() {
        project.withoutConfigurationCache();

        project.forBuildFile(build -> {
            build.line("@Deprecated(\"\")");
            build.block("fun deprecatedFun()", block -> {
                block.line("println(\"Deprecated fun!\")");
            });
            build.line("deprecatedFun()");
        });

        var buildResult = project.assertBuildFails("help");

        assertThat(normalizeString(buildResult.getOutput()))
            .contains("deprecatedFun(): Unit' is deprecated");
    }

    @Test
    void kotlinDslWarningsAsWarnings() {
        project.withoutConfigurationCache();

        project.putGradleProperty("org.gradle.kotlin.dsl.allWarningsAsErrors", false);

        project.forBuildFile(build -> {
            build.line("@Deprecated(\"\")");
            build.block("fun deprecatedFun()", block -> {
                block.line("println(\"Deprecated fun!\")");
            });
            build.line("deprecatedFun()");
        });

        project.assertBuildSuccessfully("help");
    }

    @Nested
    @MinTestableGradleVersion("8.1")
    class ConfigurationCache {

        @Test
        void defaultSettings() {
            prepareSimpleConfigurationCacheFailingScenario();

            var buildResult = project.assertBuildFails("help");

            assertThat(normalizeString(buildResult.getOutput()))
                .contains("registration of listener on 'Gradle.addBuildListener' is unsupported");
        }

        @Test
        void disabled() {
            project.withoutConfigurationCache();

            prepareSimpleConfigurationCacheFailingScenario();

            project.assertBuildSuccessfully("help");
        }

        private void prepareSimpleConfigurationCacheFailingScenario() {
            project.forBuildFile(build -> {
                build.block("val listener = object : BuildAdapter()", listener -> {
                    listener.block("override fun projectsEvaluated(gradle: Gradle)", fun -> {
                        fun.line("println(\"projectsEvaluated\")");
                    });
                });
                build.line("gradle.addBuildListener(listener)");
            });
        }

        @Test
        @MinTestableGradleVersion("8.14")
        void integrityCheck() {
            project.forBuildFile(build -> {
                build.addImport(Serializable.class);
                build.addImport(ObjectOutputStream.class);
                build.addImport(ObjectInputStream.class);

                build.block(
                    join("\n",
                        "data class User @JvmOverloads constructor(",
                        "    @Transient var name: String? = null,",
                        "    @Transient var age: Int = 0",
                        ") : Serializable"
                    ),
                    clazz -> {
                        clazz.block("private fun writeObject(out: ObjectOutputStream)", fun -> {
                            fun.line("out.defaultWriteObject()");
                            fun.line("out.writeObject(name)");
                            fun.line("out.writeInt(age)");
                        });
                        clazz.block("private fun readObject(input: ObjectInputStream)", fun -> {
                            fun.line("input.defaultReadObject()");
                            fun.line("name = input.readObject() as String?");
                            fun.line("// age is not deserialized");
                        });
                    });

                build.block("abstract class FailingTask : DefaultTask()", clazz -> {
                    clazz.line("@get:Input");
                    clazz.line("var user: User = User(\"John\", 23)");

                    clazz.line("@TaskAction");
                    clazz.block("fun action()", fun -> {
                        fun.line("logger.lifecycle(\"User: {}\", user)");
                    });
                });

                build.line("tasks.register<FailingTask>(\"failing\")");
            });

            var buildResult = project.assertBuildFails("failing");

            assertThat(normalizeString(buildResult.getOutput()))
                .contains("The value cannot be decoded properly with 'JavaObjectSerializationCodec'");
        }

        @Test
        @MinTestableGradleVersion("8.8")
        void isolatedProjects() {
            project.newChildProject("child").forBuildFile(build -> {
                build.applyPlugin("java");
            });

            project.forBuildFile(build -> {
                build.line("evaluationDependsOn(\":child\")");

                build.line("project(\":child\").configurations.forEach { println(it) }");
            });

            var buildResult = project.assertBuildFails("help");

            assertThat(normalizeString(buildResult.getOutput()))
                .contains("cannot access 'Project.configurations' functionality on another project");
        }

    }

}
