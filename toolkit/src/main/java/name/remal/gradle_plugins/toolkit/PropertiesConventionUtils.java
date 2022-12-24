package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.Callable;
import java.util.function.Function;
import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.internal.IConventionAware;

@ReliesOnInternalGradleApi
@NoArgsConstructor(access = PRIVATE)
public abstract class PropertiesConventionUtils {

    /**
     * Specifies the value to use as the convention for this property.
     * The convention is used when no value has been set for this property.
     */
    public static void setPropertyConvention(Object object, String propertyName, Callable<?> valueSupplier) {
        if (!(object instanceof IConventionAware)) {
            throw new IllegalArgumentException("Not an instance of " + IConventionAware.class + ": " + object);
        }
        val conventionAware = (IConventionAware) object;
        val conventionMapping = conventionAware.getConventionMapping();
        conventionMapping.map(propertyName, valueSupplier);
    }

    /**
     * Specifies the value to use as the convention for this property.
     * The convention is used when no value has been set for this property.
     */
    public static <T> void setPropertyConvention(T object, String propertyName, Function<T, ?> valueSupplier) {
        setPropertyConvention(object, propertyName, () -> valueSupplier.apply(object));
    }

}
