package name.remal.gradleplugins.toolkit.reflection;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;
import static java.lang.String.format;

import com.google.auto.service.AutoService;
import java.lang.StackWalker.StackFrame;
import lombok.val;

@AutoService(WhoCalled.class)
@SuppressWarnings("unused")
final class WhoCalledStackWalker implements WhoCalled {

    private static final long OFFSET = 1;

    @Override
    public Class<?> getCallingClass(int depth) {
        Class<?>[] classes = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(stream -> stream
            .skip(OFFSET)
            .limit(depth + 1L)
            .map(StackFrame::getDeclaringClass)
            .toArray(Class<?>[]::new)
        );
        val index = depth;
        if (depth >= classes.length) {
            throw new IllegalArgumentException(format(
                "Stack depth is %d, can't get element of depth %d",
                classes.length,
                depth
            ));
        }
        return classes[index];
    }

    @Override
    public boolean isCalledBy(Class<?> type) {
        return StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(stream -> stream
            .skip(OFFSET)
            .anyMatch(frame -> frame.getDeclaringClass() == type)
        );
    }

}
