package name.remal.gradleplugins.toolkit.classpath;

import org.intellij.lang.annotations.Language;

@FunctionalInterface
public interface ResourceProcessor {

    void process(
        @Language("file-reference") String resourceName,
        ResourceInputStreamOpener inputStreamOpener
    ) throws Throwable;

}
