package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.io.BufferedInputStream;
import java.io.InputStream;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class InputStreamUtils {

    public static BufferedInputStream toBufferedInputStream(InputStream inputStream) {
        if (inputStream instanceof BufferedInputStream) {
            return (BufferedInputStream) inputStream;
        } else {
            return new BufferedInputStream(inputStream);
        }
    }

}
