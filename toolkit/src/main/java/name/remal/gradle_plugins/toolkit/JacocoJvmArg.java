package name.remal.gradle_plugins.toolkit;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;

import com.google.common.base.Splitter;
import java.io.File;
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
import org.gradle.process.CommandLineArgumentProvider;
import org.jetbrains.annotations.Contract;

@Getter
@EqualsAndHashCode
public class JacocoJvmArg implements CommandLineArgumentProvider {

    private String javaAgentPath;

    private final Map<String, String> params;

    public JacocoJvmArg(String javaAgentPath, Map<String, String> params) {
        this.javaAgentPath = javaAgentPath;
        this.params = new LinkedHashMap<>(params);
    }

    public void makePathsAbsolute() {
        this.javaAgentPath = absolutizePath(getJavaAgentPath());
        computeParamIfPresent("destfile", JacocoJvmArg::absolutizePath);
    }

    public void excludeGradleClasses() {
        addParamElement("excludes", "org.gradle.*");
    }

    public void append(@Nullable Boolean value) {
        setParam("append", value);
    }

    public void dumpOnExit(@Nullable Boolean value) {
        setParam("dumponexit", value);
    }

    public void jmx(@Nullable Boolean value) {
        setParam("jmx", value);
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
        var params = getParams();
        if (value == null) {
            params.remove(key);

        } else {
            params.put(key, value);
        }
    }

    public void setParam(String key, @Nullable Boolean value) {
        setParam(key, value != null ? value.toString() : null);
    }

    public void addParamElement(String key, String value) {
        var params = getParams();
        var currentValue = params.get(key);
        if (isNotEmpty(currentValue)) {
            var hasNoValueAdded = Splitter.on(':')
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
        var sb = new StringBuilder();
        sb.append("-javaagent:").append(getJavaAgentPath()).append('=');

        boolean isFirstParam = true;
        for (Entry<String, String> entry : getParams().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
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

    @Override
    public Iterable<String> asArguments() {
        return singletonList(toString());
    }


    private static String absolutizePath(String path) {
        if (isEmpty(path)) {
            return path;
        }

        File file = new File(path);
        file = normalizeFile(file);
        return file.getAbsolutePath();
    }


    public static boolean currentJvmArgsHaveJacocoJvmArg() {
        return parseJacocoJvmArgFromCurrentJvmArgs() != null;
    }

    @Contract(pure = true)
    @Nullable
    public static JacocoJvmArg parseJacocoJvmArgFromCurrentJvmArgs() {
        return parseJacocoJvmArgFromJvmArgs(getRuntimeMXBean().getInputArguments());
    }

    @Contract(pure = true)
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
        var matcher = JACOCO_ARG.matcher(jvmArg);
        if (!matcher.matches()) {
            return null;
        }

        var javaAgentPath = requireNonNull(matcher.group(1));

        Map<String, String> params = new LinkedHashMap<>();
        var paramsString = matcher.group(2);
        if (paramsString != null) {
            for (var paramString : Splitter.on(',').split(paramsString)) {
                var equalPos = paramString.indexOf('=');
                if (equalPos < 0) {
                    continue;
                }

                var name = paramString.substring(0, equalPos);
                if (name.isEmpty()) {
                    continue;
                }

                var value = paramString.substring(equalPos + 1);
                params.put(name, value);
            }
        }

        return new JacocoJvmArg(javaAgentPath, params);
    }

}
