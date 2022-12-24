package name.remal.gradle_plugins.toolkit;

import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import org.gradle.api.provider.Provider;

interface ProviderUtilsMethods {

    <T> Provider<T> newProvider(Callable<? extends T> callable);

    <T> Provider<T> newFixedProvider(@Nullable T value);

}
