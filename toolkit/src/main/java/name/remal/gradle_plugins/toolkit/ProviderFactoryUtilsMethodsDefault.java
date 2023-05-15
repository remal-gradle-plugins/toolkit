package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;
import org.gradle.api.provider.ProviderFactory;

@AutoService(ProviderFactoryUtilsMethods.class)
final class ProviderFactoryUtilsMethodsDefault implements ProviderFactoryUtilsMethods {

    @Nullable
    @Override
    public String getEnvironmentVariable(ProviderFactory providers, String name) {
        return providers.environmentVariable(name).getOrNull();
    }

    @Nullable
    @Override
    public String getSystemProperty(ProviderFactory providers, String name) {
        return providers.systemProperty(name).getOrNull();
    }

}
