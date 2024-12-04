package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class TypeCasts {

    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

}
