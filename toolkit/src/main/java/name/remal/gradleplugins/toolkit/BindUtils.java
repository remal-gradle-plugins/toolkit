package name.remal.gradleplugins.toolkit;

import static javax.annotation.meta.When.UNKNOWN;
import static lombok.AccessLevel.PRIVATE;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings({"overloads", "checkstyle:OverloadMethodsDeclarationOrder"})
public abstract class BindUtils {

    //#region java.util.function.BiConsumer

    public static <T, U> Consumer<U> bindParams(
        BiConsumer<T, U> consumer,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return param2 -> consumer.accept(boundParam1, param2);
    }

    public static <T, U> Runnable bindParams(
        BiConsumer<T, U> consumer,
        @Nonnull(when = UNKNOWN) T boundParam1,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return () -> consumer.accept(boundParam1, boundParam2);
    }

    public static <T, U> Consumer<T> bindLastParams(
        BiConsumer<T, U> consumer,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return param1 -> consumer.accept(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.BiFunction

    public static <T, U, R> Function<U, R> bindParams(
        BiFunction<T, U, R> function,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return param2 -> function.apply(boundParam1, param2);
    }

    public static <T, U, R> Supplier<R> bindParams(
        BiFunction<T, U, R> function,
        @Nonnull(when = UNKNOWN) T boundParam1,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return () -> function.apply(boundParam1, boundParam2);
    }

    public static <T, U, R> Function<T, R> bindLastParams(
        BiFunction<T, U, R> function,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return param1 -> function.apply(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.BinaryOperator

    public static <T> UnaryOperator<T> bindParams(
        BinaryOperator<T> operator,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return param2 -> operator.apply(boundParam1, param2);
    }

    public static <T> Supplier<T> bindParams(
        BinaryOperator<T> operator,
        @Nonnull(when = UNKNOWN) T boundParam1,
        @Nonnull(when = UNKNOWN) T boundParam2
    ) {
        return () -> operator.apply(boundParam1, boundParam2);
    }

    public static <T> UnaryOperator<T> bindLastParams(
        BinaryOperator<T> operator,
        @Nonnull(when = UNKNOWN) T boundParam2
    ) {
        return param1 -> operator.apply(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.BiPredicate

    public static <T, U> Predicate<U> bindParams(
        BiPredicate<T, U> predicate,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return param2 -> predicate.test(boundParam1, param2);
    }

    public static <T, U> BooleanSupplier bindParams(
        BiPredicate<T, U> predicate,
        @Nonnull(when = UNKNOWN) T boundParam1,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return () -> predicate.test(boundParam1, boundParam2);
    }

    public static <T, U> Predicate<T> bindLastParams(
        BiPredicate<T, U> predicate,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return param1 -> predicate.test(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.Consumer

    public static <T> Runnable bindParams(
        Consumer<T> consumer,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return () -> consumer.accept(boundParam1);
    }

    //#endregion

    //#region java.util.function.DoubleBinaryOperator

    public static DoubleUnaryOperator bindParams(
        DoubleBinaryOperator operator,
        double boundParam1
    ) {
        return param2 -> operator.applyAsDouble(boundParam1, param2);
    }

    public static DoubleSupplier bindParams(
        DoubleBinaryOperator operator,
        double boundParam1,
        double boundParam2
    ) {
        return () -> operator.applyAsDouble(boundParam1, boundParam2);
    }

    public static DoubleUnaryOperator bindLastParams(
        DoubleBinaryOperator operator,
        double boundParam2
    ) {
        return param1 -> operator.applyAsDouble(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.DoubleConsumer

    public static Runnable bindParams(
        DoubleConsumer consumer,
        double boundParam1
    ) {
        return () -> consumer.accept(boundParam1);
    }

    //#endregion

    //#region java.util.function.DoubleFunction

    public static <R> Supplier<R> bindParams(
        DoubleFunction<R> function,
        double boundParam1
    ) {
        return () -> function.apply(boundParam1);
    }

    //#endregion

    //#region java.util.function.DoublePredicate

    public static BooleanSupplier bindParams(
        DoublePredicate function,
        double boundParam1
    ) {
        return () -> function.test(boundParam1);
    }

    //#endregion

    //#region java.util.function.DoubleToIntFunction

    public static IntSupplier bindParams(
        DoubleToIntFunction function,
        double boundParam1
    ) {
        return () -> function.applyAsInt(boundParam1);
    }

    //#endregion

    //#region java.util.function.DoubleToLongFunction

    public static LongSupplier bindParams(
        DoubleToLongFunction function,
        double boundParam1
    ) {
        return () -> function.applyAsLong(boundParam1);
    }

    //#endregion

    //#region java.util.function.DoubleUnaryOperator

    public static DoubleSupplier bindParams(
        DoubleUnaryOperator operator,
        double boundParam1
    ) {
        return () -> operator.applyAsDouble(boundParam1);
    }

    //#endregion

    //#region java.util.function.Function

    public static <T, R> Supplier<R> bindParams(
        Function<T, R> function,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return () -> function.apply(boundParam1);
    }

    //#endregion

    //#region java.util.function.IntBinaryOperator

    public static IntUnaryOperator bindParams(
        IntBinaryOperator operator,
        int boundParam1
    ) {
        return param2 -> operator.applyAsInt(boundParam1, param2);
    }

    public static IntSupplier bindParams(
        IntBinaryOperator operator,
        int boundParam1,
        int boundParam2
    ) {
        return () -> operator.applyAsInt(boundParam1, boundParam2);
    }

    public static IntUnaryOperator bindLastParams(
        IntBinaryOperator operator,
        int boundParam2
    ) {
        return param1 -> operator.applyAsInt(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.IntConsumer

    public static Runnable bindParams(
        IntConsumer consumer,
        int boundParam1
    ) {
        return () -> consumer.accept(boundParam1);
    }

    //#endregion

    //#region java.util.function.IntFunction

    public static <R> Supplier<R> bindParams(
        IntFunction<R> function,
        int boundParam1
    ) {
        return () -> function.apply(boundParam1);
    }

    //#endregion

    //#region java.util.function.IntPredicate

    public static BooleanSupplier bindParams(
        IntPredicate function,
        int boundParam1
    ) {
        return () -> function.test(boundParam1);
    }

    //#endregion

    //#region java.util.function.IntToIntFunction

    public static DoubleSupplier bindParams(
        IntToDoubleFunction function,
        int boundParam1
    ) {
        return () -> function.applyAsDouble(boundParam1);
    }

    //#endregion

    //#region java.util.function.IntToLongFunction

    public static LongSupplier bindParams(
        IntToLongFunction function,
        int boundParam1
    ) {
        return () -> function.applyAsLong(boundParam1);
    }

    //#endregion

    //#region java.util.function.IntUnaryOperator

    public static IntSupplier bindParams(
        IntUnaryOperator operator,
        int boundParam1
    ) {
        return () -> operator.applyAsInt(boundParam1);
    }

    //#endregion

    //#region java.util.function.LongBinaryOperator

    public static LongUnaryOperator bindParams(
        LongBinaryOperator operator,
        long boundParam1
    ) {
        return param2 -> operator.applyAsLong(boundParam1, param2);
    }

    public static LongSupplier bindParams(
        LongBinaryOperator operator,
        long boundParam1,
        long boundParam2
    ) {
        return () -> operator.applyAsLong(boundParam1, boundParam2);
    }

    public static LongUnaryOperator bindLastParams(
        LongBinaryOperator operator,
        long boundParam2
    ) {
        return param1 -> operator.applyAsLong(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.LongConsumer

    public static Runnable bindParams(
        LongConsumer consumer,
        long boundParam1
    ) {
        return () -> consumer.accept(boundParam1);
    }

    //#endregion

    //#region java.util.function.LongFunction

    public static <R> Supplier<R> bindParams(
        LongFunction<R> function,
        long boundParam1
    ) {
        return () -> function.apply(boundParam1);
    }

    //#endregion

    //#region java.util.function.LongPredicate

    public static BooleanSupplier bindParams(
        LongPredicate function,
        long boundParam1
    ) {
        return () -> function.test(boundParam1);
    }

    //#endregion

    //#region java.util.function.LongToLongFunction

    public static DoubleSupplier bindParams(
        LongToDoubleFunction function,
        long boundParam1
    ) {
        return () -> function.applyAsDouble(boundParam1);
    }

    //#endregion

    //#region java.util.function.LongToLongFunction

    public static IntSupplier bindParams(
        LongToIntFunction function,
        long boundParam1
    ) {
        return () -> function.applyAsInt(boundParam1);
    }

    //#endregion

    //#region java.util.function.LongUnaryOperator

    public static LongSupplier bindParams(
        LongUnaryOperator operator,
        long boundParam1
    ) {
        return () -> operator.applyAsLong(boundParam1);
    }

    //#endregion

    //#region java.util.function.ObjDoubleConsumer

    public static <T> DoubleConsumer bindParams(
        ObjDoubleConsumer<T> consumer,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return param2 -> consumer.accept(boundParam1, param2);
    }

    public static <T> Runnable bindParams(
        ObjDoubleConsumer<T> consumer,
        @Nonnull(when = UNKNOWN) T boundParam1,
        double boundParam2
    ) {
        return () -> consumer.accept(boundParam1, boundParam2);
    }

    public static <T> Consumer<T> bindLastParams(
        ObjDoubleConsumer<T> consumer,
        double boundParam2
    ) {
        return param1 -> consumer.accept(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.ObjDoubleConsumer

    public static <T> IntConsumer bindParams(
        ObjIntConsumer<T> consumer,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return param2 -> consumer.accept(boundParam1, param2);
    }

    public static <T> Runnable bindParams(
        ObjIntConsumer<T> consumer,
        @Nonnull(when = UNKNOWN) T boundParam1,
        int boundParam2
    ) {
        return () -> consumer.accept(boundParam1, boundParam2);
    }

    public static <T> Consumer<T> bindLastParams(
        ObjIntConsumer<T> consumer,
        int boundParam2
    ) {
        return param1 -> consumer.accept(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.ObjLongConsumer

    public static <T> LongConsumer bindParams(
        ObjLongConsumer<T> consumer,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return param2 -> consumer.accept(boundParam1, param2);
    }

    public static <T> Runnable bindParams(
        ObjLongConsumer<T> consumer,
        @Nonnull(when = UNKNOWN) T boundParam1,
        long boundParam2
    ) {
        return () -> consumer.accept(boundParam1, boundParam2);
    }

    public static <T> Consumer<T> bindLastParams(
        ObjLongConsumer<T> consumer,
        long boundParam2
    ) {
        return param1 -> consumer.accept(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.LongToLongFunction

    public static <T> BooleanSupplier bindParams(
        Predicate<T> predicate,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return () -> predicate.test(boundParam1);
    }

    //#endregion

    //#region java.util.function.ToDoubleBiFunction

    public static <T, U> ToDoubleFunction<U> bindParams(
        ToDoubleBiFunction<T, U> function,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return param2 -> function.applyAsDouble(boundParam1, param2);
    }

    public static <T, U> DoubleSupplier bindParams(
        ToDoubleBiFunction<T, U> function,
        @Nonnull(when = UNKNOWN) T boundParam1,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return () -> function.applyAsDouble(boundParam1, boundParam2);
    }

    public static <T, U> ToDoubleFunction<T> bindLastParams(
        ToDoubleBiFunction<T, U> function,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return param1 -> function.applyAsDouble(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.ToIntBiFunction

    public static <T, U> ToIntFunction<U> bindParams(
        ToIntBiFunction<T, U> function,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return param2 -> function.applyAsInt(boundParam1, param2);
    }

    public static <T, U> IntSupplier bindParams(
        ToIntBiFunction<T, U> function,
        @Nonnull(when = UNKNOWN) T boundParam1,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return () -> function.applyAsInt(boundParam1, boundParam2);
    }

    public static <T, U> ToIntFunction<T> bindLastParams(
        ToIntBiFunction<T, U> function,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return param1 -> function.applyAsInt(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.ToIntFunction

    public static <T> IntSupplier bindParams(
        ToIntFunction<T> function,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return () -> function.applyAsInt(boundParam1);
    }

    //#endregion

    //#region java.util.function.ToLongBiFunction

    public static <T, U> ToLongFunction<U> bindParams(
        ToLongBiFunction<T, U> function,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return param2 -> function.applyAsLong(boundParam1, param2);
    }

    public static <T, U> LongSupplier bindParams(
        ToLongBiFunction<T, U> function,
        @Nonnull(when = UNKNOWN) T boundParam1,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return () -> function.applyAsLong(boundParam1, boundParam2);
    }

    public static <T, U> ToLongFunction<T> bindLastParams(
        ToLongBiFunction<T, U> function,
        @Nonnull(when = UNKNOWN) U boundParam2
    ) {
        return param1 -> function.applyAsLong(param1, boundParam2);
    }

    //#endregion

    //#region java.util.function.ToLongFunction

    public static <T> LongSupplier bindParams(
        ToLongFunction<T> function,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return () -> function.applyAsLong(boundParam1);
    }

    //#endregion

    //#region java.util.function.Function

    public static <T> Supplier<T> bindParams(
        UnaryOperator<T> operator,
        @Nonnull(when = UNKNOWN) T boundParam1
    ) {
        return () -> operator.apply(boundParam1);
    }

    //#endregion

}
