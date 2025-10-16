package name.remal.gradle_plugins.toolkit;

import java.io.Serializable;
import org.gradle.api.Action;
import org.gradle.api.HasImplicitReceiver;

@HasImplicitReceiver
@FunctionalInterface
public interface SerializableAction<T> extends Action<T>, Serializable {
}
