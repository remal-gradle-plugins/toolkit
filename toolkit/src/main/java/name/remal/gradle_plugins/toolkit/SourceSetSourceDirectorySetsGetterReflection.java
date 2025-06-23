package name.remal.gradle_plugins.toolkit;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toUnmodifiableList;
import static name.remal.gradle_plugins.toolkit.SneakyThrowUtils.sneakyThrowsFunction;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isGetterOf;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSet;

@AutoService(SourceSetSourceDirectorySetsGetter.class)
class SourceSetSourceDirectorySetsGetterReflection implements SourceSetSourceDirectorySetsGetter {

    private static final List<Method> GET_SOURCE_DIRECTORY_SET_METHODS = stream(SourceSet.class.getMethods())
        .filter(ReflectionUtils::isNotStatic)
        .filter(method -> isGetterOf(method, SourceDirectorySet.class))
        .sorted(comparing(Method::getName))
        .collect(toUnmodifiableList());

    @Override
    public Collection<SourceDirectorySet> get(SourceSet sourceSet) {
        return GET_SOURCE_DIRECTORY_SET_METHODS.stream()
            .map(sneakyThrowsFunction(method -> method.invoke(sourceSet)))
            .filter(SourceDirectorySet.class::isInstance)
            .map(SourceDirectorySet.class::cast)
            .collect(toUnmodifiableList());
    }

}
