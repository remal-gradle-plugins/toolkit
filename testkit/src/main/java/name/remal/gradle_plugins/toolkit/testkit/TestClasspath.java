package name.remal.gradle_plugins.toolkit.testkit;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyMapProxy;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.PropertiesUtils.loadProperties;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class TestClasspath {

    private static final Map<String, String> PROPERTIES = asLazyMapProxy(() -> {
        var propertiesFilePathEnvName = "NAME_REMAL_GRADLE_PLUGINS_TEST_CLASSPATH_FILE";
        var propertiesFilePath = System.getenv(propertiesFilePathEnvName);
        if (isEmpty(propertiesFilePath)) {
            throw new AssertionError(format(
                "`%s` environment variable is not set or empty",
                propertiesFilePathEnvName
            ));
        }

        var propertiesFile = new File(propertiesFilePath);
        var result = ImmutableMap.<String, String>builder();
        loadProperties(propertiesFile).forEach((key, value) -> {
            if (key != null && value != null) {
                result.put(key.toString(), value.toString());
            }
        });
        return result.buildKeepingLast();
    });

    private static String getNotEmptySystemProperty(String name) {
        var value = PROPERTIES.get(name);
        if (isEmpty(value)) {
            throw new AssertionError(format("`%s` test classpath property is not set or empty", name));
        }
        return value;
    }


    public static String getTestClasspathLibraryVersion(String groupAndModuleNotation) {
        var version = getNotEmptySystemProperty(format(
            "%s|version",
            groupAndModuleNotation
        ));

        return version;
    }

    public static String getTestClasspathLibraryFullNotation(String groupAndModuleNotation) {
        var fullNotation = getNotEmptySystemProperty(format(
            "%s|full-notation",
            groupAndModuleNotation
        ));

        return fullNotation;
    }

    public static Collection<Path> getTestClasspathLibraryFilePaths(String groupAndModuleNotation) {
        var pathsString = getNotEmptySystemProperty(format(
            "%s|paths",
            groupAndModuleNotation
        ));

        return Splitter.on("\n").splitToStream(pathsString)
            .filter(not(String::isEmpty))
            .map(Paths::get)
            .collect(toImmutableSet());
    }

}
