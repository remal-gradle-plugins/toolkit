package name.remal.gradleplugins.toolkit;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.lang.Math.toIntExact;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.PredicateUtils.not;
import static name.remal.gradleplugins.toolkit.UrlUtils.openInputStreamForUrl;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.makeAccessible;
import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;

import com.google.common.base.Splitter;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.JavaVersion;
import org.gradle.util.GradleVersion;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

@NoArgsConstructor(access = PRIVATE)
@CustomLog
public abstract class CrossCompileServices {

    @SneakyThrows
    @SuppressWarnings("java:S3776")
    public static synchronized <T> T loadCrossCompileService(Class<T> service) {
        val implClassNames = getImplClassNames(service);
        val dependencyVersions = parseDependencyVersions(service, implClassNames);

        final String fallbackImplClassName;
        val fallbackImpls = dependencyVersions.entrySet().stream()
            .filter(entry -> entry.getValue().getVersion() == null)
            .map(Entry::getKey)
            .collect(toList());
        if (fallbackImpls.isEmpty()) {
            throw new CrossCompileServiceLoadingException(format(
                "Cross-compile fallback implementation not found for %s",
                service
            ));
        } else if (fallbackImpls.size() >= 2) {
            throw new CrossCompileServiceLoadingException(format(
                "Multiple cross-compile fallback implementations found for %s: %s",
                service,
                join(", ", fallbackImpls)
            ));
        } else {
            fallbackImplClassName = fallbackImpls.get(0);
            dependencyVersions.remove(fallbackImplClassName);
        }

        if (dependencyVersions.size() >= 2) {
            val dependencyVersionEntries = new ArrayList<>(dependencyVersions.entrySet());
            for (int i = 0; i < dependencyVersionEntries.size() - 1; ++i) {
                for (int g = i + 1; g < dependencyVersionEntries.size(); ++g) {
                    val thisEntry = dependencyVersionEntries.get(i);
                    val thatEntry = dependencyVersionEntries.get(g);
                    if (thisEntry.getValue().intersectsWith(thatEntry.getValue())) {
                        throw new CrossCompileServiceLoadingException(format(
                            "Cross-compile implementation versions intersect for %s: %s, %s",
                            service,
                            thisEntry.getKey(),
                            thatEntry.getKey()
                        ));
                    }
                }
            }
        }

        String implClassName = fallbackImplClassName;
        for (val dependencyVersionEntry : dependencyVersions.entrySet()) {
            if (isActive(dependencyVersionEntry.getKey(), dependencyVersionEntry.getValue())) {
                implClassName = dependencyVersionEntry.getKey();
                break;
            }
        }

        val implClass = Class.forName(implClassName, true, service.getClassLoader());
        val implCtor = implClass.getDeclaredConstructor();
        makeAccessible(implCtor);
        val impl = implCtor.newInstance();
        return service.cast(impl);
    }


    @SuppressWarnings("UnstableApiUsage")
    private static boolean isActive(String className, CrossCompileServiceDependencyVersion dependencyVersion) {
        val dependency = requireNonNull(dependencyVersion.getDependency());
        val version = requireNonNull(dependencyVersion.getVersion()).withoutSuffix();
        val earlierIncluded = dependencyVersion.isEarlierIncluded();
        val selfIncluded = dependencyVersion.isSelfIncluded();
        val laterIncluded = dependencyVersion.isLaterIncluded();
        final int comparisonResult;
        if (dependency.equals("java")) {
            val requiredMajorVersion = toIntExact(version.getNumber(0));
            val currentMajorVersion = Integer.parseInt(JavaVersion.current().getMajorVersion());
            comparisonResult = Integer.compare(requiredMajorVersion, currentMajorVersion);

        } else if (dependency.equals("gradle")) {
            val currentVersionString = GradleVersion.current().getBaseVersion().getVersion();
            val currentVersionStringNormalized = Splitter.on('.').splitToStream(currentVersionString)
                .limit(version.getNumbersCount())
                .collect(joining("."));
            val currentVersion = Version.parse(currentVersionStringNormalized);
            comparisonResult = version.compareTo(currentVersion);

        } else {
            logger.error("Unsupported cross-compile dependency for {}: {}", className, dependencyVersion);
            return false;
        }

        if (earlierIncluded && comparisonResult > 0) {
            return true;
        } else if (selfIncluded && comparisonResult == 0) {
            return true;
        } else if (laterIncluded && comparisonResult < 0) {
            return true;
        } else if (!selfIncluded && comparisonResult != 0) {
            return true;
        } else {
            return false;
        }
    }


