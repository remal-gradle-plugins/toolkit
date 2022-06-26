package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.net.URI;
import java.net.URL;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = PRIVATE)
public abstract class UrlUtils {

    @SneakyThrows
    public static URL parseUrl(String string) {
        return new URL(string);
    }

    @SneakyThrows
    public static URL toUrl(URI uri) {
        return uri.toURL();
    }

}
