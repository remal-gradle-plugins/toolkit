package name.remal.gradle_plugins.toolkit.testkit;

import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.ObjectUtils;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleDependencyVersions {

    private static final String CORRESPONDING_KOTLIN_VERSION_PROPERTY = "corresponding-kotlin.version";

    public static String getCorrespondingKotlinVersion() {
        return Optional.ofNullable(System.getProperty(CORRESPONDING_KOTLIN_VERSION_PROPERTY))
            .filter(ObjectUtils::isNotEmpty)
            .orElseThrow(() -> new AssertionError(
                CORRESPONDING_KOTLIN_VERSION_PROPERTY + " system property is not set or empty"
            ));
    }

}
