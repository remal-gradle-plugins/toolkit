package name.remal.gradleplugins.toolkit.classpath;

import static java.util.Collections.emptyList;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import javax.annotation.Nullable;

final class ClasspathFileNotExist extends ClasspathFileBase {

    ClasspathFileNotExist(File file, int jvmMajorCompatibilityVersion) {
        super(file, jvmMajorCompatibilityVersion);
    }

    @Override
    protected Collection<String> getResourceNamesImpl() {
        return emptyList();
    }

    @Nullable
    @Override
    protected InputStream openStreamImpl(String resourceName) {
        return null;
    }

}
