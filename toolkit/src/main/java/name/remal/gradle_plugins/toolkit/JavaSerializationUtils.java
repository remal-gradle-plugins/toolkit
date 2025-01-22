package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = PRIVATE)
public abstract class JavaSerializationUtils {

    @SneakyThrows
    public static byte[] serializeToBytes(Object object) {
        try (var bytesOutputStream = new ByteArrayOutputStream()) {
            try (var outputStream = new ObjectOutputStream(bytesOutputStream)) {
                outputStream.writeObject(object);
            }
            return bytesOutputStream.toByteArray();
        }
    }

    @SneakyThrows
    public static <T> T deserializeFrom(byte[] bytes, Class<T> type) {
        try (var bytesInputStream = new ByteArrayInputStream(bytes)) {
            try (var inputStream = new ObjectInputStream(bytesInputStream)) {
                var object = inputStream.readObject();
                var typedObject = type.cast(object);
                return typedObject;
            }
        }
    }

}
