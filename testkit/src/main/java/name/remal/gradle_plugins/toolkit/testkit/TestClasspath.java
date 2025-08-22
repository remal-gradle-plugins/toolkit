package name.remal.gradle_plugins.toolkit.testkit;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.defaultValue;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.PropertiesUtils.loadProperties;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class TestClasspath {

    public static String getTestClasspathLibraryVersion(String groupAndModuleNotation) {
        return getTestClasspathLibraryVersion("", groupAndModuleNotation);
    }

    public static String getTestClasspathLibraryVersion(
        String scope,
        String groupAndModuleNotation
    ) {
        var version = getNotEmptyClasspathProperty(scope, format(
            "%s|version",
            groupAndModuleNotation
        ));

        return version;
    }


    public static String getTestClasspathLibraryFullNotation(String groupAndModuleNotation) {
        return getTestClasspathLibraryFullNotation("", groupAndModuleNotation);
    }

    public static String getTestClasspathLibraryFullNotation(
        String scope,
        String groupAndModuleNotation
    ) {
        var fullNotation = getNotEmptyClasspathProperty(scope, format(
            "%s|full-notation",
            groupAndModuleNotation
        ));

        return fullNotation;
    }


    public static Set<Path> getTestClasspathLibraryFilePaths(String groupAndModuleNotation) {
        return getTestClasspathLibraryFilePaths("", groupAndModuleNotation);
    }

    public static Set<Path> getTestClasspathLibraryFilePaths(
        String scope,
        String groupAndModuleNotation
    ) {
        var pathsString = getNotEmptyClasspathProperty(scope, format(
            "%s|paths",
            groupAndModuleNotation
        ));

        return Splitter.on("\n").splitToStream(pathsString)
            .filter(not(String::isEmpty))
            .map(Paths::get)
            .collect(toImmutableSet());
    }


    public static Set<String> getTestClasspathFirstLevelLibraryNotations() {
        return getTestClasspathFirstLevelLibraryNotations("");
    }

    public static Set<String> getTestClasspathFirstLevelLibraryNotations(String scope) {
        var properties = getClasspathProperties(scope);
        return properties.entrySet().stream()
            .filter(entry -> entry.getKey().toString().endsWith(IS_FIRST_LEVEL_NOTATION_SUFFIX))
            .filter(entry -> parseBoolean(entry.getValue().toString()))
            .map(Entry::getKey)
            .map(Object::toString)
            .map(key -> key.substring(0, key.length() - IS_FIRST_LEVEL_NOTATION_SUFFIX.length()))
            .collect(toImmutableSet());
    }

    private static final String IS_FIRST_LEVEL_NOTATION_SUFFIX = "|is-first-level";


    private static String getNotEmptyClasspathProperty(String scope, String propertyName) {
        var properties = getClasspathProperties(scope);
        var value = properties.getProperty(propertyName);
        if (isEmpty(value)) {
            throw new AssertionError(format(
                "Test classpath property for scope `%s` is not set or empty: `%s`",
                scope,
                propertyName
            ));
        }
        return value;
    }

    private static Properties getClasspathProperties(String scope) {
        return SCOPE_PROPERTIES_CACHE.getUnchecked(defaultValue(scope));
    }

    private static final LoadingCache<String, Properties> SCOPE_PROPERTIES_CACHE =
        CacheBuilder.newBuilder().build(CacheLoader.from(scope -> {
            var propertiesFilePathEnvName = getPropertiesFilePathEnvName(scope);
            var propertiesFilePath = System.getenv(propertiesFilePathEnvName);
            if (isEmpty(propertiesFilePath)) {
                throw new AssertionError(format(
                    "Environment variable is not set or empty: `%s`",
                    propertiesFilePathEnvName
                ));
            }

            var propertiesFile = new File(propertiesFilePath);
            return loadProperties(propertiesFile);
        }));

    private static String getPropertiesFilePathEnvName(@Nullable String scope) {
        if (scope == null || scope.isEmpty()) {
            return "NAME_REMAL_GRADLE_PLUGINS_TEST_CLASSPATH_FILE";
        }

        scope = scope.toUpperCase().replaceAll("\\W", "_");
        return "NAME_REMAL_GRADLE_PLUGINS_TEST_CLASSPATH_" + scope + "_FILE";
    }

}
