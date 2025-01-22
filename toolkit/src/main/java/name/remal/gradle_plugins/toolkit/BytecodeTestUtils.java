package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.io.PrintWriter;
import lombok.NoArgsConstructor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * {@link CheckClassAdapter} is an optional dependency, so it's usage should be in a separate class.
 */
@NoArgsConstructor(access = PRIVATE)
abstract class BytecodeTestUtils {

    private static final boolean TRACE = false;

    @SuppressWarnings({"java:S106", "DefaultCharset"})
    public static ClassVisitor wrapWithTestClassVisitors(ClassVisitor classVisitor) {
        if (TRACE) {
            var writer = new PrintWriter(System.out, true);
            classVisitor = new TraceClassVisitor(classVisitor, writer);
        }

        classVisitor = new CheckClassAdapter(classVisitor);

        return classVisitor;
    }

}
