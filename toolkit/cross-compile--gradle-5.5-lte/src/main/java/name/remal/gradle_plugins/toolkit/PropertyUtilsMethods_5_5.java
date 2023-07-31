package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import org.gradle.api.provider.Property;

@AutoService(PropertyUtilsMethods.class)
final class PropertyUtilsMethods_5_5 implements PropertyUtilsMethods {

    @Override
    public void finalizeValueOnRead(Property<?> property) {
        // do nothing
    }

    @Override
    public void disallowChanges(Property<?> property) {
        // do nothing
    }

    @Override
    public void disallowUnsafeRead(Property<?> property) {
        // do nothing
    }

}
