package name.remal.gradle_plugins.toolkit;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.tryLoadClass;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.file.FileTreeElement;
import org.jetbrains.annotations.VisibleForTesting;

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

    private static final Set<String> ARCHIVE_FILE_TREE_SIMPLE_CLASS_NAMES = unmodifiableSet(new LinkedHashSet<>(asList(
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
        val detailsClass = unwrapGeneratedSubclass(details.getClass());
        val enclosingClass = detailsClass.getEnclosingClass();
        if (enclosingClass == null) {
            return false;
        }

        val isArchive = ABSTRACT_ARCHIVE_FILE_TREE_CLASSES.contains(enclosingClass)
            || ARCHIVE_FILE_TREE_SIMPLE_CLASS_NAMES.contains(enclosingClass.getSimpleName());
        return isArchive;
    }

    public static boolean isNotArchiveEntry(FileTreeElement details) {
        return !isArchiveEntry(details);
    }

}
