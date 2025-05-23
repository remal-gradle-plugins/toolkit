package name.remal.gradle_plugins.toolkit.testkit.internal;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import name.remal.gradle_plugins.toolkit.testkit.AbstractJupiterTestEngineTests;
import name.remal.gradle_plugins.toolkit.testkit.internal.AbstractTestableVersionExtension.ModuleVersionStringRetriever;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

public abstract class AbstractSupportedVersionExtensionTests extends AbstractJupiterTestEngineTests {

    protected abstract static class AbstractModuleVersionStringRetrieverExtension
        extends AbstractTestableVersionExtension
        implements ModuleVersionStringRetriever {

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            context.getStore(NAMESPACE).put(
                ModuleVersionStringRetriever.class,
                this
            );
            return enabled(this.getClass().getName());
        }

    }

}
