package name.remal.gradleplugins.toolkit.reflection;

import static java.lang.String.format;

import com.google.auto.service.AutoService;
import lombok.val;

@AutoService(WhoCalled.class)
@SuppressWarnings({"unused", "removal", "java:S5738", "RedundantSuppression"})
final class WhoCalledSecurityManager extends SecurityManager implements WhoCalled {

    private static final int OFFSET = 1;

    @Override
    public Class<?> getCallingClass(int depth) {
        Class<?>[] classes = getClassContext();
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
