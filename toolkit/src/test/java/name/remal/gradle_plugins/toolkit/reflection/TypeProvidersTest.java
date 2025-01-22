package name.remal.gradle_plugins.toolkit.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TypeProvidersTest {

    @Test
    void listTypeProvider() {
        var typeProvider = TypeProviders.listTypeProvider(String.class);

        var type = typeProvider.getType();
        assertInstanceOf(ParameterizedType.class, type);
        var parameterizedType = (ParameterizedType) type;
        assertEquals(List.class, parameterizedType.getRawType());
        assertEquals(String.class, parameterizedType.getActualTypeArguments()[0]);

        assertEquals(List.class, typeProvider.getRawClass());
    }

    @Test
    void setTypeProvider() {
        var typeProvider = TypeProviders.setTypeProvider(Byte.class);

        var type = typeProvider.getType();
        assertInstanceOf(ParameterizedType.class, type);
        var parameterizedType = (ParameterizedType) type;
        assertEquals(Set.class, parameterizedType.getRawType());
        assertEquals(Byte.class, parameterizedType.getActualTypeArguments()[0]);

        assertEquals(Set.class, typeProvider.getRawClass());
    }

    @Test
    void mapTypeProvider() {
        var typeProvider = TypeProviders.mapTypeProvider(String.class, Integer.class);

        var type = typeProvider.getType();
        assertInstanceOf(ParameterizedType.class, type);
        var parameterizedType = (ParameterizedType) type;
        assertEquals(Map.class, parameterizedType.getRawType());
        assertEquals(String.class, parameterizedType.getActualTypeArguments()[0]);
        assertEquals(Integer.class, parameterizedType.getActualTypeArguments()[1]);

        assertEquals(Map.class, typeProvider.getRawClass());
    }

}
