package name.remal.gradle_plugins.toolkit.reflection;

import static java.util.jar.JarFile.MANIFEST_NAME;
import static name.remal.gradle_plugins.toolkit.reflection.ModuleNameParser.parseModuleName;

import com.google.auto.service.AutoService;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;

@AutoService(ReflectionUtilsMethods.class)
final class ReflectionUtilsMethods_8_lte implements ReflectionUtilsMethods {

    @Nullable
    @Override
    @SneakyThrows
    public String moduleNameOf(Class<?> clazz) {
        val locationUrl = Optional.ofNullable(clazz.getProtectionDomain())
            .map(ProtectionDomain::getCodeSource)
            .map(CodeSource::getLocation)
            .orElse(null);
        if (locationUrl == null) {
            return null;
        }

        try (val classLoader = new UrlResourcesOnlyClassLoader(locationUrl)) {
            return parseModuleName(
                () -> classLoader.getResourceAsStream("module-info.class"),
                () -> classLoader.getResourceAsStream(MANIFEST_NAME),
                locationUrl
            );
        }
    }

    private static class UrlResourcesOnlyClassLoader extends URLClassLoader {
        public UrlResourcesOnlyClassLoader(URL url) {
            super(new URL[]{url});
        }

        @Nullable
        @Override
        public URL getResource(String name) {
            return findResource(name);
        }

        @Override
        protected Class<?> findClass(String name) {
            throw new UnsupportedOperationException();
        }
    }

}
