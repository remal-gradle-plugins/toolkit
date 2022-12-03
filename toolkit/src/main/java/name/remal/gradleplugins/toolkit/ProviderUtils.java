package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.CrossCompileServices.loadCrossCompileService;

import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

@NoArgsConstructor(access = PRIVATE)
public abstract class ProviderUtils {

    private static final ProviderUtilsMethods METHODS =
        loadCrossCompileService(ProviderUtilsMethods.class);


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

}
