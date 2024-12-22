package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.findMethod;

import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.reflection.TypedMethod0;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.internal.component.local.model.OpaqueComponentIdentifier;

@NoArgsConstructor(access = PRIVATE)
public abstract class ComponentIdentifierUtils {

    @SuppressWarnings("rawtypes")
    private static final TypedMethod0<OpaqueComponentIdentifier, Enum> getClassPathNotation =
        findMethod(OpaqueComponentIdentifier.class, Enum.class, "getClassPathNotation");

    private static final TypedMethod0<OpaqueComponentIdentifier, String> getDisplayName =
        findMethod(OpaqueComponentIdentifier.class, String.class, "getDisplayName");

    public static boolean isGradleEmbeddedComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        if (componentId == null) {
            return false;
        }

        if (componentId instanceof OpaqueComponentIdentifier) {
            val opaqueComponentId = (OpaqueComponentIdentifier) componentId;
            if (getClassPathNotation != null) {
                val classPathNotation = getClassPathNotation.invoke(opaqueComponentId);
                return classPathNotation != null;

            } else if (getDisplayName != null) {
                val displayName = getDisplayName.invoke(opaqueComponentId);
                if (displayName != null) {
                    return displayName.equals("Gradle API")
                        || displayName.equals("Gradle Kotlin DSL")
                        || displayName.equals("Gradle TestKit")
                        || displayName.equals("Local Groovy");
                }

            } else {
                throw new IllegalStateException(
                    "No getClassPathNotation method found for " + opaqueComponentId.getClass()
                );
            }
        }

        return false;
    }

    public static boolean isEmbeddedGradleApiComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        if (componentId == null) {
            return false;
        }

        if (componentId instanceof OpaqueComponentIdentifier) {
            val opaqueComponentId = (OpaqueComponentIdentifier) componentId;
            if (getClassPathNotation != null) {
                val classPathNotation = getClassPathNotation.invoke(opaqueComponentId);
                return classPathNotation != null && classPathNotation.name().equals("GRADLE_API");

            } else if (getDisplayName != null) {
                val displayName = getDisplayName.invoke(opaqueComponentId);
                return displayName != null && displayName.equals("Gradle API");

            } else {
                throw new IllegalStateException(
                    "No getClassPathNotation method found for " + opaqueComponentId.getClass()
                );
            }
        }

        return false;
    }

    public static boolean isEmbeddedGradleTestKitComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        if (componentId == null) {
            return false;
        }

        if (componentId instanceof OpaqueComponentIdentifier) {
            val opaqueComponentId = (OpaqueComponentIdentifier) componentId;
            if (getClassPathNotation != null) {
                val classPathNotation = getClassPathNotation.invoke(opaqueComponentId);
                return classPathNotation != null && classPathNotation.name().equals("GRADLE_TEST_KIT");

            } else if (getDisplayName != null) {
                val displayName = getDisplayName.invoke(opaqueComponentId);
                return displayName != null && displayName.equals("Gradle TestKit");

            } else {
                throw new IllegalStateException(
                    "No getClassPathNotation method found for " + opaqueComponentId.getClass()
                );
            }
        }

        return false;
    }

    public static boolean isLocalGroovyComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        if (componentId == null) {
            return false;
        }

        if (componentId instanceof OpaqueComponentIdentifier) {
            val opaqueComponentId = (OpaqueComponentIdentifier) componentId;
            if (getClassPathNotation != null) {
                val classPathNotation = getClassPathNotation.invoke(opaqueComponentId);
                return classPathNotation != null && classPathNotation.name().equals("LOCAL_GROOVY");

            } else if (getDisplayName != null) {
                val displayName = getDisplayName.invoke(opaqueComponentId);
                return displayName != null && displayName.equals("Local Groovy");

            } else {
                throw new IllegalStateException(
                    "No getClassPathNotation method found for " + opaqueComponentId.getClass()
                );
            }
        }

        return false;
    }

}
