package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;

import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.provider.HasConfigurableValue;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class ProviderUtils {

    private static final ProviderUtilsMethods METHODS = loadCrossCompileService(ProviderUtilsMethods.class);


    /**
     * See {@link Project#provider(Callable)}.
     */
    public static <T> Provider<T> newProvider(Callable<? extends T> callable) {
        return METHODS.newProvider(callable);
    }

    /**
     * <p>{@code noll} value is treated as missing value. See {@link Provider#get()}.</p>
     */
    public static <T> Provider<T> newFixedProvider(@Nullable T value) {
        return METHODS.newFixedProvider(value);
    }


    @Contract("_->param1")
    public static <P extends Provider<?> & HasConfigurableValue> P disallowChanges(P provider) {
        provider.disallowChanges();
        return provider;
    }

    public static <
        P extends Provider<?> & HasConfigurableValue,
        V
        > Transformer<@org.jetbrains.annotations.Nullable P, V> toDisallowedChanges(
        Transformer<@org.jetbrains.annotations.Nullable P, V> transformer
    ) {
        return in -> {
            val result = transformer.transform(in);
            return result != null ? disallowChanges(result) : null;
        };
    }

    @Contract("_->param1")
    public static <P extends Provider<?> & HasConfigurableValue> P finalizeValue(P provider) {
        provider.finalizeValue();
        return provider;
    }

    public static <
        P extends Provider<?> & HasConfigurableValue,
        V
        > Transformer<@org.jetbrains.annotations.Nullable P, V> toFinalizedValue(
        Transformer<@org.jetbrains.annotations.Nullable P, V> transformer
    ) {
        return in -> {
            val result = transformer.transform(in);
            return result != null ? finalizeValue(result) : null;
        };
    }

    public static <T, P extends Provider<T> & HasConfigurableValue> T getFinalized(P provider) {
        return finalizeValue(provider).get();
    }

    @Nullable
    public static <T, P extends Provider<T> & HasConfigurableValue> T getOrNullFinalized(P provider) {
        return finalizeValue(provider).getOrNull();
    }

}
