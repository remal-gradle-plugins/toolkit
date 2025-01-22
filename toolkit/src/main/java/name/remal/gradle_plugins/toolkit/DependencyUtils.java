package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.HasAttributes;
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class DependencyUtils {

    @Contract(value = "null->false", pure = true)
    public static boolean isPlatformDependency(@Nullable Dependency dependency) {
        if (dependency == null) {
            return false;
        }

        if (isEnforcedPlatformDependency(dependency)) {
            return true;
        }

        if (!(dependency instanceof HasAttributes)) {
            return false;
        }

        var attributes = ((HasAttributes) dependency).getAttributes();
        return hasCategory(attributes, "platform");
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isEnforcedPlatformDependency(@Nullable Dependency dependency) {
        if (dependency == null) {
            return false;
        }

        if (!(dependency instanceof HasAttributes)) {
            return false;
        }

        var attributes = ((HasAttributes) dependency).getAttributes();
        return hasCategory(attributes, "enforced-platform");
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isDocumentationDependency(@Nullable Dependency dependency) {
        if (dependency == null) {
            return false;
        }

        if (!(dependency instanceof HasAttributes)) {
            return false;
        }

        var attributes = ((HasAttributes) dependency).getAttributes();
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


    @Contract("null->false")
    @ReliesOnInternalGradleApi
    public static boolean isEmbeddedGradleDependency(@Nullable Dependency dependency) {
        return Optional.ofNullable(dependency)
            .filter(SelfResolvingDependencyInternal.class::isInstance)
            .map(SelfResolvingDependencyInternal.class::cast)
            .map(SelfResolvingDependencyInternal::getTargetComponentId)
            .filter(ComponentIdentifierUtils::isEmbeddedGradleComponentIdentifier)
            .isPresent();
    }

    @Contract("null->false")
    @ReliesOnInternalGradleApi
    public static boolean isEmbeddedGradleApiDependency(@Nullable Dependency dependency) {
        return Optional.ofNullable(dependency)
            .filter(SelfResolvingDependencyInternal.class::isInstance)
            .map(SelfResolvingDependencyInternal.class::cast)
            .map(SelfResolvingDependencyInternal::getTargetComponentId)
            .filter(ComponentIdentifierUtils::isEmbeddedGradleApiComponentIdentifier)
            .isPresent();
    }

    @Contract("null->false")
    @ReliesOnInternalGradleApi
    public static boolean isEmbeddedGradleTestKitDependency(@Nullable Dependency dependency) {
        return Optional.ofNullable(dependency)
            .filter(SelfResolvingDependencyInternal.class::isInstance)
            .map(SelfResolvingDependencyInternal.class::cast)
            .map(SelfResolvingDependencyInternal::getTargetComponentId)
            .filter(ComponentIdentifierUtils::isEmbeddedGradleTestKitComponentIdentifier)
            .isPresent();
    }

    @Contract("null->false")
    @ReliesOnInternalGradleApi
    public static boolean isEmbeddedLocalGroovyDependency(@Nullable Dependency dependency) {
        return Optional.ofNullable(dependency)
            .filter(SelfResolvingDependencyInternal.class::isInstance)
            .map(SelfResolvingDependencyInternal.class::cast)
            .map(SelfResolvingDependencyInternal::getTargetComponentId)
            .filter(ComponentIdentifierUtils::isEmbeddedLocalGroovyComponentIdentifier)
            .isPresent();
    }


    @Contract("null->false")
    public static boolean isExternalGradleDependency(@Nullable Dependency dependency) {
        return isExternalGradleApiDependency(dependency)
            || isExternalGradleTestKitDependency(dependency)
            || isExternalLocalGroovyDependency(dependency);
    }

    @Contract("null->false")
    public static boolean isExternalGradleApiDependency(@Nullable Dependency dependency) {
        if (dependency == null) {
            return false;
        }

        if (Objects.equals(dependency.getGroup(), "name.remal.gradle-api")
            && Objects.equals(dependency.getName(), "gradle-api")
        ) {
            return true;
        }

        return false;
    }

    @Contract("null->false")
    public static boolean isExternalGradleTestKitDependency(@Nullable Dependency dependency) {
        if (dependency == null) {
            return false;
        }

        if (Objects.equals(dependency.getGroup(), "name.remal.gradle-api")
            && Objects.equals(dependency.getName(), "gradle-test-kit")
        ) {
            return true;
        }

        return false;
    }

    @Contract("null->false")
    public static boolean isExternalLocalGroovyDependency(@Nullable Dependency dependency) {
        if (dependency == null) {
            return false;
        }

        if (Objects.equals(dependency.getGroup(), "name.remal.gradle-api")
            && Objects.equals(dependency.getName(), "local-groovy")
        ) {
            return true;
        }

        return false;
    }


    @Contract("null->false")
    public static boolean isGradleDependency(@Nullable Dependency dependency) {
        return isEmbeddedGradleDependency(dependency)
            || isExternalGradleDependency(dependency);
    }

    @Contract("null->false")
    public static boolean isGradleApiDependency(@Nullable Dependency dependency) {
        return isEmbeddedGradleApiDependency(dependency)
            || isExternalGradleApiDependency(dependency);
    }

    @Contract("null->false")
    public static boolean isGradleTestKitDependency(@Nullable Dependency dependency) {
        return isEmbeddedGradleTestKitDependency(dependency)
            || isExternalGradleTestKitDependency(dependency);
    }

    @Contract("null->false")
    public static boolean isLocalGroovyDependency(@Nullable Dependency dependency) {
        return isEmbeddedLocalGroovyDependency(dependency)
            || isExternalLocalGroovyDependency(dependency);
    }

}
