package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.emptyList;

import java.util.Collection;
import org.gradle.api.Action;
import org.gradle.api.Describable;
import org.intellij.lang.annotations.Pattern;

public interface OrderedAction<T> extends Action<T>, Describable {

    @Pattern("[\\w.-]+")
    String getId();

    default int getStage() {
        return 0;
    }

    /**
     * @return a list of action IDs of actions that should be executed before this action
     */
    default Collection<String> getShouldBeExecutedAfter() {
        return emptyList();
    }

    /**
     * @return a list of action IDs of actions that should be executed after this action
     */
    default Collection<String> getShouldBeExecutedBefore() {
        return emptyList();
    }

    @Override
    default String getDisplayName() {
        return "Execute " + getId();
    }

}
