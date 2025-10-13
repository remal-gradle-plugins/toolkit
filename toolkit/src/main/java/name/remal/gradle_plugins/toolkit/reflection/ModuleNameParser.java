package name.remal.gradle_plugins.toolkit.reflection;

import static lombok.AccessLevel.PRIVATE;
import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

@NoArgsConstructor(access = PRIVATE)
public abstract class ModuleNameParser {

    private static final Attributes.Name AUTOMATIC_MODULE_NAME = new Attributes.Name("Automatic-Module-Name");

    @FunctionalInterface
    public interface InputStreamSupplier {
        @Nullable
        @MustBeClosed
        InputStream get() throws Exception;
    }

    @Nullable
    public static String parseModuleName(
        @Nullable InputStreamSupplier moduleInfoInputStreamSupplier,
        @Nullable InputStreamSupplier manifestInputStreamSupplier,
        @Nullable Path jarFilePath
    ) {
        return parseModuleName(
            moduleInfoInputStreamSupplier,
            manifestInputStreamSupplier,
            jarFilePath != null ? jarFilePath.toString() : null
        );
    }

    @Nullable
    public static String parseModuleName(
        @Nullable InputStreamSupplier moduleInfoInputStreamSupplier,
        @Nullable InputStreamSupplier manifestInputStreamSupplier,
        @Nullable File jarFile
    ) {
        return parseModuleName(
            moduleInfoInputStreamSupplier,
            manifestInputStreamSupplier,
            jarFile != null ? jarFile.getPath() : null
        );
    }

    @Nullable
    public static String parseModuleName(
        @Nullable InputStreamSupplier moduleInfoInputStreamSupplier,
        @Nullable InputStreamSupplier manifestInputStreamSupplier,
        @Nullable URI uri
    ) {
        return parseModuleName(
            moduleInfoInputStreamSupplier,
            manifestInputStreamSupplier,
            uri != null ? uri.toString() : null
        );
    }

    @Nullable
    public static String parseModuleName(
        @Nullable InputStreamSupplier moduleInfoInputStreamSupplier,
        @Nullable InputStreamSupplier manifestInputStreamSupplier,
        @Nullable URL url
    ) {
        return parseModuleName(
            moduleInfoInputStreamSupplier,
            manifestInputStreamSupplier,
            url != null ? url.toString() : null
        );
    }

    @Nullable
    @SneakyThrows
    @SuppressWarnings("java:S3776")
    public static String parseModuleName(
        @Nullable InputStreamSupplier moduleInfoInputStreamSupplier,
        @Nullable InputStreamSupplier manifestInputStreamSupplier,
        @Nullable String jarFilePath
    ) {
        if (moduleInfoInputStreamSupplier != null) {
            try (var in = moduleInfoInputStreamSupplier.get()) {
                if (in != null) {
                    var classNode = new ClassNode();
                    new ClassReader(in).accept(classNode, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);

                    var moduleName = Optional.ofNullable(classNode.module)
                        .map(it -> it.name)
                        .orElse(null);
                    if (moduleName != null) {
                        return moduleName;
                    }
                }
            }
        }

        if (manifestInputStreamSupplier != null) {
            try (var in = manifestInputStreamSupplier.get()) {
                if (in != null) {
                    var manifest = new Manifest();
                    manifest.read(in);

                    var moduleName = Optional.ofNullable(manifest.getMainAttributes())
                        .map(attrs -> attrs.getValue(AUTOMATIC_MODULE_NAME))
                        .orElse(null);
                    if (moduleName != null) {
                        return moduleName;
                    }
                }
            }
        }

        if (jarFilePath != null) {
            var moduleName = getModuleNameForPath(jarFilePath);
            if (moduleName != null) {
                return moduleName;
            }
        }

        return null;
    }

    private static final Pattern FILE_NAME_PART = Pattern.compile("^(?:[^!?#]*[/\\\\]+)?([^!?#/\\\\]+)(?:[!?#].*)?$");
    private static final Pattern VERSION_PART = Pattern.compile("-(\\d+(\\..*)?)$");

    @Nullable
    private static String getModuleNameForPath(String path) {
        var fileName = FILE_NAME_PART.matcher(path).replaceFirst("$1");
        if (!fileName.endsWith(".jar")) {
            return null;
        }
        String moduleName = fileName;
        moduleName = moduleName.substring(0, moduleName.length() - ".jar".length());
        moduleName = VERSION_PART.matcher(moduleName).replaceFirst("");
        moduleName = cleanModuleName(moduleName);
        return moduleName;
    }

    private static final Pattern NON_ALPHANUM = Pattern.compile("[^A-Za-z0-9]");
    private static final Pattern REPEATING_DOTS = Pattern.compile("\\.{2,}");

    private static String cleanModuleName(String moduleName) {
        // replace non-alphanumeric
        moduleName = NON_ALPHANUM.matcher(moduleName).replaceAll(".");

        // collapse repeating dots
        moduleName = REPEATING_DOTS.matcher(moduleName).replaceAll(".");

        // drop leading dots
        if (moduleName.startsWith(".")) {
            moduleName = moduleName.substring(1);
        }

        // drop trailing dots
        if (moduleName.endsWith(".")) {
            moduleName = moduleName.substring(0, moduleName.length() - 1);
        }

        return moduleName;
    }

}
