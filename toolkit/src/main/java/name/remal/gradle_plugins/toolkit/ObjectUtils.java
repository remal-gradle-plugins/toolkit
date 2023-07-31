package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.concurrent.TimeUnit.MINUTES;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.MethodsInvoker.invokeMethod;

import groovy.lang.Closure;
import groovy.lang.GString;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import kotlin.Lazy;
import kotlin.reflect.KCallable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class ObjectUtils {

    public static final byte DEFAULT_BYTE = new PrimitiveDefaultValues().defaultByte;
    public static final short DEFAULT_SHORT = new PrimitiveDefaultValues().defaultShort;
    public static final int DEFAULT_INT = new PrimitiveDefaultValues().defaultInt;
    public static final long DEFAULT_LONG = new PrimitiveDefaultValues().defaultLong;
    public static final float DEFAULT_FLOAT = new PrimitiveDefaultValues().defaultFloat;
    public static final double DEFAULT_DOUBLE = new PrimitiveDefaultValues().defaultDouble;
    public static final char DEFAULT_CHAR = new PrimitiveDefaultValues().defaultChar;
    public static final boolean DEFAULT_BOOLEAN = new PrimitiveDefaultValues().defaultBoolean;

    @SuppressWarnings("unused")
    private static class PrimitiveDefaultValues {
        private byte defaultByte;
        private short defaultShort;
        private int defaultInt;
        private long defaultLong;
        private float defaultFloat;
        private double defaultDouble;
        private char defaultChar;
        private boolean defaultBoolean;
    }


    @Contract(value = "_->param1", pure = true)
    public static <T> T doNotInline(T object) {
        return object;
    }


    @Nullable
    @Contract("null->null")
    @SneakyThrows
    @SuppressWarnings("java:S3776")
    public static Object unwrapProviders(@Nullable Object object) {
        if (object == null) {
            return null;

        } else if (object instanceof Closure) {
            val typedObject = (Closure<?>) object;
            if (typedObject.getMaximumNumberOfParameters() == 0 && isEmpty(typedObject.getParameterTypes())) {
                return unwrapProviders(typedObject.call());
            } else {
                return object;
            }
        } else if (object instanceof GString) {
            return object.toString();

        } else if (object instanceof Optional) {
            return unwrapProviders(((Optional<?>) object).orElse(null));
        } else if (object instanceof OptionalInt) {
            val typedObject = (OptionalInt) object;
            return typedObject.isPresent() ? typedObject.getAsInt() : null;
        } else if (object instanceof OptionalLong) {
            val typedObject = (OptionalLong) object;
            return typedObject.isPresent() ? typedObject.getAsLong() : null;
        } else if (object instanceof OptionalDouble) {
            val typedObject = (OptionalDouble) object;
            return typedObject.isPresent() ? typedObject.getAsDouble() : null;

        } else if (object instanceof Callable) {
            return unwrapProviders(((Callable<?>) object).call());
        } else if (object instanceof Supplier) {
            return unwrapProviders(((Supplier<?>) object).get());
        } else if (object instanceof BooleanSupplier) {
            return ((BooleanSupplier) object).getAsBoolean();
        } else if (object instanceof IntSupplier) {
            return ((IntSupplier) object).getAsInt();
        } else if (object instanceof LongSupplier) {
            return ((LongSupplier) object).getAsLong();
        } else if (object instanceof DoubleSupplier) {
            return ((DoubleSupplier) object).getAsDouble();

        } else if (object instanceof Future) {
            return unwrapProviders(((Future<?>) object).get(5, MINUTES));
        } else if (object instanceof CompletionStage) {
            return unwrapProviders(((CompletionStage<?>) object).toCompletableFuture().get(5, MINUTES));

        } else if (object instanceof AtomicBoolean) {
            return ((AtomicBoolean) object).get();
        } else if (object instanceof AtomicInteger) {
            return ((AtomicInteger) object).get();
        } else if (object instanceof AtomicLong) {
            return ((AtomicLong) object).get();
        } else if (object instanceof AtomicReference) {
            return unwrapProviders(((AtomicReference<?>) object).get());

        } else if (object instanceof Lazy) {
            return unwrapProviders(((Lazy<?>) object).getValue());
        } else if (object instanceof KCallable) {
            val typedObject = (KCallable<?>) object;
            if (isEmpty(typedObject.getTypeParameters())) {
                return unwrapProviders(typedObject.call());
            } else {
                return typedObject;
            }

        } else if (object instanceof Provider) {
            return unwrapProviders(((Provider<?>) object).getOrNull());

        } else if (('.' + object.getClass().getName()).endsWith(".com.google.common.base.Absent")) {
            return null;
        } else if (('.' + object.getClass().getName()).endsWith(".com.google.common.base.Present")) {
            return unwrapProviders(invokeMethod(object, Object.class, "get"));

        } else {
            return object;
        }
    }


    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable CharSequence value) {
        return value == null || value.length() == 0;
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable Iterable<?> value) {
        return value == null || value.iterator().hasNext();
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable Collection<?> value) {
        return value == null || value.isEmpty();
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable Map<?, ?> value) {
        return value == null || value.isEmpty();
    }

    @Contract(value = "null->true", pure = true)
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "java:S2789"})
    public static boolean isEmpty(@Nullable Optional<?> value) {
        return value == null || !value.isPresent();
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable Object[] value) {
        return value == null || value.length == 0;
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable byte[] value) {
        return value == null || value.length == 0;
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable short[] value) {
        return value == null || value.length == 0;
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable int[] value) {
        return value == null || value.length == 0;
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable long[] value) {
        return value == null || value.length == 0;
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable float[] value) {
        return value == null || value.length == 0;
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable double[] value) {
        return value == null || value.length == 0;
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable char[] value) {
        return value == null || value.length == 0;
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable boolean[] value) {
        return value == null || value.length == 0;
    }


    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable CharSequence value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable Iterable<?> value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable Collection<?> value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable Map<?, ?> value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "java:S2789"})
    public static boolean isNotEmpty(@Nullable Optional<?> value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable Object[] value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable byte[] value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable short[] value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable int[] value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable long[] value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable float[] value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable double[] value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable char[] value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable boolean[] value) {
        return !isEmpty(value);
    }


    @Contract(value = "!null,_->param1; null,_->param2", pure = true)
    public static <T> T defaultValue(@Nullable T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Contract(pure = true)
    public static String defaultValue(@Nullable String value) {
        return defaultValue(value, "");
    }

    @Contract(pure = true)
    public static <T> List<T> defaultValue(@Nullable List<T> value) {
        return defaultValue(value, emptyList());
    }

    @Contract(pure = true)
    public static <T> Set<T> defaultValue(@Nullable Set<T> value) {
        return defaultValue(value, emptySet());
    }

    @Contract(pure = true)
    public static <K, V> Map<K, V> defaultValue(@Nullable Map<K, V> value) {
        return defaultValue(value, emptyMap());
    }

    @Contract(pure = true)
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "java:S2789"})
    public static <T> Optional<T> defaultValue(@Nullable Optional<T> value) {
        return defaultValue(value, Optional.empty());
    }


    @Contract(value = "!null,_->param1; null,_->param2", pure = true)
    public static byte defaultValue(@Nullable Byte value, byte defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Contract(pure = true)
    public static byte defaultValue(@Nullable Byte value) {
        return defaultValue(value, DEFAULT_BYTE);
    }

    @Contract(value = "!null,_->param1; null,_->param2", pure = true)
    public static short defaultValue(@Nullable Short value, short defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Contract(pure = true)
    public static short defaultValue(@Nullable Short value) {
        return defaultValue(value, DEFAULT_SHORT);
    }

    @Contract(value = "!null,_->param1; null,_->param2", pure = true)
    public static int defaultValue(@Nullable Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Contract(pure = true)
    public static int defaultValue(@Nullable Integer value) {
        return defaultValue(value, DEFAULT_INT);
    }

    @Contract(value = "!null,_->param1; null,_->param2", pure = true)
    public static long defaultValue(@Nullable Long value, long defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Contract(pure = true)
    public static long defaultValue(@Nullable Long value) {
        return defaultValue(value, DEFAULT_LONG);
    }

    @Contract(value = "!null,_->param1; null,_->param2", pure = true)
    public static float defaultValue(@Nullable Float value, float defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Contract(pure = true)
    public static float defaultValue(@Nullable Float value) {
        return defaultValue(value, DEFAULT_FLOAT);
    }

    @Contract(value = "!null,_->param1; null,_->param2", pure = true)
    public static double defaultValue(@Nullable Double value, double defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Contract(pure = true)
    public static double defaultValue(@Nullable Double value) {
        return defaultValue(value, DEFAULT_DOUBLE);
    }

    @Contract(value = "!null,_->param1; null,_->param2", pure = true)
    public static char defaultValue(@Nullable Character value, char defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Contract(pure = true)
    public static char defaultValue(@Nullable Character value) {
        return defaultValue(value, DEFAULT_CHAR);
    }

    @Contract(value = "!null,_->param1; null,_->param2", pure = true)
    public static boolean defaultValue(@Nullable Boolean value, boolean defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Contract(pure = true)
    public static boolean defaultValue(@Nullable Boolean value) {
        return defaultValue(value, DEFAULT_BOOLEAN);
    }

    @Contract(pure = true)
    public static boolean defaultTrue(@Nullable Boolean value) {
        return defaultValue(value, true);
    }

    @Contract(pure = true)
    public static boolean defaultFalse(@Nullable Boolean value) {
        return defaultValue(value, false);
    }

}
