package name.remal.gradle_plugins.toolkit.reflection;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;

import java.lang.StackWalker.StackFrame;
import java.util.List;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Unmodifiable;

@NoArgsConstructor(access = PRIVATE)
public abstract class WhoCalledUtils {

    private static final long OFFSET = 0;

    @Unmodifiable
    public static List<Class<?>> getCallingClasses(int depth) {
        return StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(stream -> stream
            .skip(OFFSET + depth)
            .map(StackFrame::getDeclaringClass)
            .collect(toUnmodifiableList())
        );
    }

    public static Class<?> getCallingClass(int depth) {
        var classes = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(stream -> stream
            .skip(OFFSET + depth)
            .limit(1)
            .map(StackFrame::getDeclaringClass)
            .toArray(Class<?>[]::new)
        );
        if (classes.length == 0) {
            throw new IllegalArgumentException(format(
                "Stack depth is %d, can't get element of depth %d",
                classes.length,
                depth
            ));
        }
        return classes[0];
    }

    public static boolean isCalledBy(Class<?> type) {
        return StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(stream -> stream
            .skip(OFFSET + 1)
            .anyMatch(frame -> frame.getDeclaringClass() == type)
        );
    }

}
