package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.tryLoadClass;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.RelativePath;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class FileTreeElementUtils {

    @ReliesOnInternalGradleApi
    @VisibleForTesting
    static final Set<Class<?>> ABSTRACT_ARCHIVE_FILE_TREE_CLASSES = Stream.of(
            tryLoadClass("org.gradle.api.internal.file.archive.AbstractArchiveFileTree"),
            tryLoadClass("org.gradle.api.internal.file.collections.FileSystemMirroringFileTree")
        )
        .filter(Objects::nonNull)
        .collect(toCollection(LinkedHashSet::new));

    private static final Set<String> ARCHIVE_FILE_TREE_SIMPLE_CLASS_NAMES = unmodifiableSet(new LinkedHashSet<>(List.of(
        // supported by Gradle natively:
        "TarFileTree",
        "ZipFileTree",

        // supported by https://github.com/freefair/gradle-plugins/:
        "ArFileTree",
        "ArchiveFileTree",
        "ArjFileTree",
        "DumpFileTree",
        "SevenZipFileTree"
    )));

    public static boolean isArchiveEntry(FileTreeElement details) {
        var detailsClass = unwrapGeneratedSubclass(details.getClass());
        var enclosingClass = detailsClass.getEnclosingClass();
        if (enclosingClass == null) {
            return false;
        }

        var isArchive = ABSTRACT_ARCHIVE_FILE_TREE_CLASSES.contains(enclosingClass)
            || ARCHIVE_FILE_TREE_SIMPLE_CLASS_NAMES.contains(enclosingClass.getSimpleName());
        return isArchive;
    }

    public static boolean isNotArchiveEntry(FileTreeElement details) {
        return !isArchiveEntry(details);
    }


    public static FileTreeElement createFileTreeElement(@Nullable File file, @Nullable RelativePath relativePath) {
        return new FileTreeElementDefault(file, relativePath);
    }

}
