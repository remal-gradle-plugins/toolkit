package name.remal.gradleplugins.toolkit.classpath;

@FunctionalInterface
public interface ClassProcessor {

    void process(
        String className,
        ResourceInputStreamOpener inputStreamOpener
    ) throws Throwable;

}
