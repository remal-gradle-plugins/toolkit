package name.remal.gradleplugins.toolkit.testkit.internal.containers;

import static name.remal.gradleplugins.toolkit.testkit.internal.containers.ProjectDirPrefix.getProjectDirPrefix;

import java.util.ArrayList;
import java.util.Collection;
import lombok.val;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

public abstract class AbstractExtensionContextContainer<Resource> implements CloseableResource {

    protected void cleanup(Resource resource, boolean isExceptionThrown) {
    }


    private final Collection<Resource> registeredResources = new ArrayList<>();

    protected final ExtensionStore extensionStore;
    protected final ExtensionContext context;

    protected AbstractExtensionContextContainer(ExtensionStore extensionStore, ExtensionContext context) {
        this.extensionStore = extensionStore;
        this.context = context;
    }

    @Override
    public final synchronized void close() {
        val isExceptionThrown = context.getExecutionException().isPresent();
        val registeredResourcesIterator = registeredResources.iterator();
        while (registeredResourcesIterator.hasNext()) {
            val resource = registeredResourcesIterator.next();
            registeredResourcesIterator.remove();

            cleanup(resource, isExceptionThrown);
        }
    }

    protected final synchronized void registerResource(Resource resource) {
        registeredResources.add(resource);
    }

    protected final ProjectDirPrefix getDirPrefix() {
        return getProjectDirPrefix(extensionStore, context);
    }

}
