package name.remal.gradle_plugins.toolkit.reflection;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeProvider<T> {

    protected TypeProvider() {
        getType();
    }

    public Type getType() {
        var superType = getClass().getGenericSuperclass();
        if (!(superType instanceof ParameterizedType)) {
            throw new IllegalStateException("Not a parameterized class: " + getClass());
        }
        return ((ParameterizedType) superType).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    public Class<T> getRawClass() {
        return (Class<T>) TypeToken.of(getType()).getRawType();
    }

}
