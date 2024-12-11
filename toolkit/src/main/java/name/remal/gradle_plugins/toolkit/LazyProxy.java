package name.remal.gradle_plugins.toolkit;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.LazyValue.lazyValue;
import static name.remal.gradle_plugins.toolkit.SneakyThrowUtils.sneakyThrow;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.defineClass;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isNotAbstract;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isStatic;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;

@NoArgsConstructor(access = PRIVATE)
public abstract class LazyProxy {

    @Contract(pure = true)
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> T asLazyProxy(Class<T> interfaceClass, LazyValueSupplier<? extends T> lazyValueSupplier) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Not an interface: " + interfaceClass);
        }

        val lazyValue = lazyValue(lazyValueSupplier);

        val proxyClass = PROXY_CLASSES.getUnchecked(interfaceClass);
        val proxyCtor = proxyClass.getConstructor(LazyValue.class);
        val proxy = (T) proxyCtor.newInstance(lazyValue);
        return proxy;
    }

    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static <E> Collection<E> asLazyCollectionProxy(
        LazyValueSupplier<? extends Collection<E>> lazyValueSupplier
    ) {
        return asLazyProxy(Collection.class, lazyValueSupplier);
    }

    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static <E> List<E> asLazyListProxy(
        LazyValueSupplier<? extends List<E>> lazyValueSupplier
    ) {
        return asLazyProxy(List.class, lazyValueSupplier);
    }

    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static <E> Set<E> asLazySetProxy(
        LazyValueSupplier<? extends Set<E>> lazyValueSupplier
    ) {
        return asLazyProxy(Set.class, lazyValueSupplier);
    }

    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asLazyMapProxy(
        LazyValueSupplier<? extends Map<K, V>> lazyValueSupplier
    ) {
        return asLazyProxy(Map.class, lazyValueSupplier);
    }


    @Contract(pure = true)
    public static boolean isLazyProxy(Object object) {
        return object instanceof LazyProxyMarkerHolder.LazyProxyMarker;
    }

    @Contract(pure = true)
    @SneakyThrows
    public static boolean isLazyProxyInitialized(Object object) {
        Class<?> proxyClass = object.getClass();
        while (proxyClass != null && proxyClass != Object.class) {
            for (val interfaceClass : proxyClass.getInterfaces()) {
                if (interfaceClass == LazyProxyMarkerHolder.LazyProxyMarker.class) {
                    val lazyValueField = proxyClass.getField(LAZY_VALUE_FIELD_NODE);
                    val lazyValue = (LazyValue<?>) lazyValueField.get(object);
                    return lazyValue.isInitialized();
                }
            }

            proxyClass = proxyClass.getSuperclass();
        }

        throw new IllegalArgumentException("Not a lazy proxy: " + object);
    }


    private static final LoadingCache<Class<?>, Class<?>> PROXY_CLASSES = CacheBuilder.newBuilder()
        .weakKeys()
        .build(CacheLoader.from(LazyProxy::generateProxyClass));

    private static final String LAZY_VALUE_FIELD_NODE = "lazyValue";

    private static final Method LAZY_VALUE_GET_METHOD;
    private static final List<Method> OBJECT_METHODS_TO_IMPLEMENT;

    static {
        try {
            LAZY_VALUE_GET_METHOD = LazyValue.class.getMethod("get");
            OBJECT_METHODS_TO_IMPLEMENT = ImmutableList.of(
                Object.class.getMethod("toString")
            );
        } catch (NoSuchMethodException e) {
            throw sneakyThrow(e);
        }
    }

    @SuppressWarnings("java:S3776")
    private static Class<?> generateProxyClass(Class<?> interfaceClass) {
        val classNode = new ClassNode();
        classNode.version = V1_8;
        classNode.access = ACC_PUBLIC | ACC_SYNTHETIC;
        classNode.name = getInternalName(interfaceClass) + "$$LazyProxy";
        if (interfaceClass.getClassLoader() == null) {
            classNode.name = getInternalName(LazyProxy.class) + '$' + classNode.name.replace('/', '_');
        }
        classNode.superName = getInternalName(Object.class);
        classNode.interfaces = asList(
            getInternalName(interfaceClass),
            getInternalName(LazyProxyMarkerHolder.LazyProxyMarker.class)
        );
        classNode.fields = new ArrayList<>();
        classNode.methods = new ArrayList<>();

        val lazyValueField = new FieldNode(
            ACC_PUBLIC | ACC_FINAL,
            LAZY_VALUE_FIELD_NODE,
            getDescriptor(LazyValue.class),
            null,
            null
        );
        classNode.fields.add(lazyValueField);

        {
            val methodNode = new MethodNode(
                ACC_PUBLIC,
                "<init>",
                getMethodDescriptor(
                    VOID_TYPE,
                    getType(lazyValueField.desc)
                ),
                null,
                null
            );
            classNode.methods.add(methodNode);

            methodNode.parameters = singletonList(new ParameterNode(lazyValueField.name, ACC_FINAL));

            val instructions = methodNode.instructions = new InsnList();
            instructions.add(new LabelNode());

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new MethodInsnNode(
                INVOKESPECIAL,
                classNode.superName,
                "<init>",
                getMethodDescriptor(VOID_TYPE)
            ));

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new VarInsnNode(ALOAD, 1));
            instructions.add(new FieldInsnNode(
                PUTFIELD,
                classNode.name,
                lazyValueField.name,
                lazyValueField.desc
            ));

            instructions.add(new InsnNode(RETURN));
        }

        val methodsToDelegate = Stream.concat(
            stream(interfaceClass.getMethods()),
            OBJECT_METHODS_TO_IMPLEMENT.stream()
        ).collect(toList());
        for (val method : methodsToDelegate) {
            if (method.isSynthetic()
                || isStatic(method)
                || (method.getDeclaringClass().isInterface() && isNotAbstract(method))
            ) {
                continue;
            }

            val methodNode = new MethodNode(
                ACC_PUBLIC,
                method.getName(),
                getMethodDescriptor(method),
                null,
                null
            );
            val isAlreadyImplemented = classNode.methods.stream().anyMatch(other ->
                other.name.equals(methodNode.name)
                    && other.desc.equals(methodNode.desc)
            );
            if (isAlreadyImplemented) {
                continue;
            }
            classNode.methods.add(methodNode);

            methodNode.parameters = stream(method.getParameters())
                .map(Parameter::getName)
                .map(name -> new ParameterNode(name, ACC_FINAL))
                .collect(toList());

            val instructions = methodNode.instructions = new InsnList();
            instructions.add(new LabelNode());

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new FieldInsnNode(
                GETFIELD,
                classNode.name,
                lazyValueField.name,
                lazyValueField.desc
            ));
            instructions.add(new MethodInsnNode(
                LAZY_VALUE_GET_METHOD.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
                getInternalName(LAZY_VALUE_GET_METHOD.getDeclaringClass()),
                LAZY_VALUE_GET_METHOD.getName(),
                getMethodDescriptor(LAZY_VALUE_GET_METHOD)
            ));

            for (int paramIndex = 0; paramIndex < method.getParameterCount(); ++paramIndex) {
                val paramClass = method.getParameterTypes()[paramIndex];
                instructions.add(new VarInsnNode(getType(paramClass).getOpcode(ILOAD), paramIndex + 1));
            }

            instructions.add(new MethodInsnNode(
                method.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
                getInternalName(method.getDeclaringClass()),
                method.getName(),
                getMethodDescriptor(method)
            ));

            instructions.add(new InsnNode(getType(method.getReturnType()).getOpcode(IRETURN)));
        }

        val classWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        classNode.accept(new CheckClassAdapter(classWriter));
        val bytecode = classWriter.toByteArray();

        ClassLoader classLoader = interfaceClass.getClassLoader();
        if (classLoader == null) {
            classLoader = LazyProxy.class.getClassLoader();
        }

        return defineClass(classLoader, bytecode);
    }


    /**
     * {@link LazyProxyMarker} needs to be public, but this class should not be accessible to any source code.
     * To do that, we wrap it with a private class.
     */
    private interface LazyProxyMarkerHolder {
        interface LazyProxyMarker {
        }
    }

}
