package name.remal.gradleplugins.toolkit;

import com.google.auto.service.AutoService;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import org.gradle.api.internal.provider.DefaultProvider;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Provider;

@AutoService(ProviderUtilsMethods.class)
final class ProviderUtilsMethodsDefault implements ProviderUtilsMethods {

    @Override
    public <T> Provider<T> newProvider(Callable<? extends T> callable) {
        return new DefaultProvider<>(callable);
    }

    @Override
    public <T> Provider<T> newFixedProvider(@Nullable T value) {
        return Providers.ofNullable(value);
    }

}
