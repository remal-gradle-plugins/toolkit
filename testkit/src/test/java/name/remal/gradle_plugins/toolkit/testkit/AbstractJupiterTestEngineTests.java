package name.remal.gradle_plugins.toolkit.testkit;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.jupiter.engine.config.JupiterConfiguration.EXTENSIONS_AUTODETECTION_ENABLED_PROPERTY_NAME;
import static org.junit.jupiter.engine.config.JupiterConfiguration.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

public abstract class AbstractJupiterTestEngineTests {

    private final JupiterTestEngine engine = new JupiterTestEngine();

    protected EngineExecutionResults executeTestsForClass(Class<?> testClass) {
        return executeTests(selectClass(testClass));
    }

    protected EngineExecutionResults executeTests(DiscoverySelector... selectors) {
        var request = configureLauncherDiscoveryRequestBuilder(request())
            .selectors(selectors)
            .build();
        return executeTests(request);
    }

    protected EngineExecutionResults executeTests(LauncherDiscoveryRequest request) {
        return EngineTestKit.execute(this.engine, request);
    }

    protected TestDescriptor discoverTests(DiscoverySelector... selectors) {
        var request = configureLauncherDiscoveryRequestBuilder(request())
            .selectors(selectors)
            .build();
        return discoverTests(request);
    }

    protected TestDescriptor discoverTests(LauncherDiscoveryRequest request) {
        return engine.discover(request, UniqueId.forEngine(engine.getId()));
    }

    protected UniqueId discoverUniqueId(Class<?> clazz, String methodName) {
        TestDescriptor engineDescriptor = discoverTests(selectMethod(clazz, methodName));
        Set<? extends TestDescriptor> descendants = engineDescriptor.getDescendants();
        TestDescriptor testDescriptor = descendants.stream()
            .skip(descendants.size() - 1)
            .findFirst()
            .orElseGet(() -> fail("no descendants"));
        return testDescriptor.getUniqueId();
    }


    private static LauncherDiscoveryRequestBuilder configureLauncherDiscoveryRequestBuilder(
        LauncherDiscoveryRequestBuilder request
    ) {
        return request
            .configurationParameter(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, "false")
            .configurationParameter(EXTENSIONS_AUTODETECTION_ENABLED_PROPERTY_NAME, "false")
            ;
    }


    @ExtendWith(DisabledIfNotExecutedFromTestKit.class)
    @TestMethodOrder(MethodName.class)
    @Inherited
    @Target({TYPE, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    protected @interface ExampleTests {
    }

    private static class DisabledIfNotExecutedFromTestKit implements ExecutionCondition {

        private static final String TEST_KIT_PACKAGE_NAME_PREFIX = "org.junit.platform.testkit.";

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            var stackTraceElements = new Throwable().getStackTrace();
            for (var stackTraceElement : stackTraceElements) {
                if (stackTraceElement.getClassName().startsWith(TEST_KIT_PACKAGE_NAME_PREFIX)) {
                    return enabled("Stack-trace has JUnit5 TestKit classes");
                }
            }
            return disabled("Stack-trace doesn't have JUnit5 TestKit classes");
        }

    }

}
