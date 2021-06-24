package name.remal.gradleplugins.toolkit;

import java.util.concurrent.Callable;
import lombok.val;
import org.gradle.api.internal.IConventionAware;

public interface PropertiesConventionUtils {

    /**
     * Specifies the value to use as the convention for this property.
     * The convention is used when no value has been set for this property.
     */
    static void setPropertyConvention(Object object, String propertyName, Callable<?> valueSupplier) {
        val conventionMapping = ((IConventionAware) object).getConventionMapping();
        conventionMapping.map(propertyName, valueSupplier);
    }

}
