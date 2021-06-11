package name.remal.gradleplugins.testkit.internal;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import name.remal.gradleplugins.testkit.AbstractJupiterTestEngineTests;
import name.remal.gradleplugins.testkit.internal.AbstractSupportedVersionExtension.ModuleVersionStringRetriever;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

public abstract class AbstractSupportedVersionExtensionTests extends AbstractJupiterTestEngineTests {

    protected abstract static class AbstractModuleVersionStringRetrieverExtension
        extends AbstractSupportedVersionExtension
        implements ModuleVersionStringRetriever {

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            context.getStore(AbstractSupportedVersionExtension.NAMESPACE).put(
                ModuleVersionStringRetriever.class,
                (ModuleVersionStringRetriever) this::getModuleVersionString
            );
            return enabled(this.getClass().getName());
        }

    }

}
