package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.PluginUtils.findPluginIdFor;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import com.google.errorprone.annotations.concurrent.LazyInit;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import org.gradle.api.Plugin;

@EqualsAndHashCode(of = "type")
public final class PluginDescription {

    private final Class<? extends Plugin<?>> type;

    public PluginDescription(Class<? extends Plugin<?>> type) {
        this.type = unwrapGeneratedSubclass(type);
    }

    public Class<? extends Plugin<?>> getType() {
        return type;
    }


    private static final String UNINITIALIZED_ID = "~!@#$%^&*()_+";

    @Nullable
    @LazyInit
    private volatile String id = UNINITIALIZED_ID;

    @Nullable
    @SuppressWarnings({"StringEquality", "java:S4973", "ReferenceEquality"})
    public synchronized String getId() {
        if (id == UNINITIALIZED_ID) {
            id = findPluginIdFor(getType());
        }
        return id;
    }


    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("Gradle plugin ");

        var id = getId();
        var type = getType();
        if (id != null) {
            sb.append(id).append(" (").append(type).append(')');
        } else {
            sb.append("of ").append(type.getName());
        }

        return sb.toString();
    }

}
