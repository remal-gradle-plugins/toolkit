package name.remal.gradle_plugins.toolkit;

import org.gradle.api.provider.Property;

interface PropertyUtilsMethods {

    void finalizeValueOnRead(Property<?> property);

    void disallowChanges(Property<?> property);

    void disallowUnsafeRead(Property<?> property);

}
