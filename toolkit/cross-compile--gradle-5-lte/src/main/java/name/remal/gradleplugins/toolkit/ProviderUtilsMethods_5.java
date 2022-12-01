package name.remal.gradleplugins.toolkit;

import com.google.auto.service.AutoService;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import org.gradle.api.internal.provider.DefaultProvider;
import org.gradle.api.provider.Provider;

@AutoService(ProviderUtilsMethods.class)
@SuppressWarnings("UnstableApiUsage")
final class ProviderUtilsMethods_5 implements ProviderUtilsMethods {

    @Override
    public <T> Provider<T> newProvider(Callable<? extends T> callable) {
        return new DefaultProvider<>(callable);
    }

    @Override
    public <T> Provider<T> newFixedProvider(@Nullable T value) {
        return new DefaultProvider<T>(() -> value) {
            @Nullable
            @Override
            @SuppressWarnings("unchecked")
            public Class<T> getType() {
                return value != null ? (Class<T>) value.getClass() : null;
            }

            @Override
            public String toString() {
                return String.format("fixed(%s, %s)", getType(), getOrNull());
            }
        };
    }

}
