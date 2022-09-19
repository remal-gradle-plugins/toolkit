package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = PRIVATE)
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
