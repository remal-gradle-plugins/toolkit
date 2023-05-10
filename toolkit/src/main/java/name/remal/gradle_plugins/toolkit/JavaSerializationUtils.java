package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class JavaSerializationUtils {

    @SneakyThrows
    public static byte[] serializeToBytes(Object object) {
        try (val bytesOutputStream = new ByteArrayOutputStream()) {
            try (val outputStream = new ObjectOutputStream(bytesOutputStream)) {
                outputStream.writeObject(object);
            }
            return bytesOutputStream.toByteArray();
        }
    }

    @SneakyThrows
    public static <T> T deserializeFrom(byte[] bytes, Class<T> type) {
        try (val bytesInputStream = new ByteArrayInputStream(bytes)) {
            try (val inputStream = new ObjectInputStream(bytesInputStream)) {
                val object = inputStream.readObject();
                val typedObject = type.cast(object);
                return typedObject;
            }
        }
    }

}
