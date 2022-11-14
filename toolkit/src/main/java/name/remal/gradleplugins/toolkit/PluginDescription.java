package name.remal.gradleplugins.toolkit;

import static name.remal.gradleplugins.toolkit.PluginUtils.findPluginIdFor;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.val;
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
        val sb = new StringBuilder();
        sb.append("Gradle plugin ");

        val id = getId();
        val type = getType();
        if (id != null) {
            sb.append(id).append(" (").append(type).append(')');
        } else {
            sb.append("of ").append(type.getName());
        }

        return sb.toString();
    }

}
