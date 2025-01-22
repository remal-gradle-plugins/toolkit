package name.remal.gradle_plugins.toolkit.testkit;

import name.remal.gradle_plugins.toolkit.testkit.internal.AbstractSupportedGradleVersionExtensionTests;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

class MinSupportedGradleVersionTest extends AbstractSupportedGradleVersionExtensionTests {

    private static class CurrentGradleVersion6 extends AbstractCurrentGradleVersionRetrieverExtension {
        @Override
        public GradleVersion getCurrentGradleVersion() {
            return GradleVersion.version("6.0");
        }
    }


    @ExtendWith(CurrentGradleVersion6.class)
    @MinSupportedGradleVersion("5.0")
    @ExampleTests
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790"})
    static class ClassExample5 {
        @Test
        void test() {
        }
    }

    @Test
    void annotated_class_5() {
        var tests = executeTestsForClass(ClassExample5.class).testEvents();
        tests.assertStatistics(stats -> stats.succeeded(1));
    }

    @ExtendWith(CurrentGradleVersion6.class)
    @MinSupportedGradleVersion("6.0")
    @ExampleTests
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790"})
    static class ClassExample6 {
        @Test
        void test() {
        }
    }

    @Test
    void annotated_class_6() {
        var tests = executeTestsForClass(ClassExample6.class).testEvents();
        tests.assertStatistics(stats -> stats.succeeded(1));
    }


    @ExtendWith(CurrentGradleVersion6.class)
    @MinSupportedGradleVersion("7.0")
    @ExampleTests
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790"})
    static class ClassExample7 {
        @Test
        void test() {
        }
    }

    @Test
    void annotated_class_7() {
        var tests = executeTestsForClass(ClassExample7.class).testEvents();
        tests.assertStatistics(stats -> stats.succeeded(0));
    }


    @ExtendWith(CurrentGradleVersion6.class)
    @ExampleTests
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790"})
    static class MethodExample5 {
        @Test
        @MinSupportedGradleVersion("5.0")
        void annotated() {
        }

        @Test
        void notAnnotated() {
        }
    }

    @Test
    void annotated_method_5() {
        var tests = executeTestsForClass(MethodExample5.class).testEvents();
        tests.assertStatistics(stats -> stats.succeeded(2).skipped(0));
    }


    @ExtendWith(CurrentGradleVersion6.class)
    @ExampleTests
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790"})
    static class MethodExample6 {
        @Test
        @MinSupportedGradleVersion("6.0")
        void annotated() {
        }

        @Test
        void notAnnotated() {
        }
    }

    @Test
    void annotated_method_6() {
        var tests = executeTestsForClass(MethodExample6.class).testEvents();
        tests.assertStatistics(stats -> stats.succeeded(2).skipped(0));
    }


    @ExtendWith(CurrentGradleVersion6.class)
    @ExampleTests
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790"})
    static class MethodExample7 {
        @Test
        @MinSupportedGradleVersion("7.0")
        void annotated() {
        }

        @Test
        void notAnnotated() {
        }
    }

    @Test
    void annotated_method_7() {
        var tests = executeTestsForClass(MethodExample7.class).testEvents();
        tests.assertStatistics(stats -> stats.succeeded(1).skipped(1));
    }

}
