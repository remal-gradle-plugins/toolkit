package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.Callable;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.internal.IConventionAware;

@NoArgsConstructor(access = PRIVATE)
public abstract class PropertiesConventionUtils {

    /**
     * Specifies the value to use as the convention for this property.
     * The convention is used when no value has been set for this property.
     */
    public static void setPropertyConvention(Object object, String propertyName, Callable<?> valueSupplier) {
        val conventionMapping = ((IConventionAware) object).getConventionMapping();
        conventionMapping.map(propertyName, valueSupplier);
    }

}
