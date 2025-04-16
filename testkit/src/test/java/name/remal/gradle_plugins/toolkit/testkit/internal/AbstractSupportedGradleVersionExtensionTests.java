package name.remal.gradle_plugins.toolkit.testkit.internal;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import name.remal.gradle_plugins.toolkit.testkit.AbstractJupiterTestEngineTests;
import name.remal.gradle_plugins.toolkit.testkit.internal.AbstractTestableGradleVersionExtension.CurrentGradleVersionRetriever;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

public abstract class AbstractSupportedGradleVersionExtensionTests extends AbstractJupiterTestEngineTests {

    protected abstract static class AbstractCurrentGradleVersionRetrieverExtension
        extends AbstractTestableGradleVersionExtension
        implements CurrentGradleVersionRetriever {

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            context.getStore(NAMESPACE).put(
                CurrentGradleVersionRetriever.class,
                this
            );
            return enabled(this.getClass().getName());
        }

    }

}
