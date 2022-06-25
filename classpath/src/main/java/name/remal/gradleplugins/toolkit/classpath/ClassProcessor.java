package name.remal.gradleplugins.toolkit.classpath;

import java.io.File;

@FunctionalInterface
public interface ClassProcessor {

    void process(
        File classpathFile,
        String className,
        ResourceInputStreamOpener inputStreamOpener
    ) throws Throwable;

}
