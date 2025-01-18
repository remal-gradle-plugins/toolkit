package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class UriUtils {

    @Contract(pure = true)
    @SneakyThrows
    public static URI parseUri(String string) {
        return new URI(string);
    }

    @Contract(pure = true)
    @SneakyThrows
    public static URI toUri(URL url) {
        return url.toURI();
    }

    public static URI toUri(File file) {
        return file.toURI();
    }

    public static URI toUri(Path path) {
        return path.toUri();
    }

}
