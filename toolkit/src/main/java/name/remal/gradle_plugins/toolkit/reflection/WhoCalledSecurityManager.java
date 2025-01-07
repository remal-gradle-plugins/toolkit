package name.remal.gradle_plugins.toolkit.reflection;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;

@AutoService(WhoCalled.class)
@SuppressWarnings({"unused", "removal", "java:S5738", "RedundantSuppression"})
final class WhoCalledSecurityManager extends SecurityManager implements WhoCalled {

    private static final int OFFSET = 1;

    @Override
    @Unmodifiable
    public List<Class<?>> getCallingClasses(int depth) {
        val result = new ArrayList<Class<?>>();
        val classes = getClassContext();
        for (int i = OFFSET + depth; i < classes.length; i++) {
            result.add(classes[i]);
        }
        return unmodifiableList(result);
    }

    @Override
    public Class<?> getCallingClass(int depth) {
        val classes = getClassContext();
        val index = OFFSET + depth;
        if (index >= classes.length) {
            throw new IllegalArgumentException(format(
                "Stack depth is %d, can't get element of depth %d",
                classes.length - OFFSET,
                depth
            ));
        }
        return classes[index];
    }

    @Override
    public boolean isCalledBy(Class<?> type) {
        Class<?>[] classes = getClassContext();
        for (int i = OFFSET + 1; i < classes.length; i++) {
            if (classes[i] == type) {
                return true;
            }
        }

        return false;
    }

}
