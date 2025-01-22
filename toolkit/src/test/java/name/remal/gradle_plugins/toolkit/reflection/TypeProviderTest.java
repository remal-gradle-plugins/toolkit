package name.remal.gradle_plugins.toolkit.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.ParameterizedType;
import java.util.Queue;
import org.junit.jupiter.api.Test;

class TypeProviderTest {

    @Test
    void success() {
        var typeProvider = new TypeProvider<Queue<Long>>() { };

        var type = typeProvider.getType();
        assertInstanceOf(ParameterizedType.class, type);
        var parameterizedType = (ParameterizedType) type;
        assertEquals(Queue.class, parameterizedType.getRawType());
        assertEquals(Long.class, parameterizedType.getActualTypeArguments()[0]);

        assertEquals(Queue.class, typeProvider.getRawClass());
    }

    @Test
    @SuppressWarnings("rawtypes")
    void failure() {
        assertThrows(IllegalStateException.class, () -> new TypeProvider() { });
    }

}
