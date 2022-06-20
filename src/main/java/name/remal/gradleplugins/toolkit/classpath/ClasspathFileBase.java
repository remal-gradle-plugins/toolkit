package name.remal.gradleplugins.toolkit.classpath;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static name.remal.gradleplugins.toolkit.PredicateUtils.containsString;
import static name.remal.gradleplugins.toolkit.PredicateUtils.not;
import static name.remal.gradleplugins.toolkit.PredicateUtils.startsWithString;
import static name.remal.gradleplugins.toolkit.classpath.Utils.toDeepImmutableCollectionMap;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.makeAccessible;
import static org.objectweb.asm.ClassReader.SKIP_CODE;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradleplugins.toolkit.LazyInitializer;
import name.remal.gradleplugins.toolkit.ObjectUtils;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "file")
abstract class ClasspathFileBase implements ClasspathFileMethods {

    public static ClasspathFileMethods of(File file, int jvmMajorCompatibilityVersion) {
        file = normalizeFile(file);
        if (file.isDirectory()) {
            return new ClasspathFileDir(file, jvmMajorCompatibilityVersion);
        } else if (file.isFile()) {
            return new ClasspathFileJar(file, jvmMajorCompatibilityVersion);
        } else {
            return new ClasspathFileNotExist(file, jvmMajorCompatibilityVersion);
        }
    }


    protected final File file;
    protected final int jvmMajorCompatibilityVersion;


    //#region getResourceNames()

    private final LazyInitializer<Collection<String>> resourceNames = new LazyInitializer<Collection<String>>() {
        @Override
        protected Collection<String> create() {
            return ImmutableList.copyOf(getResourceNamesImpl());
        }
    };

    @Override
    @Unmodifiable
    public final Collection<String> getResourceNames() {
        return resourceNames.get();
    }

    protected abstract Collection<String> getResourceNamesImpl();

    //#endregion


    //#region openStream()

    @Override
    @Nullable
    @MustBeClosed
    public final InputStream openStream(@Language("file-reference") String resourceName) {
        while (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }

        if (resourceName.isEmpty()) {
            return null;
        }

        return openStreamImpl(resourceName);
    }

    @Nullable
    protected abstract InputStream openStreamImpl(String resourceName);

    //#endregion


    //#region forEachResource()

    @Override
    @SneakyThrows
    public void forEachResource(ResourceProcessor processor) {
        for (String resourceName : getResourceNames()) {
            ResourceInputStreamOpenerImpl inputStreamSupplier = new ResourceInputStreamOpenerImpl() {
                @Override
                @SuppressWarnings("MustBeClosedChecker")
                protected InputStream openStreamImpl() {
                    return requireNonNull(ClasspathFileBase.this.openStream(resourceName));
                }
            };
            try {
                processor.process(resourceName, inputStreamSupplier);

            } finally {
                inputStreamSupplier.disable();
            }
        }
    }

    //#endregion


    //#region getClassesIndex()


    private final LazyInitializer<ClassesIndex> classesIndex = new LazyInitializer<ClassesIndex>() {
        @Override
        protected ClassesIndex create() {
            Map<String, Set<String>> parentClasses = new LinkedHashMap<>();

            forEachClassResource((className, inputStreamOpener) -> {
                try (val inputStream = inputStreamOpener.openStream()) {
                    val classVisitor = new ClassVisitor(getAsmApi()) {
                        @Override
                        public void visit(
                            int version,
                            int access,
                            String name,
                            String signature,
                            @Nullable String superInternalName,
                            @Nullable String[] interfaceInternalNames
                        ) {
                            if (superInternalName != null) {
                                val superName = superInternalName.replace('/', '.');
                                parentClasses.computeIfAbsent(className, __ -> new LinkedHashSet<>())
                                    .add(superName);
                            }

                            if (interfaceInternalNames != null) {
                                stream(interfaceInternalNames)
                                    .filter(Objects::nonNull)
                                    .map(interfaceInternalName -> interfaceInternalName.replace('/', '.'))
                                    .forEach(interfaceInternalName ->
                                        parentClasses.computeIfAbsent(className, __ -> new LinkedHashSet<>())
                                            .add(interfaceInternalName)
                                    );
                            }

                            super.visit(version, access, name, signature, superInternalName, interfaceInternalNames);
                        }
                    };
                    val classReader = new ClassReader(inputStream);
                    classReader.accept(classVisitor, SKIP_CODE);
                }
            });

            return new ClassesIndex(
                parentClasses
            );
        }
    };

    @Override
    public final ClassesIndex getClassesIndex() {
        return classesIndex.get();
    }

    //#endregion


    //#region getAllServices()

