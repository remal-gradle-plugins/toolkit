package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.findExtension;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradleplugins.toolkit.ServiceRegistryUtils.getService;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.isGetterOf;

import java.util.function.BiFunction;
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

    @SuppressWarnings("ConstantConditions")
    public static <T> Provider<T> getJavaToolchainToolProviderFor(
        Project project,
        BiFunction<JavaToolchainService, Action<? super JavaToolchainSpec>, Provider<T>> getter
    ) {
        val javaToolchainService = getJavaToolchainServiceFor(project);
        val currentJvmProvider = getter.apply(javaToolchainService, spec ->
            spec.getLanguageVersion().set(JavaLanguageVersion.of(JavaVersion.current().getMajorVersion()))
        );

        return project.provider(() -> {
            val extension = findExtension(project, JavaPluginExtension.class);
            if (extension != null) {
                val toolchain = extension.getToolchain();
                if (toolchain != null) {
                    val provider = getter.apply(javaToolchainService, spec ->
                        copyJavaToolchainSpec(toolchain, spec)
                    );
                    return provider.orElse(currentJvmProvider).get();
                }
            }

            return currentJvmProvider.get();
        });
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static void copyJavaToolchainSpec(JavaToolchainSpec from, JavaToolchainSpec to) {
        for (val method : JavaToolchainSpec.class.getMethods()) {
            if (isGetterOf(method, Property.class)) {
                val propertyFrom = (Property<Object>) method.invoke(from);
                val propertyTo = (Property<Object>) method.invoke(to);
                if (propertyFrom != null && propertyTo != null) {
                    propertyTo.set(propertyFrom);
                }
            }
        }
    }

}
