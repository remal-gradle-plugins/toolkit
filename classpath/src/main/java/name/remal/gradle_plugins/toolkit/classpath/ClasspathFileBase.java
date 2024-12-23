package name.remal.gradle_plugins.toolkit.classpath;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.LazyValue.lazyValue;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.containsString;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.startsWithString;
import static name.remal.gradle_plugins.toolkit.cache.ToolkitCaches.newFileToolkitCache;
import static name.remal.gradle_plugins.toolkit.classpath.Utils.toDeepImmutableSetMap;
import static name.remal.gradle_plugins.toolkit.classpath.Utils.toImmutableSet;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import name.remal.gradle_plugins.toolkit.LazyValue;
import name.remal.gradle_plugins.toolkit.ObjectUtils;
import name.remal.gradle_plugins.toolkit.cache.ToolkitCache;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ClassReader;

@RequiredArgsConstructor
@EqualsAndHashCode(of = {"file", "jvmMajorCompatibilityVersion"})
abstract class ClasspathFileBase implements ClasspathFileMethods {

    public static ClasspathFileBase of(File file, int jvmMajorCompatibilityVersion) {
        file = normalizeFile(file);
        if (file.isDirectory()) {
            return new ClasspathFileDir(file, jvmMajorCompatibilityVersion);
        } else if (file.isFile()) {
            return newCachedClasspathFileJar(file, jvmMajorCompatibilityVersion);
        } else {
            return new ClasspathFileNotExist(file, jvmMajorCompatibilityVersion);
        }
    }

    //#region newCachedClasspathFileJar()

    private static synchronized ClasspathFileJar newCachedClasspathFileJar(
        File file,
        int jvmMajorCompatibilityVersion
    ) {
        val cache = getClasspathFileJarCache(jvmMajorCompatibilityVersion);
        return cache.get(file);
    }

    private static final ConcurrentMap<Integer, ToolkitCache<File, ClasspathFileJar>> CLASSPATH_FILE_JAR_CACHES =
        new ConcurrentHashMap<>();

    private static ToolkitCache<File, ClasspathFileJar> getClasspathFileJarCache(int jvmMajorCompatibilityVersion) {
        val cache = CLASSPATH_FILE_JAR_CACHES.computeIfAbsent(
            jvmMajorCompatibilityVersion,
            version -> newFileToolkitCache(file -> {
                return new ClasspathFileJar(file, version);
            })
        );
        cache.cleanup();
        return cache;
    }

    //#endregion


    protected final File file;
    protected final int jvmMajorCompatibilityVersion;

    File getFile() {
        return file;
    }


    //#region getResourceNames()

    @Override
    @Unmodifiable
    public final Set<String> getResourceNames() {
        return resourceNames.get();
    }

    private final LazyValue<Set<String>> resourceNames = lazyValue(() ->
        toImmutableSet(new TreeSet<>(getResourceNamesImpl()))
    );

