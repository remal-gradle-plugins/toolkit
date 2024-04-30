package name.remal.gradle_plugins.toolkit.testkit;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.ObjectUtils;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleDependencyVersions {

    public static String getCorrespondingKotlinVersion() {
        val propertyName = "corresponding-kotlin.version";

        return getNotEmptySystemProperty(propertyName)
            .orElseThrow(() -> new AssertionError(format(
                "%s system property is not set or empty",
                propertyName
            )));
    }

    public static String getJUnitVersion() {
        val propertyName = "junit.version";

        return getNotEmptySystemProperty(propertyName)
            .orElseThrow(() -> new AssertionError(format(
                "%s system property is not set or empty",
                propertyName
            )));
    }

    public static String getExternalPluginToTestVersion(String pluginId) {
        val propertyName = "external-plugin-version-" + pluginId;

        return getNotEmptySystemProperty(propertyName)
            .orElseThrow(() -> new AssertionError(format(
                "%s system property is not set or empty."
                    + " Make sure that `%s` plugin is added as a dependency to `externalPluginsToTest` configuration.",
                propertyName,
                pluginId
            )));
    }


    private static Optional<String> getNotEmptySystemProperty(String name) {
        return Optional.ofNullable(System.getProperty(name))
            .filter(ObjectUtils::isNotEmpty);
    }

}
