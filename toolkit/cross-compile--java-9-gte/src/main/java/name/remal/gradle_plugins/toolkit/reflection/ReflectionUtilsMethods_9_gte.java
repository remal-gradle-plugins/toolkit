package name.remal.gradle_plugins.toolkit.reflection;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;
import lombok.val;

@AutoService(ReflectionUtilsMethods.class)
final class ReflectionUtilsMethods_9_gte implements ReflectionUtilsMethods {

    @Nullable
    @Override
    public String moduleNameOf(Class<?> clazz) {
        val module = clazz.getModule();
        return module.isNamed() ? module.getName() : null;
    }

}
