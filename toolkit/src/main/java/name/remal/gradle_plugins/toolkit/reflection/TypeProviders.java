package name.remal.gradle_plugins.toolkit.reflection;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.reflect.TypeUtils.parameterize;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class TypeProviders {

    public static <E> TypeProvider<List<E>> listTypeProvider(Class<E> elementType) {
        return new TypeProvider<List<E>>() {
            @Override
            public Type getType() {
                return parameterize(List.class, elementType);
            }
        };
    }

    public static <E> TypeProvider<Set<E>> setTypeProvider(Class<E> elementType) {
        return new TypeProvider<Set<E>>() {
            @Override
            public Type getType() {
                return parameterize(Set.class, elementType);
            }
        };
    }

    public static <K, V> TypeProvider<Map<K, V>> mapTypeProvider(Class<K> keyType, Class<V> valueType) {
        return new TypeProvider<Map<K, V>>() {
            @Override
            public Type getType() {
                return parameterize(Map.class, keyType, valueType);
            }
        };
    }

}
