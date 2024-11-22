package name.remal.gradle_plugins.toolkit.reflection;

import static lombok.AccessLevel.PACKAGE;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.jetbrains.annotations.Unmodifiable;

@Value
@Builder(access = PACKAGE)
public class ClassMetadata<CLASS> {

    Class<CLASS> introspectedClass;


    @Unmodifiable
    @Singular
    List<Constructor<CLASS>> constructors;

    @Unmodifiable
    @Singular
    List<Field> instanceFields;

    @Unmodifiable
    @Singular
    List<Method> instanceMethods;

    @Unmodifiable
    @Singular
    List<PropertyMetadata<? super CLASS, ?>> properties;


    @Unmodifiable
    @Singular
    List<Field> staticFields;

    @Unmodifiable
    @Singular
    List<Method> staticMethods;

}
