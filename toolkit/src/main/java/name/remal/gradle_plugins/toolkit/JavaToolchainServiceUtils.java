package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getOptionalExtension;
import static name.remal.gradle_plugins.toolkit.ServiceRegistryUtils.getService;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isGetterOf;

import java.util.function.BiFunction;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.gradle.util.GradleVersion;

@MinGradleVersion("6.7")
@NoArgsConstructor(access = PRIVATE)
public abstract class JavaToolchainServiceUtils {

    private static final GradleVersion MIN_SERVICE_VERSION = GradleVersion.version("7.0");

    public static JavaToolchainService getJavaToolchainServiceFor(Project project) {
        try {
            return getService(project, JavaToolchainService.class);

        } catch (Exception exception) {
            if (GradleVersion.current().compareTo(MIN_SERVICE_VERSION) < 0) {
                try {
                    project.getPluginManager().apply(JavaBasePlugin.class);
                    return getExtension(project, JavaToolchainService.class);
                } catch (Exception e) {
                    exception.addSuppressed(e);
                }
            }

            throw exception;
        }
    }

    public static <T> Provider<T> getJavaToolchainToolProviderFor(
        Project project,
        BiFunction<JavaToolchainService, Action<? super JavaToolchainSpec>, Provider<T>> getter
    ) {
        return getJavaToolchainToolProviderFor(project, getter, __ -> { });
    }

    public static <T> Provider<T> getJavaToolchainToolProviderFor(
        Project project,
        BiFunction<JavaToolchainService, Action<? super JavaToolchainSpec>, Provider<T>> getter,
        Action<? super JavaToolchainSpec> configurer
    ) {
        val javaToolchainService = getJavaToolchainServiceFor(project);
        val currentJvmProvider = getter.apply(javaToolchainService, spec -> {
            spec.getLanguageVersion().convention(JavaLanguageVersion.of(JavaVersion.current().getMajorVersion()));
            configurer.execute(spec);
        });

        return project.provider(() -> {
            val toolchain = getJavaToolchainSpecOf(project);
            if (toolchain != null) {
                val provider = getter.apply(javaToolchainService, spec -> {
                    copyJavaToolchainSpec(toolchain, spec);
                    configurer.execute(spec);
                });
                return provider.orElse(currentJvmProvider).get();
            }

            return currentJvmProvider.get();
        });
    }

    @Nullable
    public static JavaToolchainSpec getJavaToolchainSpecOf(Project project) {
        return getOptionalExtension(project, JavaPluginExtension.class)
            .map(JavaPluginExtension::getToolchain)
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static void copyJavaToolchainSpec(JavaToolchainSpec from, JavaToolchainSpec to) {
        for (val method : JavaToolchainSpec.class.getMethods()) {
            if (isGetterOf(method, Property.class)) {
                val propertyFrom = (Property<Object>) method.invoke(from);
                val propertyTo = (Property<Object>) method.invoke(to);
                if (propertyFrom != null && propertyTo != null) {
                    propertyTo.convention(propertyFrom);
                }
            }
        }
    }

}