    protected abstract Set<String> getResourceNamesImpl();

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
            ResourceInputStreamOpenerImpl inputStreamSupplier = new ResourceInputStreamOpenerImpl(file, resourceName) {
                @Override
                @SuppressWarnings("MustBeClosedChecker")
                protected InputStream openStreamImpl() {
                    return requireNonNull(ClasspathFileBase.this.openStream(resourceName));
                }
            };
            try {
                processor.process(file, resourceName, inputStreamSupplier);

            } finally {
                inputStreamSupplier.disable();
            }
        }
    }

    @Override
    public void forEachResource(String resourceName, ResourceProcessor processor) {
        while (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }

        val normalizedResourceName = resourceName;
        forEachResource((classpathFile, currentResourceName, inputStreamOpener) -> {
            if (currentResourceName.equals(normalizedResourceName)) {
                processor.process(classpathFile, currentResourceName, inputStreamOpener);
            }
        });
    }

    //#endregion


    //#region getClassesIndex()


    @Override
    public final ClassesIndex getClassesIndex() {
        return classesIndex.get();
    }

    private final LazyValue<ClassesIndex> classesIndex = lazyValue(() -> {
        ClassesIndex classesIndex = new ClassesIndex();

        forEachClassResource((classpathFile, className, inputStreamOpener) -> {
            if (className.equals("module-info")
                || className.equals("package-info")
                || className.endsWith(".package-info")
            ) {
                return;
            }

            try (InputStream inputStream = inputStreamOpener.openStream()) {
                ClassReader classReader = new ClassReader(inputStream);

                String superInternalName = classReader.getSuperName();
                if (superInternalName != null) {
                    String superName = superInternalName.replace('/', '.');
                    classesIndex.registerParentClass(className, superName);
                }

                String[] interfaceInternalNames = classReader.getInterfaces();
                if (interfaceInternalNames != null) {
                    List<String> interfaceNames = new ArrayList<>();
                    for (String interfaceInternalName : interfaceInternalNames) {
                        String interfaceName = interfaceInternalName.replace('/', '.');
                        interfaceNames.add(interfaceName);
                    }
                    classesIndex.registerParentClasses(className, interfaceNames);
                }
            }
        });

        return classesIndex;
    });

    //#endregion


    //#region getAllServices()

    @Override
    @Unmodifiable
    public final Map<String, Set<String>> getAllServices() {
        return allServices.get();
    }

    @SuppressWarnings("InjectedReferences")
    private static final String SERVICES_PREFIX = "META-INF/services/";

    private final LazyValue<Map<String, Set<String>>> allServices = lazyValue(() -> {
        List<String> serviceNames = getResourceNames().stream()
            .filter(startsWithString(SERVICES_PREFIX))
            .map(resourceName -> resourceName.substring(SERVICES_PREFIX.length()))
            .filter(not(containsString("/")))
            .collect(toList());

        Map<String, Set<String>> allServices = new LinkedHashMap<>();
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

        return toDeepImmutableSetMap(allServices);
    });

    //#endregion


    //#region getAllServices()

    @Override
    @Unmodifiable
    public final Map<String, Set<String>> getAllSpringFactories() {
        return allSpringFactories.get();
    }

    @SuppressWarnings("InjectedReferences")
    private final LazyValue<Map<String, Set<String>>> allSpringFactories = lazyValue(() -> {
        final Properties properties;
        try (val inputStream = openStream("META-INF/spring.factories")) {
            if (inputStream == null) {
                return emptyMap();
            }

            properties = new Properties();
            try (val reader = new InputStreamReader(inputStream, UTF_8)) {
                properties.load(reader);
            }
        }

        Map<String, Set<String>> allFactories = new LinkedHashMap<>();
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
        return toDeepImmutableSetMap(allFactories);
    });

    //#endregion


    //#region Utilities

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + '[' + file + ']';
    }

    protected static String normalizePathSeparator(String path) {
        if (File.separatorChar != '/') {
            return path.replace(File.separatorChar, '/');
        } else {
            return path;
        }
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

    @RequiredArgsConstructor
    @ToString
    protected abstract static class ResourceInputStreamOpenerImpl implements ResourceInputStreamOpener {

        protected abstract InputStream openStreamImpl();


        private final File file;
        private final String resourceName;

        private Status status = Status.NOT_OPENED;

        private enum Status {
            NOT_OPENED,
            OPENED,
            DISABLED,
        }

        @Override
        @MustBeClosed
        public final synchronized InputStream openStream() {
            if (status == Status.NOT_OPENED) {
                status = Status.OPENED;
                return openStreamImpl();

            } else if (status == Status.OPENED) {
                throw new IllegalStateException(format(
                    "%s can't open InputStream multiple times",
                    ResourceInputStreamOpener.class.getSimpleName()
                ));

            } else if (status == Status.DISABLED) {
                throw new IllegalStateException(format(
                    "%s can't be used here",
                    ResourceInputStreamOpener.class.getSimpleName()
                ));

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
