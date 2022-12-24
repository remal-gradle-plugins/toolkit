package name.remal.gradle_plugins.toolkit.classpath;

import java.io.File;
import org.intellij.lang.annotations.Language;

@FunctionalInterface
public interface ResourceProcessor {

    void process(
        File classpathFile,
        @Language("file-reference") String resourceName,
        ResourceInputStreamOpener inputStreamOpener
    ) throws Throwable;

}
