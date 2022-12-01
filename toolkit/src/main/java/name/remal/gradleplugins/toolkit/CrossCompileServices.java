package name.remal.gradleplugins.toolkit;

import static java.lang.Math.toIntExact;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.CrossCompileVersionComparator.CrossCompileVersionComparisonResult.DEPENDENCY_EQUALS_TO_CURRENT;
import static name.remal.gradleplugins.toolkit.CrossCompileVersionComparator.CrossCompileVersionComparisonResult.DEPENDENCY_GREATER_THAN_CURRENT;
import static name.remal.gradleplugins.toolkit.CrossCompileVersionComparator.CrossCompileVersionComparisonResult.DEPENDENCY_LESS_THAN_CURRENT;
import static name.remal.gradleplugins.toolkit.CrossCompileVersionComparator.CrossCompileVersionComparisonResult.compareDependencyVersionToCurrentVersionObjects;
import static name.remal.gradleplugins.toolkit.PredicateUtils.not;
import static name.remal.gradleplugins.toolkit.ResourceUtils.readResource;
import static name.remal.gradleplugins.toolkit.UrlUtils.readStringFromUrl;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.makeAccessible;
import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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

    public static synchronized <T> T loadCrossCompileService(Class<T> service) {
        return loadCrossCompileService(service, null);
    }

    @SneakyThrows
    @SuppressWarnings("java:S3776")
    public static synchronized <T> T loadCrossCompileService(
        Class<T> service,
        @Nullable CrossCompileVersionComparator dependencyVersionComparator
    ) {
        val implClassNames = getImplClassNames(service);
        val impls = parseServiceImpls(service, implClassNames);
        val fallbackImpl = extractFallbackImpl(service, impls);
        impls.removeIf(impl -> impl.getDependencyVersion().isNothingIncluded());

        assertNoIntersections(service, impls);


        if (dependencyVersionComparator == null) {
            dependencyVersionComparator = DEFAULT_VERSION_COMPARATOR;
        } else {
            dependencyVersionComparator = dependencyVersionComparator.then(DEFAULT_VERSION_COMPARATOR);
        }

        CrossCompileServiceImpl impl = getImpl(impls, dependencyVersionComparator);

        if (impl == null) {
            impl = fallbackImpl;
        }


        try {
            val implClass = Class.forName(impl.getClassName(), true, service.getClassLoader());
            val implCtor = implClass.getDeclaredConstructor();
            makeAccessible(implCtor);
            val implInstance = implCtor.newInstance();
            return service.cast(implInstance);

        } catch (Throwable exception) {
            throw new CrossCompileServiceLoadingException(
                format(
                    "Error instantiating cross-compile implementation of %s: %s",
                    service.getName(),
                    impl
                ),
                exception
            );
        }
    }

    @SneakyThrows
    @SuppressWarnings("UnstableApiUsage")
    private static Set<String> getImplClassNames(Class<?> service) {
        Set<String> implClassNames = new LinkedHashSet<>();

        val resourceName = "META-INF/services/" + service.getName();
        val resourceUrls = service.getClassLoader().getResources(resourceName);
        while (resourceUrls.hasMoreElements()) {
            val resourceUrl = resourceUrls.nextElement();
            val content = readStringFromUrl(resourceUrl, UTF_8);
            Splitter.onPattern("[\\r\\n]+").splitToStream(content)
                .map(line -> {
                    val commentPos = line.indexOf('#');
                    return commentPos >= 0 ? line.substring(0, commentPos) : line;
                })
                .map(String::trim)
                .filter(not(String::isEmpty))
                .forEach(implClassNames::add);
        }

        return implClassNames;
    }

    @SneakyThrows
    @SuppressWarnings("java:S3776")
    private static List<CrossCompileServiceImpl> parseServiceImpls(
        Class<?> loadingClass,
        Collection<String> implClassNames
    ) {
        Map<String, CrossCompileServiceDependencyVersion> versionInfos = new LinkedHashMap<>();
        for (val implClassName : implClassNames) {
            final byte[] implClassBytecode;
            try {
                //noinspection InjectedReferences
                implClassBytecode = readResource(
                    implClassName.replace('.', '/') + ".class",
                    loadingClass.getClassLoader()
                );
            } catch (ResourceNotFoundException ignored) {
                logger.error("Cross-compile implementation class not found: {}", implClassName);
                continue;
            }

            val dependencyVersionInfoBuilder = CrossCompileServiceDependencyVersion.builder();
            val isProcessed = new AtomicBoolean();
            val asmApi = getAsmApi();
            val classVisitor = new ClassVisitor(asmApi) {
                @Nullable
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (descriptor.endsWith("/RemalGradlePluginsCrossCompilation;")
                        && isProcessed.compareAndSet(false, true)
                    ) {
                        return new AnnotationVisitor(asmApi) {
                            @Override
                            public void visit(String name, Object value) {
                                switch (name) {
                                    case "dependency":
                                        dependencyVersionInfoBuilder.dependency(value.toString());
                                        break;
                                    case "version":
                                        dependencyVersionInfoBuilder.version(Version.parse(value.toString()));
                                        break;
                                    case "versionOperator":
                                        val versionOperator = value.toString();
                                        switch (versionOperator) {
                                            case "lt":
                                                dependencyVersionInfoBuilder
                                                    .earlierIncluded(true)
                                                ;
                                                break;
                                            case "lte":
                                                dependencyVersionInfoBuilder
                                                    .earlierIncluded(true)
                                                    .selfIncluded(true)
                                                ;
                                                break;
                                            case "eq":
                                                dependencyVersionInfoBuilder
                                                    .selfIncluded(true)
                                                ;
                                                break;
                                            case "gte":
                                                dependencyVersionInfoBuilder
                                                    .selfIncluded(true)
                                                    .laterIncluded(true)
                                                ;
                                                break;
                                            case "gt":
                                                dependencyVersionInfoBuilder
                                                    .laterIncluded(true)
                                                ;
                                                break;
                                            default:
                                                logger.error(
                                                    "Unsupported cross-compile 'versionOperator' parameter for {}: {}",
                                                    implClassName,
                                                    versionOperator
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


        return versionInfos.entrySet().stream()
            .sorted(Entry.<String, CrossCompileServiceDependencyVersion>comparingByValue())
            .map(entry ->
                CrossCompileServiceImpl.builder()
                    .className(entry.getKey())
                    .dependencyVersion(entry.getValue())
                    .build()
            )
            .collect(toCollection(ArrayList::new));
    }

    @SneakyThrows
    private static int getAsmApi() {
        val field = ClassVisitor.class.getDeclaredField("api");
        makeAccessible(field);
        return field.getInt(new ClassNode());
    }

    private static CrossCompileServiceImpl extractFallbackImpl(
        Class<?> service,
        List<CrossCompileServiceImpl> impls
    ) {
        val fallbackImpls = impls.stream()
            .filter(impl -> impl.getDependencyVersion().getVersion() == null)
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
                fallbackImpls.stream()
                    .map(Object::toString)
                    .collect(joining(", "))
            ));
        }

        val fallbackImpl = fallbackImpls.get(0);
        impls.remove(fallbackImpl);
        return fallbackImpl;
    }

    private static void assertNoIntersections(
        Class<?> service,
        List<CrossCompileServiceImpl> impls
    ) {
        for (int i = 0; i < impls.size() - 1; ++i) {
            for (int g = i + 1; g < impls.size(); ++g) {
                val thisImpl = impls.get(i);
                val thatImpl = impls.get(g);
                if (thisImpl.getDependencyVersion().intersectsWith(thatImpl.getDependencyVersion())) {
                    throw new CrossCompileServiceLoadingException(format(
                        "Cross-compile implementation versions intersect for %s: %s, %s",
                        service,
                        thisImpl,
                        thatImpl
                    ));
                }
            }
        }
    }

    @Nullable
    private static CrossCompileServiceImpl getImpl(
        List<CrossCompileServiceImpl> impls,
        CrossCompileVersionComparator dependencyVersionComparator
    ) {
        {
            val onlySelfIncludedImpls = impls.stream()
                .filter(impl -> impl.getDependencyVersion().isOnlySelfIncluded())
                .collect(toList());
            for (val impl : onlySelfIncludedImpls) {
                val isActive = isActive(impl, dependencyVersionComparator);
                if (isActive) {
                    return impl;
                }
            }
        }

        {
            val earlierIncludedImpls = impls.stream()
                .filter(impl -> impl.getDependencyVersion().isEarlierIncluded())
                .sorted(comparing(CrossCompileServiceImpl::getDependencyVersion))
                .collect(toList());
            for (val impl : earlierIncludedImpls) {
                val isActive = isActive(impl, dependencyVersionComparator);
                if (isActive) {
                    return impl;
                }
            }
        }

        {
            val laterIncludedImpls = impls.stream()
                .filter(impl -> impl.getDependencyVersion().isLaterIncluded())
                .filter(not(impl -> impl.getDependencyVersion().isEarlierIncluded()))
                .sorted(comparing(CrossCompileServiceImpl::getDependencyVersion)
                    .reversed()
                )
                .collect(toList());
            for (val impl : laterIncludedImpls) {
                val isActive = isActive(impl, dependencyVersionComparator);
                if (isActive) {
                    return impl;
                }
            }
        }

        return null;
    }

    @SneakyThrows
    @VisibleForTesting
    static boolean isActive(
        CrossCompileServiceImpl impl,
        CrossCompileVersionComparator dependencyVersionComparator
    ) {
        val className = impl.getClassName();
        val dependencyVersion = impl.getDependencyVersion();
        val dependency = dependencyVersion.getDependency();
        val version = requireNonNull(dependencyVersion.getVersion()).withoutSuffix();
        val earlierIncluded = dependencyVersion.isEarlierIncluded();
        val selfIncluded = dependencyVersion.isSelfIncluded();
        val laterIncluded = dependencyVersion.isLaterIncluded();

        val comparisonResult = dependencyVersionComparator.compareDependencyVersionToCurrentVersion(
            dependency,
            version.toString()
        );

        if (comparisonResult == null) {
            logger.error("Unsupported cross-compile dependency for {}: {}", className, dependencyVersion);
            return false;

        } else if (comparisonResult == DEPENDENCY_GREATER_THAN_CURRENT) {
            // the current version is less than the dependency version
            return earlierIncluded;

        } else if (comparisonResult == DEPENDENCY_EQUALS_TO_CURRENT) {
            // the current version equals to the dependency version
            return selfIncluded;

        } else if (comparisonResult == DEPENDENCY_LESS_THAN_CURRENT) {
            // the current version is greater than the dependency version
            return laterIncluded;

        } else {
            throw new UnsupportedOperationException("Unsupported comparison result: " + comparisonResult);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static final CrossCompileVersionComparator DEFAULT_VERSION_COMPARATOR =
        (dependency, dependencyVersionString) -> {
            val dependencyVersion = Version.parse(dependencyVersionString);
            if (dependency.equals("java")) {
                val majorVersion = toIntExact(dependencyVersion.getNumber(0));
                val currentMajorVersion = Integer.parseInt(JavaVersion.current().getMajorVersion());
                return compareDependencyVersionToCurrentVersionObjects(majorVersion, currentMajorVersion);

            } else if (dependency.equals("gradle")) {
                val currentVersionString = GradleVersion.current().getBaseVersion().getVersion();
                val currentVersionStringNormalized = Splitter.on('.').splitToStream(currentVersionString)
                    .limit(dependencyVersion.getNumbersCount())
                    .collect(joining("."));
                val currentVersion = Version.parse(currentVersionStringNormalized);
                return compareDependencyVersionToCurrentVersionObjects(dependencyVersion, currentVersion);

            } else {
                return null;
            }
        };

}
