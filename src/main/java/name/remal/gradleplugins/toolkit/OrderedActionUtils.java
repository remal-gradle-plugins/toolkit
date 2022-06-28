package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.ProxyUtils.toDynamicInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionAware;

@NoArgsConstructor(access = PRIVATE)
public abstract class OrderedActionUtils {

    private static final String EXTENSION_PREFIX = "name.remal.gradle-plugins.toolkit:toolkit:";
    private static final String DO_FIRST_ORDERED_EXTENSION = EXTENSION_PREFIX + "doFirstOrdered";
    private static final String DO_LAST_ORDERED_EXTENSION = EXTENSION_PREFIX + "doLastOrdered";

    @SuppressWarnings("unchecked")
    public static void doFirstOrdered(Task task, OrderedAction<? super Task> action) {
        val container = getOrCreateExtension(task, DO_FIRST_ORDERED_EXTENSION, ext -> {
            task.onlyIf(__ -> {
                val allActions = new ArrayList<>(ext.getActions());
                Collections.reverse(allActions);
                allActions.forEach(it -> task.doFirst(it.getId(), (OrderedAction<Task>) it));
                return true;
            });
        });

        container.add(action);
    }

    @SuppressWarnings("unchecked")
    public static void doLastOrdered(Task task, OrderedAction<? super Task> action) {
        val container = getOrCreateExtension(task, DO_LAST_ORDERED_EXTENSION, ext -> {
            task.onlyIf(__ -> {
                val allActions = ext.getActions();
                allActions.forEach(it -> task.doLast(it.getId(), (OrderedAction<Task>) it));
                return true;
            });
        });

        container.add(action);
    }


    private static OrderedActionsTaskExtension getOrCreateExtension(
        ExtensionAware object,
        String extensionName,
        Consumer<OrderedActionsTaskExtension> onCreate
    ) {
        val untypedExtension = object.getExtensions().findByName(extensionName);
        if (untypedExtension == null) {
            val extension = object.getExtensions().create(extensionName, OrderedActionsTaskExtensionImpl.class);
            onCreate.accept(extension);
            return extension;

        } else if (untypedExtension instanceof OrderedActionsTaskExtension) {
            return (OrderedActionsTaskExtension) untypedExtension;

        } else {
            return toDynamicInterface(untypedExtension, OrderedActionsTaskExtension.class);
        }
    }

}
