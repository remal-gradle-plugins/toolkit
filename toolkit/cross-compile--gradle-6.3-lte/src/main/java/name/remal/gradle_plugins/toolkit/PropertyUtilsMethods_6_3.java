package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import org.gradle.api.provider.Property;

@AutoService(PropertyUtilsMethods.class)
final class PropertyUtilsMethods_6_3 implements PropertyUtilsMethods {

    @Override
    public void finalizeValueOnRead(Property<?> property) {
        property.finalizeValueOnRead();
    }

    @Override
    public void disallowChanges(Property<?> property) {
        property.disallowChanges();
    }

    @Override
    public void disallowUnsafeRead(Property<?> property) {
        // do nothing
    }

}
