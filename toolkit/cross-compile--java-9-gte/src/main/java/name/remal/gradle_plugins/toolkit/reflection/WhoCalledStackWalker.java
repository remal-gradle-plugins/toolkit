package name.remal.gradle_plugins.toolkit.reflection;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import com.google.auto.service.AutoService;
import java.lang.StackWalker.StackFrame;
import java.util.List;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;

@AutoService(WhoCalled.class)
@SuppressWarnings("unused")
final class WhoCalledStackWalker implements WhoCalled {

    private static final long OFFSET = 1;

    @Override
    @Unmodifiable
    public List<Class<?>> getCallingClasses(int depth) {
        val classes = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(stream -> stream
            .skip(OFFSET + depth)
            .map(StackFrame::getDeclaringClass)
            .collect(toList())
        );
        return unmodifiableList(classes);
    }

    @Override
    public Class<?> getCallingClass(int depth) {
        val classes = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(stream -> stream
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

    @Override
    public boolean isCalledBy(int depth, Class<?> type) {
        return StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(stream -> stream
            .skip(OFFSET + depth)
            .anyMatch(frame -> frame.getDeclaringClass() == type)
        );
    }

}
