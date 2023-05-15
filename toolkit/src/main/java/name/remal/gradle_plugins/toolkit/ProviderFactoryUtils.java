package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;

import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.gradle.api.provider.ProviderFactory;

@NoArgsConstructor(access = PRIVATE)
public abstract class ProviderFactoryUtils {

    private static final ProviderFactoryUtilsMethods METHODS =
        loadCrossCompileService(ProviderFactoryUtilsMethods.class);

    @Nullable
    public static String getEnvironmentVariable(ProviderFactory providers, String name) {
        return METHODS.getEnvironmentVariable(providers, name);
    }

    @Nullable
    public static String getSystemProperty(ProviderFactory providers, String name) {
        return METHODS.getSystemProperty(providers, name);
    }

}
