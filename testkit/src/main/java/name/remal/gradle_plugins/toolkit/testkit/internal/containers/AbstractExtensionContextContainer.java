package name.remal.gradle_plugins.toolkit.testkit.internal.containers;

import static name.remal.gradle_plugins.toolkit.testkit.internal.containers.ProjectDirPrefix.getProjectDirPrefix;

import java.util.ArrayList;
import java.util.Collection;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

@Internal
public abstract class AbstractExtensionContextContainer<Resource> implements CloseableResource {

    protected void cleanup(Resource resource, boolean isExceptionThrown) throws Throwable {
    }

    protected void additionalCleanup(boolean isExceptionThrown) throws Throwable {
    }


    private final Collection<Resource> registeredResources = new ArrayList<>();

    protected final ExtensionStore extensionStore;
    protected final ExtensionContext context;

    protected AbstractExtensionContextContainer(ExtensionStore extensionStore, ExtensionContext context) {
        this.extensionStore = extensionStore;
        this.context = context;
    }

    @Override
    public final synchronized void close() throws Throwable {
        var isExceptionThrown = context.getExecutionException().isPresent();

        var registeredResourcesIterator = registeredResources.iterator();
        while (registeredResourcesIterator.hasNext()) {
            var resource = registeredResourcesIterator.next();
            registeredResourcesIterator.remove();

            cleanup(resource, isExceptionThrown);
        }

        additionalCleanup(isExceptionThrown);
    }


    protected final synchronized void registerResource(Resource resource) {
        registeredResources.add(resource);
    }

    protected final ProjectDirPrefix getDirPrefix() {
        return getProjectDirPrefix(extensionStore, context);
    }

}
