package name.remal.gradleplugins.toolkit;

import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.findMethod;
import static name.remal.gradleplugins.toolkit.reflection.MethodsInvoker.invokeMethod;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.isGetterOf;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.tryLoadClass;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradleplugins.toolkit.reflection.TypedMethod0;
import org.gradle.api.JavaVersion;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.scala.tasks.AbstractScalaCompile;

@NoArgsConstructor(access = PRIVATE)
public abstract class AbstractCompileUtils {

    @Nullable
    private static final TypedMethod0<AbstractCompile, DirectoryProperty> getDestinationDirectoryMethod =
        findMethod(AbstractCompile.class, DirectoryProperty.class, "getDestinationDirectory");

    @Nullable
    private static final TypedMethod0<AbstractCompile, File> compileGetDestinationDirMethod =
        findMethod(AbstractCompile.class, File.class, "getDestinationDir");

    @Nullable
    public static File getDestinationDir(AbstractCompile task) {
        if (getDestinationDirectoryMethod != null) {
            return requireNonNull(getDestinationDirectoryMethod.invoke(task))
                .getAsFile()
                .getOrNull();

        } else if (compileGetDestinationDirMethod != null) {
            return compileGetDestinationDirMethod.invoke(task);

        } else {
            throw new IllegalStateException(
                "Both 'getDestinationDirectory' and 'getDestinationDir' methods can't be found for task: " + task
            );
        }
    }


    @Nullable
    public static CompileOptions getCompileOptionsOf(AbstractCompile task) {
        if (task instanceof JavaCompile) {
            return ((JavaCompile) task).getOptions();
        } else if (task instanceof GroovyCompile) {
            return ((GroovyCompile) task).getOptions();
        } else if (task instanceof AbstractScalaCompile) {
            return ((AbstractScalaCompile) task).getOptions();
        }

        @SuppressWarnings("unchecked")
        val getter = findMethod((Class<AbstractCompile>) task.getClass(), CompileOptions.class, "getOptions");
        if (getter != null) {
            return getter.invoke(task);
        }

        return null;
    }


    public static JavaVersion getCompilerJavaVersionOf(AbstractCompile task) {
        val javaVersion = getCompilerJavaVersionOrNullOf(task);
        return javaVersion != null ? javaVersion : JavaVersion.current();
    }

    @Nullable
    private static final Class<?> JAVA_COMPILER_CLASS = tryLoadClass(
        "org.gradle.jvm.toolchain.JavaCompiler",
        AbstractCompileUtils.class.getClassLoader()
    );

    @Nullable
    private static final Class<?> JAVA_LAUNCHER_CLASS = tryLoadClass(
        "org.gradle.jvm.toolchain.JavaLauncher",
        AbstractCompileUtils.class.getClassLoader()
    );

    @Nullable
    @VisibleForTesting
    @SneakyThrows
    @SuppressWarnings({"UnstableApiUsage", "rawtypes", "java:S3776", "unchecked"})
    static JavaVersion getCompilerJavaVersionOrNullOf(AbstractCompile task) {
        val taskClass = (Class<AbstractCompile>) unwrapGeneratedSubclass(task.getClass());

        if (task instanceof JavaCompile) {
            val getJavaCompilerMethod = findMethod(taskClass, Property.class, "getJavaCompiler");
            if (getJavaCompilerMethod != null) {
                return getJavaVersionFromJavaLauncherProperty(getJavaCompilerMethod.invoke(task));
            } else {
                return JavaVersion.current();
            }

        } else if (task instanceof GroovyCompile
            || task instanceof AbstractScalaCompile
        ) {
            val getJavaLauncherMethod = findMethod(taskClass, Property.class, "getJavaLauncher");
            if (getJavaLauncherMethod != null) {
                return getJavaVersionFromJavaLauncherProperty(getJavaLauncherMethod.invoke(task));
            } else {
                return JavaVersion.current();
            }
        }

        if (JAVA_COMPILER_CLASS == null && JAVA_LAUNCHER_CLASS == null) {
            return JavaVersion.current();
        }

        for (val method : taskClass.getMethods()) {
            if (isGetterOf(method, Property.class)) {
                val propertyGenericType = TypeToken.of(task.getClass())
                    .method(method)
                    .getReturnType()
                    .getSupertype((Class) Property.class)
                    .getType();
                final Class<?> propertyType;
                if (propertyGenericType instanceof ParameterizedType) {
                    val parameterizedType = (ParameterizedType) propertyGenericType;
                    val propertyValueGenericType = parameterizedType.getActualTypeArguments()[0];
                    propertyType = TypeToken.of(propertyValueGenericType).getRawType();
                } else {
                    continue;
                }

                if ((JAVA_COMPILER_CLASS != null && JAVA_COMPILER_CLASS.isAssignableFrom(propertyType))
                    || (JAVA_LAUNCHER_CLASS != null && JAVA_LAUNCHER_CLASS.isAssignableFrom(propertyType))
                ) {
                    return getJavaVersionFromJavaLauncherProperty((Property) method.invoke(task));
                }
            }
        }

        return null;
    }

    @Nullable
    @SuppressWarnings("rawtypes")
    private static JavaVersion getJavaVersionFromJavaLauncherProperty(@Nullable Property property) {
        return Optional.ofNullable(property)
            .map(Property::getOrNull)
            .map(javaLauncher -> invokeMethod(javaLauncher, Object.class, "getMetadata"))
            .map(metadata -> invokeMethod(metadata, Object.class, "getLanguageVersion"))
            .map(languageVersion -> invokeMethod(languageVersion, int.class, "asInt"))
            .map(JavaVersion::toVersion)
            .orElse(null);
    }

}