    private final LazyInitializer<Map<String, Collection<String>>> allServices =
        new LazyInitializer<Map<String, Collection<String>>>() {
            @SuppressWarnings("InjectedReferences")
            private static final String SERVICES_PREFIX = "META-INF/services/";

            @Override
            @SneakyThrows
            @SuppressWarnings("UnstableApiUsage")
            protected Map<String, Collection<String>> create() {
                List<String> serviceNames = getResourceNames().stream()
                    .filter(startsWithString(SERVICES_PREFIX))
                    .map(resourceName -> resourceName.substring(SERVICES_PREFIX.length()))
                    .filter(not(containsString("/")))
                    .collect(toList());

                Map<String, Collection<String>> allServices = new LinkedHashMap<>();
                for (String serviceName : serviceNames) {
                    final String content;
                    try (val inputStream = openStream(SERVICES_PREFIX + serviceName)) {
                        if (inputStream == null) {
                            continue;
                        }

                        byte[] bytes = toByteArray(inputStream);
                        content = new String(bytes, UTF_8);
                    }

                    Splitter.on(CharMatcher.anyOf("\r\n")).splitToStream(content)
                        .map(line -> {
                            val commentPos = line.indexOf('#');
                            if (commentPos >= 0) {
                                return line.substring(0, commentPos);
                            } else {
                                return line;
                            }
                        })
                        .map(String::trim)
                        .filter(ObjectUtils::isNotEmpty)
                        .forEach(implName -> {
                            val implNames = allServices.computeIfAbsent(serviceName, __ -> new LinkedHashSet<>());
                            implNames.add(implName);
                        });
                }

                return toDeepImmutableCollectionMap(allServices);
            }
        };

    @Override
    @Unmodifiable
    public final Map<String, Collection<String>> getAllServices() {
        return allServices.get();
    }

    //#endregion


    //#region getAllServices()

    private final LazyInitializer<Map<String, Collection<String>>> allSpringFactories =
        new LazyInitializer<Map<String, Collection<String>>>() {
            @Override
            @SuppressWarnings({"UnstableApiUsage", "InjectedReferences"})
            protected Map<String, Collection<String>> create() throws Throwable {
                try (InputStream inputStream = openStream("META-INF/spring.factories")) {
                    if (inputStream == null) {
                        return emptyMap();
                    }

                    Properties properties = new Properties();
                    properties.load(inputStream);

                    Map<String, Collection<String>> allFactories = new LinkedHashMap<>();
                    properties.stringPropertyNames().forEach(factoryName -> {
                        String implNamesString = properties.getProperty(factoryName, "");
                        Splitter.on(',').splitToStream(implNamesString)
                            .map(String::trim)
                            .filter(ObjectUtils::isNotEmpty)
                            .forEach(implName -> {
                                Collection<String> implNames = allFactories.computeIfAbsent(
                                    factoryName,
                                    __ -> new LinkedHashSet<>()
                                );
                                implNames.add(implName);
                            });
                    });
                    return toDeepImmutableCollectionMap(allFactories);
                }
            }
        };

    @Override
    @Unmodifiable
    public final Map<String, Collection<String>> getAllSpringFactories() {
        return allSpringFactories.get();
    }

    //#endregion


    //#region Utilities

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + '[' + file + ']';
    }

    protected static File normalizeFile(File file) {
        return file
            .toPath()
            .toAbsolutePath()
            .normalize()
            .toFile();
    }

    protected static String normalizePath(String path) {
        if (File.separatorChar != '/') {
            return path.replace(File.separatorChar, '/');
        } else {
            return path;
        }
    }

    @SneakyThrows
    protected static int getAsmApi() {
        val classWriter = new ClassWriter(0);
        val apiField = makeAccessible(ClassVisitor.class.getDeclaredField("api"));
        return apiField.getInt(classWriter);
    }

    @RequiredArgsConstructor
    protected abstract static class InputStreamCloseWrapper extends InputStream {

        protected boolean shouldCloseDelegate() {
            return true;
        }

        protected void additionalClose() throws Throwable {
            // do nothing by default
        }


        private final InputStream delegate;

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public synchronized void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }

        @Override
        @SneakyThrows
        public void close() {
            if (shouldCloseDelegate()) {
                delegate.close();
            }
            additionalClose();
        }

    }

    protected abstract static class ResourceInputStreamOpenerImpl implements ResourceInputStreamOpener {

        protected abstract InputStream openStreamImpl();


        private enum Status {
            NOT_OPENED,
            OPENED,
            DISABLED,
        }

        private Status status = Status.NOT_OPENED;

        @Override
        @MustBeClosed
        public final synchronized InputStream openStream() {
            if (status == Status.NOT_OPENED) {
                status = Status.OPENED;
                return openStreamImpl();

            } else if (status == Status.OPENED) {
                throw new IllegalStateException("InputStream has already been opened for this resource");

            } else if (status == Status.DISABLED) {
                throw new IllegalStateException("InputStream can't be opened here");

            } else {
                throw new UnsupportedOperationException("Unsupported status: " + status);
            }
        }

        public final synchronized void disable() {
            status = Status.DISABLED;
        }

    }

    //#endregion

}
