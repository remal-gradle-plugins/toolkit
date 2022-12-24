package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;
import static name.remal.gradle_plugins.toolkit.EnumerationUtils.compoundEnumeration;
import static name.remal.gradle_plugins.toolkit.ExtendedUrlClassLoader.LoadingOrder.PARENT_FIRST;
import static org.apache.commons.lang3.ArrayUtils.contains;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import lombok.val;

public class ExtendedUrlClassLoader extends URLClassLoader {

    public enum LoadingOrder {
        PARENT_FIRST, SELF_FIRST, PARENT_ONLY, SELF_ONLY
    }

    @FunctionalInterface
    public interface LoadingOrderRetrieval {
        LoadingOrder getLoadingOrder(String resourceName);
    }


    private final LoadingOrderRetrieval loadingOrderRetrieval;

    public ExtendedUrlClassLoader(
        LoadingOrderRetrieval loadingOrderRetrieval,
        URL[] urls,
        @Nullable ClassLoader parent
    ) {
        super(uniqueUrls(urls), parent != null ? parent : getSystemClassLoader());
        this.loadingOrderRetrieval = loadingOrderRetrieval;
    }

    public ExtendedUrlClassLoader(LoadingOrder loadingOrder, URL[] urls, @Nullable ClassLoader parent) {
        this(__ -> loadingOrder, urls, parent);
    }

    public ExtendedUrlClassLoader(
        LoadingOrderRetrieval loadingOrderRetrieval,
        Iterable<URL> urls,
        @Nullable ClassLoader parent
    ) {
        this(loadingOrderRetrieval, iterableUrlsToArray(urls), parent);
    }

    public ExtendedUrlClassLoader(LoadingOrder loadingOrder, Iterable<URL> urls, @Nullable ClassLoader parent) {
        this(__ -> loadingOrder, urls, parent);
    }

    public ExtendedUrlClassLoader(URL[] urls, @Nullable ClassLoader parent) {
        this(PARENT_FIRST, urls, parent);
    }

    public ExtendedUrlClassLoader(Iterable<URL> urls, @Nullable ClassLoader parent) {
        this(PARENT_FIRST, urls, parent);
    }

    public ExtendedUrlClassLoader(LoadingOrderRetrieval loadingOrderRetrieval, URL[] urls) {
        this(loadingOrderRetrieval, urls, null);
    }

    public ExtendedUrlClassLoader(LoadingOrder loadingOrder, URL[] urls) {
        this(loadingOrder, urls, null);
    }

    public ExtendedUrlClassLoader(LoadingOrderRetrieval loadingOrderRetrieval, Iterable<URL> urls) {
        this(loadingOrderRetrieval, urls, null);
    }

    public ExtendedUrlClassLoader(LoadingOrder loadingOrder, Iterable<URL> urls) {
        this(loadingOrder, urls, null);
    }

    public ExtendedUrlClassLoader(URL[] urls) {
        this(urls, null);
    }

    public ExtendedUrlClassLoader(Iterable<URL> urls) {
        this(urls, null);
    }


