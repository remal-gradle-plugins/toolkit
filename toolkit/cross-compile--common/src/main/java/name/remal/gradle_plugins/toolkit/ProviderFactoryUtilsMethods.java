package name.remal.gradle_plugins.toolkit;

import javax.annotation.Nullable;
import org.gradle.api.provider.ProviderFactory;

interface ProviderFactoryUtilsMethods {

    @Nullable
    String getEnvironmentVariable(ProviderFactory providers, String name);

    @Nullable
    String getSystemProperty(ProviderFactory providers, String name);

}
