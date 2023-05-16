package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.util.Objects.requireNonNull;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;

import com.google.common.base.Splitter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

@Getter
@EqualsAndHashCode
class JacocoJvmArg {

    private final String javaAgentPath;

    private final Map<String, String> params;

    public JacocoJvmArg(String javaAgentPath, Map<String, String> params) {
        this.javaAgentPath = javaAgentPath;
        this.params = new LinkedHashMap<>(params);
    }

    @Nullable
    public String getParam(String key) {
        return getParams().get(key);
    }

    @Nullable
    public String computeParamIfPresent(String key, Function<? super String, String> action) {
        return getParams().computeIfPresent(key, (__, oldValue) -> action.apply(oldValue));
    }

    public void setParam(String key, @Nullable String value) {
        val params = getParams();
        if (value == null) {
            params.remove(key);

        } else {
            params.put(key, value);
        }
    }

    public void addParamElement(String key, String value) {
        val params = getParams();
        val currentValue = params.get(key);
        if (isNotEmpty(currentValue)) {
            val hasNoValueAdded = Splitter.on(':')
                .splitToStream(currentValue)
                .noneMatch(value::equals);
            if (hasNoValueAdded) {
                params.put(key, currentValue + ':' + value);
            }
        } else {
            params.put(key, value);
        }
    }

    @Override
    public String toString() {
        val sb = new StringBuilder();
        sb.append("-javaagent:").append(getJavaAgentPath()).append('=');

        boolean isFirstParam = true;
        for (Entry<String, String> entry : getParams().entrySet()) {
            val key = entry.getKey();
            val value = entry.getValue();
            if (key == null || key.isEmpty() || value == null) {
                continue;
            }

            if (isFirstParam) {
                isFirstParam = false;
            } else {
                sb.append(',');
            }
            sb.append(key).append('=').append(value);
        }

        return sb.toString();
    }


    public static boolean currentJvmArgsHaveJacocoJvmArg() {
        return parseJacocoJvmArgFromCurrentJvmArgs() != null;
    }

    @Nullable
    public static JacocoJvmArg parseJacocoJvmArgFromCurrentJvmArgs() {
        return parseJacocoJvmArgFromJvmArgs(getRuntimeMXBean().getInputArguments());
    }

    @Nullable
    public static JacocoJvmArg parseJacocoJvmArgFromJvmArgs(Collection<String> jvmArgs) {
        return jvmArgs.stream()
            .map(JacocoJvmArg::parseJacocoJvmArgFromJvmArg)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private static final Pattern JACOCO_ARG = Pattern.compile("^-javaagent:(.*?[/\\\\]jacocoagent\\.jar)(?:=(.*))?$");

    @Nullable
    private static JacocoJvmArg parseJacocoJvmArgFromJvmArg(String jvmArg) {
        val matcher = JACOCO_ARG.matcher(jvmArg);
        if (!matcher.matches()) {
            return null;
        }

        val javaAgentPath = requireNonNull(matcher.group(1));

        Map<String, String> params = new LinkedHashMap<>();
        val paramsString = matcher.group(2);
        if (paramsString != null) {
            for (val paramString : Splitter.on(',').split(paramsString)) {
                val equalPos = paramString.indexOf('=');
                if (equalPos < 0) {
                    continue;
                }

                val name = paramString.substring(0, equalPos);
                if (name.isEmpty()) {
                    continue;
                }

                val value = paramString.substring(equalPos + 1);
                params.put(name, value);
            }
        }

        return new JacocoJvmArg(javaAgentPath, params);
    }

}
