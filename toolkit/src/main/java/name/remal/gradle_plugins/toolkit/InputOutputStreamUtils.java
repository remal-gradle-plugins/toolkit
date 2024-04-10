package name.remal.gradle_plugins.toolkit;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
public abstract class InputOutputStreamUtils {

    @FunctionalInterface
    public interface InputStreamOnClose {
        void execute(InputStream inputStream) throws Throwable;
    }

    public static InputStream withOnClose(InputStream inputStream, InputStreamOnClose onClose) {
        return new FilterInputStream(inputStream) {
            @Override
            @SneakyThrows
            public void close() {
                try {
                    super.close();
                } finally {
                    onClose.execute(inputStream);
                }
            }
        };
    }

    @SneakyThrows
    public static byte[] readBytesFromStream(InputStream inputStream) {
        return toByteArray(inputStream);
    }

    public static String readStringFromStream(InputStream inputStream, Charset charset) {
        val bytes = readBytesFromStream(inputStream);
        return new String(bytes, charset);
    }

    public static String readStringFromStream(InputStream inputStream) {
        return readStringFromStream(inputStream, UTF_8);
    }

    @FunctionalInterface
    public interface OutputStreamOnClose {
        void execute(OutputStream outputStream) throws Throwable;
    }

    public static OutputStream withOnClose(OutputStream outputStream, OutputStreamOnClose onClose) {
        return new FilterOutputStream(outputStream) {
            @Override
            @SneakyThrows
            public void close() {
                try {
                    super.close();
                } finally {
                    onClose.execute(outputStream);
                }
            }
        };
    }

}
