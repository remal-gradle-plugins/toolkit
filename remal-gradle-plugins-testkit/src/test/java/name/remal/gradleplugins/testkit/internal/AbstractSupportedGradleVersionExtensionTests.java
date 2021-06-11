package name.remal.gradleplugins.testkit.internal;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import name.remal.gradleplugins.testkit.AbstractJupiterTestEngineTests;
import name.remal.gradleplugins.testkit.internal.AbstractSupportedGradleVersionExtension.CurrentGradleVersionRetriever;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

public abstract class AbstractSupportedGradleVersionExtensionTests extends AbstractJupiterTestEngineTests {

    protected abstract static class AbstractCurrentGradleVersionRetrieverExtension
        extends AbstractSupportedGradleVersionExtension
        implements CurrentGradleVersionRetriever {

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            context.getStore(AbstractSupportedGradleVersionExtension.NAMESPACE).put(
                CurrentGradleVersionRetriever.class,
                (CurrentGradleVersionRetriever) this::getCurrentGradleVersion
            );
            return enabled(this.getClass().getName());
        }

    }

}
