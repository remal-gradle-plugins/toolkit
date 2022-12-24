package name.remal.gradle_plugins.toolkit.classpath;

import java.io.File;

@FunctionalInterface
public interface ClassProcessor {

    void process(
        File classpathFile,
        String className,
        ResourceInputStreamOpener inputStreamOpener
    ) throws Throwable;

}
