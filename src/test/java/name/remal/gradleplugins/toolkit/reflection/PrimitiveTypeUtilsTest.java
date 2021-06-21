package name.remal.gradleplugins.toolkit.reflection;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class PrimitiveTypeUtilsTest {

    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPES = new LinkedHashMap<>();

    static {
        List<Class<?>> wrapperTypes = asList(
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Void.class
        );
        for (val wrapperType : wrapperTypes) {
            try {
                val primitiveType = (Class<?>) wrapperType.getField("TYPE").get(wrapperType);
                WRAPPER_TO_PRIMITIVE_TYPES.put(wrapperType, primitiveType);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Test
    void wrapPrimitiveType() {
        WRAPPER_TO_PRIMITIVE_TYPES.forEach((wrapperType, primitiveType) -> {
            assertEquals(
                wrapperType,
                PrimitiveTypeUtils.wrapPrimitiveType(wrapperType),
                primitiveType::getCanonicalName
            );
            assertEquals(
                wrapperType,
                PrimitiveTypeUtils.wrapPrimitiveType(primitiveType),
                primitiveType::getCanonicalName
            );
        });

        assertEquals(String.class, PrimitiveTypeUtils.wrapPrimitiveType(String.class));
        assertEquals(Collection.class, PrimitiveTypeUtils.wrapPrimitiveType(Collection.class));
    }

    @Test
    void unwrapPrimitiveType() {
        WRAPPER_TO_PRIMITIVE_TYPES.forEach((wrapperType, primitiveType) -> {
            assertEquals(
                primitiveType,
                PrimitiveTypeUtils.unwrapPrimitiveType(wrapperType),
                primitiveType::getCanonicalName
            );
            assertEquals(
                primitiveType,
                PrimitiveTypeUtils.unwrapPrimitiveType(primitiveType),
                primitiveType::getCanonicalName
            );
        });

        assertEquals(String.class, PrimitiveTypeUtils.unwrapPrimitiveType(String.class));
        assertEquals(Collection.class, PrimitiveTypeUtils.unwrapPrimitiveType(Collection.class));
    }

}
