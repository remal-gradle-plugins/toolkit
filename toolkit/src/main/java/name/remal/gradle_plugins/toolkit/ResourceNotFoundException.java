package name.remal.gradle_plugins.toolkit;

import org.intellij.lang.annotations.Language;

public class ResourceNotFoundException extends IllegalStateException {

    ResourceNotFoundException(@Language("file-reference") String resourceName) {
        super("Classpath resource can't be found: " + resourceName);
    }

    ResourceNotFoundException(Class<?> loadingClass, @Language("file-reference") String resourceName) {
        super("Classpath resource can't be found (for loading " + loadingClass + "): " + resourceName);
    }

}
