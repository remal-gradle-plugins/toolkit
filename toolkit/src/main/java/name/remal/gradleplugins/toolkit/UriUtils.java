package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = PRIVATE)
public abstract class UriUtils {

    @SneakyThrows
    public static URI parseUri(String string) {
        return new URI(string);
    }

    public static URI toUri(File file) {
        return file.toURI();
    }

    public static URI toUri(Path path) {
        return path.toUri();
    }

}
