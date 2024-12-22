package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.findMethod;

import java.util.Objects;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import name.remal.gradle_plugins.toolkit.reflection.TypedMethod0;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.internal.component.local.model.OpaqueComponentIdentifier;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class ComponentIdentifierUtils {

    @ReliesOnInternalGradleApi
    @SuppressWarnings("rawtypes")
    private static final TypedMethod0<OpaqueComponentIdentifier, Enum> getClassPathNotation =
        findMethod(OpaqueComponentIdentifier.class, Enum.class, "getClassPathNotation");

    @ReliesOnInternalGradleApi
    private static final TypedMethod0<OpaqueComponentIdentifier, String> getDisplayName =
        findMethod(OpaqueComponentIdentifier.class, String.class, "getDisplayName");

    @Contract("null->false")
    @ReliesOnInternalGradleApi
    public static boolean isEmbeddedGradleComponentIdentifier(@Nullable ComponentIdentifier componentId) {
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

    @Contract("null->false")
    @ReliesOnInternalGradleApi
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

    @Contract("null->false")
    @ReliesOnInternalGradleApi
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

    @Contract("null->false")
    @ReliesOnInternalGradleApi
    public static boolean isEmbeddedLocalGroovyComponentIdentifier(@Nullable ComponentIdentifier componentId) {
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


    @Contract("null->false")
    public static boolean isExternalGradleComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        return isExternalGradleApiComponentIdentifier(componentId)
            || isExternalGradleTestKitComponentIdentifier(componentId)
            || isExternalLocalGroovyComponentIdentifier(componentId);
    }

    @Contract("null->false")
    public static boolean isExternalGradleApiComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        if (componentId == null) {
            return false;
        }

        if (componentId instanceof ModuleComponentIdentifier) {
            val moduleComponentId = (ModuleComponentIdentifier) componentId;
            if (Objects.equals(moduleComponentId.getGroup(), "name.remal.gradle-api")
                && Objects.equals(moduleComponentId.getModule(), "gradle-api")
            ) {
                return true;
            }
        }

        return false;
    }

    @Contract("null->false")
    public static boolean isExternalGradleTestKitComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        if (componentId == null) {
            return false;
        }

        if (componentId instanceof ModuleComponentIdentifier) {
            val moduleComponentId = (ModuleComponentIdentifier) componentId;
            if (Objects.equals(moduleComponentId.getGroup(), "name.remal.gradle-api")
                && Objects.equals(moduleComponentId.getModule(), "gradle-test-kit")
            ) {
                return true;
            }
        }

        return false;
    }

    @Contract("null->false")
    public static boolean isExternalLocalGroovyComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        if (componentId == null) {
            return false;
        }

        if (componentId instanceof ModuleComponentIdentifier) {
            val moduleComponentId = (ModuleComponentIdentifier) componentId;
            if (Objects.equals(moduleComponentId.getGroup(), "name.remal.gradle-api")
                && Objects.equals(moduleComponentId.getModule(), "local-groovy")
            ) {
                return true;
            }
        }

        return false;
    }


    @Contract("null->false")
    public static boolean isGradleComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        return isEmbeddedGradleComponentIdentifier(componentId)
            || isExternalGradleComponentIdentifier(componentId);
    }

    @Contract("null->false")
    public static boolean isGradleApiComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        return isEmbeddedGradleApiComponentIdentifier(componentId)
            || isExternalGradleApiComponentIdentifier(componentId);
    }

    @Contract("null->false")
    public static boolean isGradleTestKitComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        return isEmbeddedGradleTestKitComponentIdentifier(componentId)
            || isExternalGradleTestKitComponentIdentifier(componentId);
    }

    @Contract("null->false")
    public static boolean isLocalGroovyComponentIdentifier(@Nullable ComponentIdentifier componentId) {
        return isEmbeddedLocalGroovyComponentIdentifier(componentId)
            || isExternalLocalGroovyComponentIdentifier(componentId);
    }

}
