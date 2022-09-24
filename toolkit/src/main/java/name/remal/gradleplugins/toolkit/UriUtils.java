package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.net.URI;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = PRIVATE)
public abstract class UriUtils {

    @SneakyThrows
    public static URI parseUri(String string) {
        return new URI(string);
    }

}
