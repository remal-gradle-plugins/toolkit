package name.remal.gradleplugins.toolkit.testkit.internal.containers;

import static java.lang.String.format;
import static org.junit.platform.commons.util.ReflectionUtils.isInnerClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.List;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.val;
import org.junit.platform.commons.util.AnnotationUtils;

@Value
@EqualsAndHashCode(of = "parameter")
class AnnotatedParam {

    @NonNull
    Parameter parameter;

    public Executable getDeclaringExecutable() {
        return parameter.getDeclaringExecutable();
    }

    public String getName() {
        if (!parameter.isNamePresent()) {
            throw new IllegalStateException(this + ": name can't be retrieved via reflection"
                + " (is it compiled with '-parameters' flag?"
            );
        }

        return parameter.getName();
    }

    public int getIndex() {
        val params = getDeclaringExecutable().getParameters();
        for (int i = 0; i < params.length; ++i) {
            val param = params[i];
            if (param.equals(parameter)) {
                return i;
            }
        }
        throw new RuntimeException("Parameter index can't be retrieved for " + parameter);
    }

    public boolean isAnnotated(Class<? extends Annotation> annotationType) {
        val annotatedParameter = getAnnotatedParameter();
        return AnnotationUtils.isAnnotated(annotatedParameter, annotationType);
    }

    @Nullable
    public <T extends Annotation> T findAnnotation(Class<T> annotationType) {
        val annotatedParameter = getAnnotatedParameter();
        return AnnotationUtils.findAnnotation(annotatedParameter, annotationType).orElse(null);
    }

    public <T extends Annotation> List<T> findRepeatableAnnotations(Class<T> annotationType) {
        val annotatedParameter = getAnnotatedParameter();
        return AnnotationUtils.findRepeatableAnnotations(annotatedParameter, annotationType);
    }

    /**
     * There is a bug in {@code javac} on JDK versions prior to JDK 9. This method returns true annotated parameter.
     */
    private AnnotatedElement getAnnotatedParameter() {
        Executable executable = parameter.getDeclaringExecutable();
        if (executable instanceof Constructor && isInnerClass(executable.getDeclaringClass())
            && executable.getParameterAnnotations().length == executable.getParameterCount() - 1
        ) {
            val index = getIndex();
            if (index == 0) {
                throw new IllegalStateException(format(
                    "A %s should never be created for parameter index 0 in an inner class constructor",
                    AnnotatedParam.class.getSimpleName()
                ));
            }

            return executable.getParameters()[index - 1];
        }

        return this.parameter;
    }

    @Override
    public String toString() {
        return format("Executable %s: parameter %s", getDeclaringExecutable(), parameter);
    }

}
