package name.remal.gradleplugins.toolkit.classpath;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;

final class ClasspathFileDir extends ClasspathFileBase {

    ClasspathFileDir(File file, int jvmMajorCompatibilityVersion) {
        super(file, jvmMajorCompatibilityVersion);
    }

    @Override
    @SneakyThrows
    protected Collection<String> getResourceNamesImpl() {
        val filePath = file.toPath();
        try (val walker = walk(filePath)) {
            return walker.filter(Files::isRegularFile)
                .filter(path -> !path.equals(filePath))
                .map(filePath::relativize)
                .map(Path::toString)
                .map(ClasspathFileBase::normalizePath)
                .collect(toList());
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
