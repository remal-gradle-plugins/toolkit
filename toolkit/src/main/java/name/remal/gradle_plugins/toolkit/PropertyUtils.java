package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;
import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyProxy;

import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class PropertyUtils {

    private static final PropertyUtilsMethods METHODS = asLazyProxy(
        PropertyUtilsMethods.class,
        () -> loadCrossCompileService(PropertyUtilsMethods.class)
    );


    @Contract("_->param1")
    public static <P extends Property<?>> P finalizeValue(P property) {
        property.finalizeValue();
        return property;
    }

    @Contract("_->param1")
    public static <P extends Property<?>> P finalizeValueOnRead(P property) {
        METHODS.finalizeValueOnRead(property);
        return property;
    }

    @Contract("_->param1")
    public static <P extends Property<?>> P disallowChanges(P property) {
        METHODS.disallowChanges(property);
        return property;
    }

    @Contract("_->param1")
    public static <P extends Property<?>> P disallowUnsafeRead(P property) {
        METHODS.disallowUnsafeRead(property);
        return property;
    }


    public static <T, P extends Property<T>> T getFinalized(P provider) {
        return finalizeValue(provider).get();
    }

    @Nullable
    public static <T, P extends Property<T>> T getOrNullFinalized(P provider) {
        return finalizeValue(provider).getOrNull();
    }

}
