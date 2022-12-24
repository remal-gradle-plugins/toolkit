package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.StringUtils.indentString;
import static name.remal.gradle_plugins.toolkit.StringUtils.substringAfter;
import static name.remal.gradle_plugins.toolkit.StringUtils.substringAfterLast;
import static name.remal.gradle_plugins.toolkit.StringUtils.substringBefore;
import static name.remal.gradle_plugins.toolkit.StringUtils.substringBeforeLast;
import static name.remal.gradle_plugins.toolkit.StringUtils.trimLeftWith;
import static name.remal.gradle_plugins.toolkit.StringUtils.trimRightWith;
import static name.remal.gradle_plugins.toolkit.StringUtils.trimWith;

import java.util.function.Function;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.StringUtils.CharPredicate;

@NoArgsConstructor(access = PRIVATE)
public abstract class FunctionUtils {

    public static Function<String, String> toIndentedString(int indent) {
        return string -> indentString(string, indent);
    }


    public static Function<String, String> toSubstringedBefore(String stringToFind) {
        if (stringToFind.isEmpty()) {
            throw new IllegalArgumentException("stringToFind must not be empty");
        }
        return string -> substringBefore(string, stringToFind);
    }

    public static Function<String, String> toSubstringedBefore(String stringToFind, String missingString) {
        if (stringToFind.isEmpty()) {
            throw new IllegalArgumentException("stringToFind must not be empty");
        }
        return string -> substringBefore(string, stringToFind, missingString);
    }

    public static Function<String, String> toSubstringedBeforeLast(String stringToFind) {
        if (stringToFind.isEmpty()) {
            throw new IllegalArgumentException("stringToFind must not be empty");
        }
        return string -> substringBeforeLast(string, stringToFind);
    }

    public static Function<String, String> toSubstringedBeforeLast(String stringToFind, String missingString) {
        if (stringToFind.isEmpty()) {
            throw new IllegalArgumentException("stringToFind must not be empty");
        }
        return string -> substringBeforeLast(string, stringToFind, missingString);
    }

    public static Function<String, String> toSubstringedAfter(String stringToFind) {
        if (stringToFind.isEmpty()) {
            throw new IllegalArgumentException("stringToFind must not be empty");
        }
        return string -> substringAfter(string, stringToFind);
    }

    public static Function<String, String> toSubstringedAfter(String stringToFind, String missingString) {
        if (stringToFind.isEmpty()) {
            throw new IllegalArgumentException("stringToFind must not be empty");
        }
        return string -> substringAfter(string, stringToFind, missingString);
    }

    public static Function<String, String> toSubstringedAfterLast(String stringToFind) {
        if (stringToFind.isEmpty()) {
            throw new IllegalArgumentException("stringToFind must not be empty");
        }
        return string -> substringAfterLast(string, stringToFind);
    }

    public static Function<String, String> toSubstringedAfterLast(String stringToFind, String missingString) {
        if (stringToFind.isEmpty()) {
            throw new IllegalArgumentException("stringToFind must not be empty");
        }
        return string -> substringAfterLast(string, stringToFind, missingString);
    }


    public static Function<String, String> toTrimmedWith(CharPredicate charPredicate) {
        return string -> trimWith(string, charPredicate);
    }

    public static Function<String, String> toTrimmedWith(char... charsToRemove) {
        return string -> trimWith(string, charsToRemove);
    }

    public static Function<String, String> toTrimmedWith(CharSequence charsToRemove) {
        return string -> trimWith(string, charsToRemove);
    }

    public static Function<String, String> toTrimmedLeftWith(CharPredicate charPredicate) {
        return string -> trimLeftWith(string, charPredicate);
    }

    public static Function<String, String> toTrimmedLeftWith(char... charsToRemove) {
        return string -> trimLeftWith(string, charsToRemove);
    }

    public static Function<String, String> toTrimmedLeftWith(CharSequence charsToRemove) {
        return string -> trimLeftWith(string, charsToRemove);
    }

    public static Function<String, String> toTrimmedRightWith(CharPredicate charPredicate) {
        return string -> trimRightWith(string, charPredicate);
    }

    public static Function<String, String> toTrimmedRightWith(char... charsToRemove) {
        return string -> trimRightWith(string, charsToRemove);
    }

    public static Function<String, String> toTrimmedRightWith(CharSequence charsToRemove) {
        return string -> trimRightWith(string, charsToRemove);
    }

}
