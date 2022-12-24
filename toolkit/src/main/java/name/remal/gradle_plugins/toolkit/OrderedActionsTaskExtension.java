package name.remal.gradle_plugins.toolkit;

import java.util.List;
import org.jetbrains.annotations.Unmodifiable;

interface OrderedActionsTaskExtension {

    void add(Object untypedAction);

    @Unmodifiable
    List<OrderedAction<?>> getActions();

}
