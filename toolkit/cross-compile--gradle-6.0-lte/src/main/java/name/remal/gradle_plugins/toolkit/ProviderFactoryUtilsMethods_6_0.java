package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;
import org.gradle.api.provider.ProviderFactory;

@AutoService(ProviderFactoryUtilsMethods.class)
final class ProviderFactoryUtilsMethods_6_0 implements ProviderFactoryUtilsMethods {

    @Nullable
    @Override
    public String getEnvironmentVariable(ProviderFactory providers, String name) {
        return System.getenv(name);
    }

    @Nullable
    @Override
    public String getSystemProperty(ProviderFactory providers, String name) {
        return System.getProperty(name);
    }

}