    @SneakyThrows
    @SuppressWarnings("java:S3776")
    private static Map<String, CrossCompileServiceDependencyVersion> parseDependencyVersions(
        Class<?> loadingClass,
        Collection<String> implClassNames
    ) {
        Map<String, CrossCompileServiceDependencyVersion> versionInfos = new LinkedHashMap<>();
        for (val implClassName : implClassNames) {
            final byte[] implClassBytecode;
            try (val inputStream = openResource(loadingClass, implClassName.replace('.', '/') + ".class")) {
                implClassBytecode = toByteArray(inputStream);
            } catch (ResourceNotFoundException e) {
                logger.error("Cross-compile implementation not found: {}", implClassName);
                continue;
            }

            val dependencyVersionInfoBuilder = CrossCompileServiceDependencyVersion.builder();
            val isProcessed = new AtomicBoolean();
            val classVisitor = new ClassVisitor(ASM_API) {
                @Nullable
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (descriptor.endsWith("/RemalGradlePluginsCrossCompilation;")
                        && isProcessed.compareAndSet(false, true)
                    ) {
                        return new AnnotationVisitor(ASM_API) {
                            @Override
                            public void visit(String name, Object value) {
                                switch (name) {
                                    case "dependency":
                                        dependencyVersionInfoBuilder.dependency(value.toString());
                                        break;
                                    case "version":
                                        dependencyVersionInfoBuilder.version(Version.parse(value.toString()));
                                        break;
                                    case "versionExtender":
                                        val versionExtender = value.toString();
                                        switch (versionExtender) {
                                            case "lt":
                                                dependencyVersionInfoBuilder.earlierIncluded(true);
                                                break;
                                            case "lte":
                                                dependencyVersionInfoBuilder.earlierIncluded(true).selfIncluded(true);
                                                break;
                                            case "eq":
                                                dependencyVersionInfoBuilder.selfIncluded(true);
                                                break;
                                            case "ne":
                                                // do nothing
                                                break;
                                            case "gte":
                                                dependencyVersionInfoBuilder.selfIncluded(true).laterIncluded(true);
                                                break;
                                            case "gt":
                                                dependencyVersionInfoBuilder.laterIncluded(true);
                                                break;
                                            default:
                                                logger.error(
                                                    "Unsupported cross-compile 'versionExtender' parameter for {}: {}",
                                                    implClassName,
                                                    versionExtender
                                                );
                                        }
                                        break;
                                    default:
                                        logger.error(
                                            "Unsupported cross-compile '{}' parameter for {}",
                                            name,
                                            implClassName
                                        );
                                }
                            }
                        };
                    }
                    return null;
                }
            };
            new ClassReader(implClassBytecode).accept(classVisitor, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);


            CrossCompileServiceDependencyVersion dependencyVersionInfo = dependencyVersionInfoBuilder.build();

            val version = dependencyVersionInfo.getVersion();
            if (version != null) {
                dependencyVersionInfo = dependencyVersionInfo.withVersion(version.withoutSuffix());

                val dependency = dependencyVersionInfo.getDependency();
                if (dependency.equals("java")) {
                    dependencyVersionInfo = dependencyVersionInfo.withMaxVersionNumbersCount(1);
                } else if (dependency.equals("gradle")) {
                    dependencyVersionInfo = dependencyVersionInfo.withMaxVersionNumbersCount(3);
                }
            }

            versionInfos.put(implClassName, dependencyVersionInfo);
        }


        Map<String, CrossCompileServiceDependencyVersion> sortedVersionInfos = new LinkedHashMap<>();
        versionInfos.entrySet().stream()
            .sorted(Entry.<String, CrossCompileServiceDependencyVersion>comparingByValue().reversed())
            .forEach(entry -> sortedVersionInfos.put(entry.getKey(), entry.getValue()));
        return sortedVersionInfos;
    }


    @SneakyThrows
    @SuppressWarnings("UnstableApiUsage")
    private static Set<String> getImplClassNames(Class<?> service) {
        Set<String> implClassNames = new LinkedHashSet<>();

        val resourceName = "META-INF/services/" + service.getName();
        val resourceUrls = service.getClassLoader().getResources(resourceName);
        while (resourceUrls.hasMoreElements()) {
            val resourceUrl = resourceUrls.nextElement();
            try (val inputStream = openInputStreamForUrl(resourceUrl)) {
                val contentBytes = toByteArray(inputStream);
                val content = new String(contentBytes, UTF_8);
                Splitter.onPattern("[\\r\\n]+").splitToStream(content)
                    .map(line -> {
                        val commentPos = line.indexOf('#');
                        return commentPos >= 0 ? line.substring(0, commentPos) : line;
                    })
                    .map(String::trim)
                    .filter(not(String::isEmpty))
                    .forEach(implClassNames::add);
            }
        }

        return implClassNames;
    }


    @MustBeClosed
    @SneakyThrows
    private static InputStream openResource(Class<?> loadingClass, String resourceName) {
        val resourceUrl = loadingClass.getClassLoader().getResource(resourceName);
        if (resourceUrl == null) {
            throw new ResourceNotFoundException(
                loadingClass,
                resourceName
            );
        }
        return openInputStreamForUrl(resourceUrl);
    }


    private static final int ASM_API = getAsmApi();

    @SneakyThrows
    private static int getAsmApi() {
        val field = ClassVisitor.class.getDeclaredField("api");
        return makeAccessible(field).getInt(new ClassNode());
    }

}
