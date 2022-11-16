package name.remal.gradleplugins.toolkit.classpath;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toCollection;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;

@SuppressWarnings("java:S2160")
final class ClasspathFileDir extends ClasspathFileBase {

    ClasspathFileDir(File file, int jvmMajorCompatibilityVersion) {
        super(file, jvmMajorCompatibilityVersion);
    }

    @Override
    public boolean hasResource(String resourceName) {
        return new File(file, resourceName).isFile();
    }

    @Override
    @SneakyThrows
    protected Set<String> getResourceNamesImpl() {
        val filePath = file.toPath();
        try (val walker = walk(filePath)) {
            return walker
                .filter(path -> !path.equals(filePath))
                .filter(Files::isRegularFile)
                .map(filePath::relativize)
                .map(Path::toString)
                .map(ClasspathFileBase::normalizePathSeparator)
                .collect(toCollection(LinkedHashSet::new));
        }
    }

    @Override
    @Nullable
    @SneakyThrows
    protected InputStream openStreamImpl(String resourceName) {
        val resourceFile = new File(file, resourceName);
        if (resourceFile.isFile()) {
            return newInputStream(resourceFile.toPath());
        }
        return null;
    }

}
