package name.remal.gradleplugins.toolkit;

import static name.remal.gradleplugins.toolkit.ObjectUtils.isNotEmpty;

import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Describable;
import org.gradle.api.specs.Spec;

@RequiredArgsConstructor
public class DescribableSpec<T> implements Describable, Spec<T> {

    @Nullable
    private final String description;

    private final Spec<? super T> spec;

    @Override
    public boolean isSatisfiedBy(T element) {
        return spec.isSatisfiedBy(element);
    }

    @Override
    public String getDisplayName() {
        if (isNotEmpty(description)) {
            return description;
        }
        if (spec instanceof Describable) {
            val specDescription = ((Describable) spec).getDisplayName();
            if (isNotEmpty(specDescription)) {
                return specDescription;
            }
        }
        return spec.toString();
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

}
