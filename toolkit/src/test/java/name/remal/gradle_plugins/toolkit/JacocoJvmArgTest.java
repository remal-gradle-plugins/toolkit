package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.JacocoJvmArg.parseJacocoJvmArgFromJvmArgs;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.List;
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
        assertThat(parseJacocoJvmArgFromJvmArgs(List.of()))
            .isNull();

        assertThat(parseJacocoJvmArgFromJvmArgs(List.of("unknown")))
            .isNull();

        assertThat(parseJacocoJvmArgFromJvmArgs(List.of("-javaagent:path=")))
            .isNull();

        assertThat(parseJacocoJvmArgFromJvmArgs(List.of("-javaagent:/jacocoagent.zip=")))
            .isNull();

        assertThat(parseJacocoJvmArgFromJvmArgs(List.of("-javaagent:/jacocoagent.jar")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of()));

        assertThat(parseJacocoJvmArgFromJvmArgs(List.of("-javaagent:/jacocoagent.jar=")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of()));

        assertThat(parseJacocoJvmArgFromJvmArgs(List.of("-javaagent:/jacocoagent.jar=1=a")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of("1", "a")));

        assertThat(parseJacocoJvmArgFromJvmArgs(List.of("-javaagent:/jacocoagent.jar=1=a,2=b")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of("1", "a", "2", "b")));

        assertThat(parseJacocoJvmArgFromJvmArgs(List.of("-javaagent:/jacocoagent.jar=1=a,2=b,")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of("1", "a", "2", "b")));

        assertThat(parseJacocoJvmArgFromJvmArgs(List.of("-javaagent:/jacocoagent.jar=1=a,2=b,=c")))
            .isEqualTo(new JacocoJvmArg("/jacocoagent.jar", ImmutableMap.of("1", "a", "2", "b")));
    }

}
