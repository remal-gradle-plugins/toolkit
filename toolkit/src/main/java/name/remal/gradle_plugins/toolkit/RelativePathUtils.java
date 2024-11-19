package name.remal.gradle_plugins.toolkit;

import static java.lang.reflect.Proxy.newProxyInstance;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.RelativePath;

@NoArgsConstructor(access = PRIVATE)
public abstract class RelativePathUtils {

    public static FileTreeElement relativePathToMockedFileTreeElement(RelativePath relativePath) {
        return (FileTreeElement) newProxyInstance(
            FileTreeElement.class.getClassLoader(),
            new Class<?>[]{FileTreeElement.class},
            new ProxyInvocationHandler()
                .add(
                    method -> method.getParameterCount() == 0 && method.getName().equals("getRelativePath"),
                    (proxy, method, args) -> relativePath
                )
                .add(
                    method -> method.getParameterCount() == 0 && method.getName().equals("getPath"),
                    (proxy, method, args) -> relativePath.getPathString()
                )
                .add(
                    method -> method.getParameterCount() == 0 && method.getName().equals("getName"),
                    (proxy, method, args) -> relativePath.getLastName()
                )
                .add(
                    method -> method.getParameterCount() == 0 && method.getName().equals("isDirectory"),
                    (proxy, method, args) -> !relativePath.isFile()
                )
        );
    }

}
