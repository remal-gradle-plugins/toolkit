package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class TypeCasts {

    @Contract(pure = true)
    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

}
