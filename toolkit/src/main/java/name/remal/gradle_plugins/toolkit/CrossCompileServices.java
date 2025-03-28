package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileVersionComparator.standardVersionCrossCompileVersionComparator;
import static name.remal.gradle_plugins.toolkit.CrossCompileVersionComparisonResult.DEPENDENCY_EQUALS_TO_CURRENT;
import static name.remal.gradle_plugins.toolkit.CrossCompileVersionComparisonResult.DEPENDENCY_GREATER_THAN_CURRENT;
import static name.remal.gradle_plugins.toolkit.CrossCompileVersionComparisonResult.DEPENDENCY_LESS_THAN_CURRENT;
import static name.remal.gradle_plugins.toolkit.FunctionUtils.toSubstringedBefore;
import static name.remal.gradle_plugins.toolkit.ResourceUtils.readResource;
import static name.remal.gradle_plugins.toolkit.UrlUtils.readStringFromUrl;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.makeAccessible;
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
import org.gradle.api.JavaVersion;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.Unmodifiable;
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
        var implClassNames = getImplClassNames(service);
        var impls = parseServiceImpls(service, implClassNames);
        var fallbackImpl = extractFallbackImpl(service, impls);
        impls.removeIf(impl -> impl.getDependencyVersion().isNothingIncluded());

        assertNoIntersections(service, impls);


        var dependencyVersionComparatorWithDefault = withDefaultCrossCompileVersionComparator(
            dependencyVersionComparator
        );

        CrossCompileServiceImpl impl = getImpl(impls, dependencyVersionComparatorWithDefault);

        if (impl == null) {
            impl = fallbackImpl;
        }


        try {
            var implClass = Class.forName(impl.getClassName(), true, service.getClassLoader());
            var implCtor = implClass.getDeclaredConstructor();
            makeAccessible(implCtor);
            var implInstance = implCtor.newInstance();
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


    @Unmodifiable
    public static synchronized <T> List<T> loadAllCrossCompileServiceImplementations(Class<T> service) {
        return loadAllCrossCompileServiceImplementations(service, null);
    }

    @Unmodifiable
    public static synchronized <T> List<T> loadAllCrossCompileServiceImplementations(
        Class<T> service,
        @Nullable CrossCompileVersionComparator dependencyVersionComparator
    ) {
        var implClassNames = getImplClassNames(service);
        var impls = parseServiceImpls(service, implClassNames);

        var dependencyVersionComparatorWithDefault = withDefaultCrossCompileVersionComparator(
            dependencyVersionComparator
        );

        var activeImpls = impls.stream()
            .filter(impl -> impl.isFallback() || isActive(impl, dependencyVersionComparatorWithDefault))
            .collect(toUnmodifiableList());

        List<T> instances = new ArrayList<>(impls.size());
        for (var impl : activeImpls) {
            try {
                var implClass = Class.forName(impl.getClassName(), true, service.getClassLoader());
                var implCtor = implClass.getDeclaredConstructor();
                makeAccessible(implCtor);
                var implInstance = implCtor.newInstance();
                var instance = service.cast(implInstance);
                instances.add(instance);

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
        return List.copyOf(instances);
    }


    private static CrossCompileVersionComparator withDefaultCrossCompileVersionComparator(
        @Nullable CrossCompileVersionComparator dependencyVersionComparator
    ) {
        if (dependencyVersionComparator == null) {
            return DEFAULT_VERSION_COMPARATOR;
        } else {
            return dependencyVersionComparator.then(DEFAULT_VERSION_COMPARATOR);
        }
    }


    @SneakyThrows
    @SuppressWarnings("UnstableApiUsage")
    private static Set<String> getImplClassNames(Class<?> service) {
        Set<String> implClassNames = new LinkedHashSet<>();

        var resourceName = "META-INF/services/" + service.getName();
        var resourceUrls = requireNonNull(service.getClassLoader()).getResources(resourceName);
        while (resourceUrls.hasMoreElements()) {
            var resourceUrl = resourceUrls.nextElement();
            var content = readStringFromUrl(resourceUrl, UTF_8);
            Splitter.onPattern("[\\r\\n]+").splitToStream(content)
                .map(toSubstringedBefore("#"))
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
        for (var implClassName : implClassNames) {
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

            var dependencyVersionInfoBuilder = CrossCompileServiceDependencyVersion.builder();
            var isProcessed = new AtomicBoolean();
            var asmApi = getAsmApi();
            var classVisitor = new ClassVisitor(asmApi) {
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
                                        var versionOperator = value.toString();
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

            var version = dependencyVersionInfo.getVersion();
            if (version != null) {
                dependencyVersionInfo = dependencyVersionInfo.withVersion(version.withoutSuffix());

                var dependency = dependencyVersionInfo.getDependency();
                if (dependency.equals("java")) {
                    dependencyVersionInfo = dependencyVersionInfo.withMaxVersionNumbersCount(1);
                } else if (dependency.equals("gradle")) {
                    dependencyVersionInfo = dependencyVersionInfo.withMaxVersionNumbersCount(3);
                }
            }

            versionInfos.put(implClassName, dependencyVersionInfo);
        }


        return versionInfos.entrySet().stream()
            .sorted(Entry.comparingByValue())
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
        var field = ClassVisitor.class.getDeclaredField("api");
        makeAccessible(field);
        return field.getInt(new ClassNode());
    }

    private static CrossCompileServiceImpl extractFallbackImpl(
        Class<?> service,
        List<CrossCompileServiceImpl> impls
    ) {
        var fallbackImpls = impls.stream()
            .filter(CrossCompileServiceImpl::isFallback)
            .collect(toUnmodifiableList());
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

        var fallbackImpl = fallbackImpls.get(0);
        impls.remove(fallbackImpl);
        return fallbackImpl;
    }

    private static void assertNoIntersections(
        Class<?> service,
        List<CrossCompileServiceImpl> impls
    ) {
        for (int i = 0; i < impls.size() - 1; ++i) {
            for (int g = i + 1; g < impls.size(); ++g) {
                var thisImpl = impls.get(i);
                var thatImpl = impls.get(g);
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
            var onlySelfIncludedImpls = impls.stream()
                .filter(impl -> impl.getDependencyVersion().isOnlySelfIncluded())
                .collect(toUnmodifiableList());
            for (var impl : onlySelfIncludedImpls) {
                var isActive = isActive(impl, dependencyVersionComparator);
                if (isActive) {
                    return impl;
                }
            }
        }

        {
            var earlierIncludedImpls = impls.stream()
                .filter(impl -> impl.getDependencyVersion().isEarlierIncluded())
                .sorted(comparing(CrossCompileServiceImpl::getDependencyVersion))
                .collect(toUnmodifiableList());
            for (var impl : earlierIncludedImpls) {
                var isActive = isActive(impl, dependencyVersionComparator);
                if (isActive) {
                    return impl;
                }
            }
        }

        {
            var laterIncludedImpls = impls.stream()
                .filter(impl -> impl.getDependencyVersion().isLaterIncluded())
                .filter(not(impl -> impl.getDependencyVersion().isEarlierIncluded()))
                .sorted(comparing(CrossCompileServiceImpl::getDependencyVersion)
                    .reversed()
                )
                .collect(toUnmodifiableList());
            for (var impl : laterIncludedImpls) {
                var isActive = isActive(impl, dependencyVersionComparator);
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
        var className = impl.getClassName();
        var dependencyVersion = impl.getDependencyVersion();
        var dependency = dependencyVersion.getDependency();
        var version = requireNonNull(dependencyVersion.getVersion()).withoutSuffix();
        var earlierIncluded = dependencyVersion.isEarlierIncluded();
        var selfIncluded = dependencyVersion.isSelfIncluded();
        var laterIncluded = dependencyVersion.isLaterIncluded();

        var comparisonResult = dependencyVersionComparator.compareDependencyVersionToCurrentVersion(
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

    private static final CrossCompileVersionComparator DEFAULT_VERSION_COMPARATOR =
        standardVersionCrossCompileVersionComparator("gradle", GradleVersion.current().getVersion())
            .then(standardVersionCrossCompileVersionComparator("java", JavaVersion.current().getMajorVersion()));

}
