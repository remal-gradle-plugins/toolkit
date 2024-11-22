package name.remal.gradle_plugins.toolkit.reflection;

import static java.util.Arrays.stream;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.getClassHierarchy;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class ClassMetadataBuilder {

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> ClassMetadata<T> buildClassMetadata(Class<T> introspectedClass) {
        return (ClassMetadata<T>) CACHE.get(introspectedClass);
    }

    private static final LoadingCache<Class<?>, ClassMetadata<?>> CACHE = CacheBuilder.newBuilder()
        .weakKeys()
        .build(CacheLoader.from(ClassMetadataBuilder::buildClassMetadataImpl));

    @SuppressWarnings("unchecked")
    private static <T> ClassMetadata<T> buildClassMetadataImpl(Class<T> introspectedClass) {
        val classMetadata = ClassMetadata.<T>builder()
            .introspectedClass(introspectedClass);

        // process constructors:
        stream(introspectedClass.getDeclaredConstructors())
            .filter(not(Member::isSynthetic))
            .map(ctor -> (Constructor<T>) ctor)
            .forEach(classMetadata::constructor);

        val classHierarchy = new ArrayList<>(getClassHierarchy(introspectedClass));
        classHierarchy.remove(Object.class);
        val introspectedTypeToken = TypeToken.of(introspectedClass);

        // process instance fields:
        classHierarchy.stream()
            .map(Class::getDeclaredFields)
            .flatMap(Arrays::stream)
            .filter(not(Member::isSynthetic))
            .filter(ReflectionUtils::isNotStatic)
            .forEach(classMetadata::instanceField);


        // process static fields:
        classHierarchy.stream()
            .map(Class::getDeclaredFields)
            .flatMap(Arrays::stream)
            .filter(not(Member::isSynthetic))
            .filter(ReflectionUtils::isStatic)
            .forEach(classMetadata::staticField);

        // process static methods:
        classHierarchy.stream()
            .map(Class::getDeclaredMethods)
            .flatMap(Arrays::stream)
            .filter(not(Member::isSynthetic))
            .filter(ReflectionUtils::isStatic)
            .forEach(classMetadata::staticMethod);


        return classMetadata.build();
    }

}
