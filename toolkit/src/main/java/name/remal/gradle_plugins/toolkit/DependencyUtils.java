package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.util.Objects;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.HasAttributes;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class DependencyUtils {

    @Contract(value = "null->false", pure = true)
    public static boolean isPlatformDependency(@Nullable Dependency dependency) {
        if (isEnforcedPlatformDependency(dependency)) {
            return true;
        }

        if (!(dependency instanceof HasAttributes)) {
            return false;
        }

        val attributes = ((HasAttributes) dependency).getAttributes();
        return hasCategory(attributes, "platform");
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isEnforcedPlatformDependency(@Nullable Dependency dependency) {
        if (!(dependency instanceof HasAttributes)) {
            return false;
        }

        val attributes = ((HasAttributes) dependency).getAttributes();
        return hasCategory(attributes, "enforced-platform");
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isDocumentationDependency(@Nullable Dependency dependency) {
        if (!(dependency instanceof HasAttributes)) {
            return false;
        }

        val attributes = ((HasAttributes) dependency).getAttributes();
        return hasCategory(attributes, "documentation");
    }


    private static boolean hasCategory(AttributeContainer attributes, String category) {
        return attributes.keySet().stream()
            .filter(attribute -> attribute.getName().equals("org.gradle.category")
                || attribute.getName().equals("org.gradle.component.category")
            )
            .map(attributes::getAttribute)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .anyMatch(category::equals);
    }

}
