package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.findMethod;

import java.util.Collection;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.internal.deprecation.DeprecatableConfiguration;

@NoArgsConstructor(access = PRIVATE)
public abstract class ConfigurationUtils {

    public static boolean isDeprecatedForConsumption(Configuration conf) {
        val deprecatableConf = (DeprecatableConfiguration) conf;

        val isDeprecatedForConsumption = findMethod(
            DeprecatableConfiguration.class,
            boolean.class,
            "isDeprecatedForConsumption"
        );

        val getConsumptionDeprecation = findMethod(
            DeprecatableConfiguration.class,
            Object.class,
            "getConsumptionDeprecation"
        );

        val getConsumptionAlternatives = findMethod(
            DeprecatableConfiguration.class,
            Collection.class,
            "getConsumptionAlternatives"
        );

        if (isDeprecatedForConsumption != null
            && getConsumptionDeprecation != null
            && getConsumptionAlternatives != null
        ) {
            return isDeprecatedForConsumption.invoke(deprecatableConf)
                || getConsumptionDeprecation.invoke(deprecatableConf) != null
                || isNotEmpty(getConsumptionAlternatives.invoke(deprecatableConf));

        } else if (isDeprecatedForConsumption != null) {
            return isDeprecatedForConsumption.invoke(deprecatableConf);

        } else if (getConsumptionDeprecation != null) {
            return getConsumptionDeprecation.invoke(deprecatableConf) != null;

        } else if (getConsumptionAlternatives != null) {
            return isNotEmpty(getConsumptionAlternatives.invoke(deprecatableConf));

        } else {
            throw new AssertionError(format(
                "%s doesn't have any of candidate methods: `%s`, `%s`, or %s",
                DeprecatableConfiguration.class,
                "isDeprecatedForConsumption",
                "getConsumptionDeprecation",
                "getConsumptionAlternatives"
            ));
        }
    }

    public static boolean isDeprecatedForResolution(Configuration conf) {
        val deprecatableConf = (DeprecatableConfiguration) conf;

        val isDeprecatedForResolution = findMethod(
            DeprecatableConfiguration.class,
            boolean.class,
            "isDeprecatedForResolution"
        );

        val getResolutionAlternatives = findMethod(
            DeprecatableConfiguration.class,
            Collection.class,
            "getResolutionAlternatives"
        );

        if (isDeprecatedForResolution != null && getResolutionAlternatives != null) {
            return isDeprecatedForResolution.invoke(deprecatableConf)
                || isNotEmpty(getResolutionAlternatives.invoke(deprecatableConf));

        } else if (isDeprecatedForResolution != null) {
            return isDeprecatedForResolution.invoke(deprecatableConf);

        } else if (getResolutionAlternatives != null) {
            return isNotEmpty(getResolutionAlternatives.invoke(deprecatableConf));

        } else {
            throw new AssertionError(format(
                "%s doesn't have any of candidate methods: `%s`, or %s",
                DeprecatableConfiguration.class,
                "isDeprecatedForResolution",
                "getResolutionAlternatives"
            ));
        }
    }

}
