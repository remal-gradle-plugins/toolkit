package name.remal.gradle_plugins.toolkit.testkit.functional;

import static name.remal.gradle_plugins.toolkit.testkit.functional.JacocoJvmArg.parseJacocoJvmArgFromJvmArgs;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

class JacocoJvmArgTest {

    @Test
    void to_string() {
        assertThat(new JacocoJvmArg("path", ImmutableMap.of()))
            .asString().isEqualTo("-javaagent:path=");

        assertThat(new JacocoJvmArg("path", ImmutableMap.of("", "a")))
            .asString().isEqualTo("-javaagent:path=");

        assertThat(new JacocoJvmArg("path", ImmutableMap.of("1", "a")))
            .asString().isEqualTo("-javaagent:path=1=a");

        assertThat(new JacocoJvmArg("path", ImmutableMap.of("1", "a", "2", "b")))
            .asString().isEqualTo("-javaagent:path=1=a,2=b");
    }

    @Test
    void parse() {
        assertThat(parseJacocoJvmArgFromJvmArgs(ImmutableList.of()))
            .isNull();

        assertThat(parseJacocoJvmArgFromJvmArgs(ImmutableList.of("unknown")))
            .isNull();

        assertThat(parseJacocoJvmArgFromJvmArgs(ImmutableList.of("-javaagent:path=")))
            .isNull();

        assertThat(parseJacocoJvmArgFromJvmArgs(ImmutableList.of("-javaagent:/jacocoagent.zip=")))
            .isNull();

        assertThat(parseJacocoJvmArgFromJvmArgs(ImmutableList.of("-javaagent:/jacocoagent.jar")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of()));

        assertThat(parseJacocoJvmArgFromJvmArgs(ImmutableList.of("-javaagent:/jacocoagent.jar=")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of()));

        assertThat(parseJacocoJvmArgFromJvmArgs(ImmutableList.of("-javaagent:/jacocoagent.jar=1=a")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of("1", "a")));

        assertThat(parseJacocoJvmArgFromJvmArgs(ImmutableList.of("-javaagent:/jacocoagent.jar=1=a,2=b")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of("1", "a", "2", "b")));

        assertThat(parseJacocoJvmArgFromJvmArgs(ImmutableList.of("-javaagent:/jacocoagent.jar=1=a,2=b,")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of("1", "a", "2", "b")));

        assertThat(parseJacocoJvmArgFromJvmArgs(ImmutableList.of("-javaagent:/jacocoagent.jar=1=a,2=b,=c")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of("1", "a", "2", "b")));
    }

}
