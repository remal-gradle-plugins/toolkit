package name.remal.gradle_plugins.toolkit.reflection;

import java.util.List;
import org.jetbrains.annotations.Unmodifiable;

interface WhoCalled {

    @Unmodifiable
    List<Class<?>> getCallingClasses(int depth);

    Class<?> getCallingClass(int depth);

    boolean isCalledBy(int depth, Class<?> type);

}
