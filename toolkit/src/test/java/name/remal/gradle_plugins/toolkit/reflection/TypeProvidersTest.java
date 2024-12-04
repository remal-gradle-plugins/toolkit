package name.remal.gradle_plugins.toolkit.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.val;
import org.junit.jupiter.api.Test;

class TypeProvidersTest {

    @Test
    void listTypeProvider() {
        val typeProvider = TypeProviders.listTypeProvider(String.class);

        val type = typeProvider.getType();
        assertInstanceOf(ParameterizedType.class, type);
        val parameterizedType = (ParameterizedType) type;
        assertEquals(List.class, parameterizedType.getRawType());
        assertEquals(String.class, parameterizedType.getActualTypeArguments()[0]);

        assertEquals(List.class, typeProvider.getRawClass());
    }

    @Test
    void setTypeProvider() {
        val typeProvider = TypeProviders.setTypeProvider(Byte.class);

        val type = typeProvider.getType();
        assertInstanceOf(ParameterizedType.class, type);
        val parameterizedType = (ParameterizedType) type;
        assertEquals(Set.class, parameterizedType.getRawType());
        assertEquals(Byte.class, parameterizedType.getActualTypeArguments()[0]);

        assertEquals(Set.class, typeProvider.getRawClass());
    }

    @Test
    void mapTypeProvider() {
        val typeProvider = TypeProviders.mapTypeProvider(String.class, Integer.class);

        val type = typeProvider.getType();
        assertInstanceOf(ParameterizedType.class, type);
        val parameterizedType = (ParameterizedType) type;
        assertEquals(Map.class, parameterizedType.getRawType());
        assertEquals(String.class, parameterizedType.getActualTypeArguments()[0]);
        assertEquals(Integer.class, parameterizedType.getActualTypeArguments()[1]);

        assertEquals(Map.class, typeProvider.getRawClass());
    }

}
