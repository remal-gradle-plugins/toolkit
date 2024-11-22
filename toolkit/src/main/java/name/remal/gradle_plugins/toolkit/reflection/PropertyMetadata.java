package name.remal.gradle_plugins.toolkit.reflection;

import static lombok.AccessLevel.PACKAGE;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.jetbrains.annotations.Unmodifiable;

@Value
@Builder(access = PACKAGE)
public class PropertyMetadata<CLASS, TYPE> {

    Class<CLASS> declaringClass;

    String name;

    Class<TYPE> type;

    Type genericType;

    @Nullable
    Field field;

    @Unmodifiable
    @Singular
    List<Method> getters;

    @Unmodifiable
    @Singular
    List<Method> setters;

}
