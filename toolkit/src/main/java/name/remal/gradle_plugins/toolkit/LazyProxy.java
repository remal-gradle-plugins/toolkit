package name.remal.gradle_plugins.toolkit;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.SneakyThrowUtils.sneakyThrow;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.defineClass;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isNotAbstract;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isStatic;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.reflection.TypeProvider;
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

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> T asLazyProxy(Class<T> interfaceClass, LazyValue<? extends T> lazyValue) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Not an interface: " + interfaceClass);
        }

        val proxyClass = PROXY_CLASSES.getUnchecked(interfaceClass);
        val proxyCtor = proxyClass.getConstructor(LazyValue.class);
        val proxy = (T) proxyCtor.newInstance(lazyValue);
        return proxy;
    }

    public static <T> T asLazyProxy(TypeProvider<T> interfaceTypeProvider, LazyValue<? extends T> lazyValue) {
        return asLazyProxy(interfaceTypeProvider.getRawClass(), lazyValue);
    }

    @SuppressWarnings({"unchecked", "java:S1172"})
    public static <E, T extends List<E>> T asLazyListProxy(
        @SuppressWarnings("unused") Class<E> elementType,
        LazyValue<? extends T> lazyValue
    ) {
        return (T) asLazyProxy(List.class, lazyValue);
    }

    @SuppressWarnings({"unchecked", "java:S1172"})
    public static <E, T extends Set<E>> T asLazySetProxy(
        @SuppressWarnings("unused") Class<E> elementType,
        LazyValue<? extends T> lazyValue
    ) {
        return (T) asLazyProxy(Set.class, lazyValue);
    }

    @SuppressWarnings({"unchecked", "java:S1172"})
    public static <K, V, T extends Map<K, V>> T asLazyMapProxy(
        @SuppressWarnings("unused") Class<K> keyType,
        @SuppressWarnings("unused") Class<K> valueType,
        LazyValue<? extends T> lazyValue
    ) {
        return (T) asLazyProxy(Map.class, lazyValue);
    }


    public static boolean isLazyProxy(Object object) {
        return object instanceof LazyProxyMarker;
    }


    private static final LoadingCache<Class<?>, Class<?>> PROXY_CLASSES = CacheBuilder.newBuilder()
        .weakKeys()
        .build(CacheLoader.from(LazyProxy::generateProxyClass));

    private static final Method LAZY_VALUE_GET_METHOD;
    private static final List<Method> OBJECT_METHODS_TO_IMPLEMENT;

    static {
        try {
            LAZY_VALUE_GET_METHOD = LazyValue.class.getMethod("get");
            OBJECT_METHODS_TO_IMPLEMENT = stream(Object.class.getMethods())
                .filter(ProxyUtils::isToStringMethod)
                .collect(toImmutableList());
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
            classNode.name = getInternalName(LazyProxy.class) + '$' + classNode.name.replace('/', '$');
        }
        classNode.superName = getInternalName(Object.class);
        classNode.interfaces = asList(
            getInternalName(interfaceClass),
            getInternalName(LazyProxyMarker.class)
        );
        classNode.fields = new ArrayList<>();
        classNode.methods = new ArrayList<>();

        val delegateField = new FieldNode(
            ACC_PRIVATE | ACC_FINAL,
            "delegate",
            getDescriptor(LazyValue.class),
            null,
            null
        );
        classNode.fields.add(delegateField);

        {
            val methodNode = new MethodNode(
                ACC_PUBLIC,
                "<init>",
                getMethodDescriptor(
                    VOID_TYPE,
                    getType(delegateField.desc)
                ),
                null,
                null
            );
            classNode.methods.add(methodNode);

            methodNode.parameters = singletonList(new ParameterNode(delegateField.name, ACC_FINAL));

            val instructions = methodNode.instructions = new InsnList();
            instructions.add(new LabelNode());

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new MethodInsnNode(
                INVOKESPECIAL,
                classNode.superName,
                "<init>",
                getMethodDescriptor(
                    VOID_TYPE
                )
            ));

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new VarInsnNode(ALOAD, 1));
            instructions.add(new FieldInsnNode(
                PUTFIELD,
                classNode.name,
                delegateField.name,
                delegateField.desc
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
                delegateField.name,
                delegateField.desc
            ));
            instructions.add(new MethodInsnNode(
                LAZY_VALUE_GET_METHOD.getDeclaringClass().isInterface()
                    ? INVOKEINTERFACE
                    : INVOKEVIRTUAL,
                getInternalName(LAZY_VALUE_GET_METHOD.getDeclaringClass()),
                LAZY_VALUE_GET_METHOD.getName(),
                getMethodDescriptor(LAZY_VALUE_GET_METHOD)
            ));

            for (int paramIndex = 0; paramIndex < method.getParameterCount(); ++paramIndex) {
                val paramClass = method.getParameterTypes()[paramIndex];
                instructions.add(new VarInsnNode(getType(paramClass).getOpcode(ILOAD), paramIndex + 1));
            }

            instructions.add(new MethodInsnNode(
                method.getDeclaringClass().isInterface()
                    ? INVOKEINTERFACE
                    : INVOKEVIRTUAL,
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


    private interface LazyProxyMarker {
    }

}
