package name.remal.gradle_plugins.toolkit.classpath;

import static java.util.Collections.emptySet;

import java.io.File;
import java.io.InputStream;
import java.util.Set;
import javax.annotation.Nullable;

final class ClasspathFileNotExist extends ClasspathFileBase {

    ClasspathFileNotExist(File file, int jvmMajorCompatibilityVersion) {
        super(file, jvmMajorCompatibilityVersion);
    }

    @Override
    public boolean hasResource(String resourceName) {
        return false;
    }

    @Override
    protected Set<String> getResourceNamesImpl() {
        return emptySet();
    }

    @Nullable
    @Override
    protected InputStream openStreamImpl(String resourceName) {
        return null;
    }

}
