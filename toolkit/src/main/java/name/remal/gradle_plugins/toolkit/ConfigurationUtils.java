package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.findMethod;

import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import name.remal.gradle_plugins.toolkit.reflection.TypedMethod0;
import org.gradle.api.artifacts.Configuration;

@NoArgsConstructor(access = PRIVATE)
public abstract class ConfigurationUtils {

    @ReliesOnInternalGradleApi
    @SuppressWarnings("unchecked")
    public static boolean isConfigurationDependenciesDeclarationDeprecated(Configuration configuration) {
        val getDeclarationAlternativesMethod = (TypedMethod0<Configuration, Object>) findMethod(
            configuration.getClass(),
            Object.class,
            "getDeclarationAlternatives"
        );
        if (getDeclarationAlternativesMethod != null) {
            val declarationAlternatives = getDeclarationAlternativesMethod.invoke(configuration);
            return declarationAlternatives != null;
        }
        return false;
    }

    @ReliesOnInternalGradleApi
    @SuppressWarnings("unchecked")
    public static boolean isConfigurationConsumptionDeprecated(Configuration configuration) {
        val getConsumptionDeprecationMethod = (TypedMethod0<Configuration, Object>) findMethod(
            configuration.getClass(),
            Object.class,
            "getConsumptionDeprecation"
        );
        if (getConsumptionDeprecationMethod != null) {
            val consumptionDeprecation = getConsumptionDeprecationMethod.invoke(configuration);
            return consumptionDeprecation != null;
        }
        return false;
    }

    @ReliesOnInternalGradleApi
    @SuppressWarnings("unchecked")
    public static boolean isConfigurationResolutionDeprecated(Configuration configuration) {
        val getResolutionAlternativesMethod = (TypedMethod0<Configuration, Object>) findMethod(
            configuration.getClass(),
            Object.class,
            "getResolutionAlternatives"
        );
        if (getResolutionAlternativesMethod != null) {
            val resolutionAlternatives = getResolutionAlternativesMethod.invoke(configuration);
            return resolutionAlternatives != null;
        }
        return false;
    }

}