    @Override
    @SuppressWarnings("java:S3776")
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(className)) {
            Class<?> loadedClass = findLoadedClass(className);

            if (loadedClass == null) {
                loadedClass = findBootstrapClassOrNull(className);
            }

            if (loadedClass == null) {
                val resourceName = className.replace('.', '/') + ".class";
                val loadingOrder = loadingOrderRetrieval.getLoadingOrder(resourceName);
                switch (loadingOrder) {
                    case PARENT_FIRST:
                        loadedClass = findParentClassOrNull(className);
                        if (loadedClass == null) {
                            loadedClass = findClassOrNull(className);
                        }
                        break;
                    case SELF_FIRST:
                        loadedClass = findClassOrNull(className);
                        if (loadedClass == null) {
                            loadedClass = findParentClassOrNull(className);
                        }
                        break;
                    case PARENT_ONLY:
                        loadedClass = findParentClassOrNull(className);
                        break;
                    case SELF_ONLY:
                        loadedClass = findClassOrNull(className);
                        break;
                    default:
                        throw new IllegalStateException(format(
                            "Unsupported %s: %s",
                            LoadingOrder.class.getSimpleName(),
                            loadingOrder
                        ));
                }
            }

            if (loadedClass == null) {
                throw new ClassNotFoundException(className);
            }

            if (resolve) {
                resolveClass(loadedClass);
            }

            return loadedClass;
        }
    }

    @Nullable
    protected static Class<?> findBootstrapClassOrNull(String className) {
        try {
            return BOOTSTRAP_CLASS_LOADER.loadClass(className);
        } catch (ClassNotFoundException expected) {
            return null;
        }
    }

    @Nullable
    protected final Class<?> findClassOrNull(String className) {
        try {
            return findClass(className);
        } catch (ClassNotFoundException expected) {
            return null;
        }
    }

    @Nullable
    protected final Class<?> findParentClassOrNull(String className) {
        val parent = getParent();
        if (parent == null) {
            return null;
        }

        try {
            return parent.loadClass(className);
        } catch (ClassNotFoundException expected) {
            return null;
        }
    }


    @Nullable
    @Override
    public URL getResource(String resourceName) {
        URL result;
        val loadingOrder = loadingOrderRetrieval.getLoadingOrder(resourceName);
        switch (loadingOrder) {
            case PARENT_FIRST:
                result = getResourceFromParent(resourceName);
                if (result == null) {
                    result = findResource(resourceName);
                }
                break;
            case SELF_FIRST:
                result = findResource(resourceName);
                if (result == null) {
                    result = getResourceFromParent(resourceName);
                }
                break;
            case PARENT_ONLY:
                result = getResourceFromParent(resourceName);
                break;
            case SELF_ONLY:
                result = findResource(resourceName);
                break;
            default:
                throw new IllegalStateException(format(
                    "Unsupported %s: %s",
                    LoadingOrder.class.getSimpleName(),
                    loadingOrder
                ));
        }
        return result;
    }

    @Nullable
    private URL getResourceFromParent(String resourceName) {
        val parentClassLoader = getParent();
        if (parentClassLoader != null) {
            return parentClassLoader.getResource(resourceName);
        } else {
            return getSystemResource(resourceName);
        }
    }


    @Override
    public Enumeration<URL> getResources(String resourceName) throws IOException {
        val loadingOrder = loadingOrderRetrieval.getLoadingOrder(resourceName);
        switch (loadingOrder) {
            case PARENT_FIRST:
                return compoundEnumeration(
                    getResourcesFromParent(resourceName),
                    findResources(resourceName)
                );
            case SELF_FIRST:
                return compoundEnumeration(
                    findResources(resourceName),
                    getResourcesFromParent(resourceName)
                );
            case PARENT_ONLY:
                return getResourcesFromParent(resourceName);
            case SELF_ONLY:
                return findResources(resourceName);
            default:
                throw new IllegalStateException(format(
                    "Unsupported %s: %s",
                    LoadingOrder.class.getSimpleName(),
                    loadingOrder
                ));
        }
    }


    private Enumeration<URL> getResourcesFromParent(String resourceName) throws IOException {
        val parentClassLoader = getParent();
        if (parentClassLoader != null) {
            return parentClassLoader.getResources(resourceName);
        } else {
            return getSystemResources(resourceName);
        }
    }


    @Override
    public synchronized void addURL(URL url) {
        if (!contains(getURLs(), url)) {
            super.addURL(url);
        }
    }


    private static URL[] uniqueUrls(URL[] urls) {
        return Arrays.stream(urls).distinct().toArray(URL[]::new);
    }


    private static URL[] iterableUrlsToArray(Iterable<URL> urls) {
        return StreamSupport.stream(urls.spliterator(), false).toArray(URL[]::new);
    }


    private static final ClassLoader BOOTSTRAP_CLASS_LOADER = new BootstrapClassLoader();

    private static final class BootstrapClassLoader extends ClassLoader {
        private BootstrapClassLoader() {
            super(null);
        }

        static {
            ClassLoader.registerAsParallelCapable();
        }
    }


    static {
        registerAsParallelCapable();
    }

}
