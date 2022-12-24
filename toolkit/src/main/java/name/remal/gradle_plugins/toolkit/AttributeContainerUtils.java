package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE;
import static org.gradle.api.attributes.Category.LIBRARY;
import static org.gradle.api.attributes.Usage.JAVA_RUNTIME;
import static org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE;

import lombok.NoArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;

@NoArgsConstructor(access = PRIVATE)
public abstract class AttributeContainerUtils {

    public static Action<AttributeContainer> javaRuntimeLibrary(ObjectFactory objects) {
        return attrs -> {
            attrs.attribute(
                USAGE_ATTRIBUTE,
                objects.named(Usage.class, JAVA_RUNTIME)
            );
            attrs.attribute(
                CATEGORY_ATTRIBUTE,
                objects.named(Category.class, LIBRARY)
            );
        };
    }

}
