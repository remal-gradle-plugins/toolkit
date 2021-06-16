package name.remal.gradleplugins.toolkit.testkit.internal;

import java.lang.reflect.Member;
import lombok.val;
import name.remal.gradleplugins.toolkit.testkit.internal.containers.ExtensionStore;
import name.remal.gradleplugins.toolkit.testkit.internal.containers.ProjectDirPrefix;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public abstract class AbstractProjectDirPrefixExtension
    implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    protected final ExtensionStore extensionStore = new ExtensionStore(this);

    @Override
    public void beforeAll(ExtensionContext context) {
        val dirPrefix = ProjectDirPrefix.getProjectDirPrefix(extensionStore, context);
        context.getTestClass().map(Class::getName).ifPresent(dirPrefix::push);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        val dirPrefix = ProjectDirPrefix.getProjectDirPrefix(extensionStore, context);
        context.getTestMethod().map(Member::getName).ifPresent(dirPrefix::push);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        val dirPrefix = ProjectDirPrefix.getProjectDirPrefix(extensionStore, context);
        context.getTestMethod().map(Member::getName).ifPresent(dirPrefix::pop);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        val dirPrefix = ProjectDirPrefix.getProjectDirPrefix(extensionStore, context);
        context.getTestClass().map(Class::getName).ifPresent(dirPrefix::pop);
    }

}
