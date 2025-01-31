package name.remal.gradle_plugins.toolkit.testkit.internal;

import java.lang.reflect.Member;
import name.remal.gradle_plugins.toolkit.testkit.internal.containers.ExtensionStore;
import name.remal.gradle_plugins.toolkit.testkit.internal.containers.ProjectDirPrefix;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Internal
public abstract class AbstractProjectDirPrefixExtension
    implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    protected final ExtensionStore extensionStore = new ExtensionStore(this);

    @Override
    public void beforeAll(ExtensionContext context) {
        var dirPrefix = ProjectDirPrefix.getProjectDirPrefix(extensionStore, context);
        context.getTestClass().map(Class::getName).ifPresent(dirPrefix::push);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        var dirPrefix = ProjectDirPrefix.getProjectDirPrefix(extensionStore, context);
        context.getTestMethod().map(Member::getName).ifPresent(dirPrefix::push);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        var dirPrefix = ProjectDirPrefix.getProjectDirPrefix(extensionStore, context);
        context.getTestMethod().map(Member::getName).ifPresent(dirPrefix::pop);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        var dirPrefix = ProjectDirPrefix.getProjectDirPrefix(extensionStore, context);
        context.getTestClass().map(Class::getName).ifPresent(dirPrefix::pop);
    }

}
