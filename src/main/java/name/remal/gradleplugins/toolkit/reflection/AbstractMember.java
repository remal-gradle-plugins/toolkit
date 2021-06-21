package name.remal.gradleplugins.toolkit.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import org.jetbrains.annotations.Contract;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
abstract class AbstractMember {

    protected static boolean isPublic(Class<?> type) {
        return Modifier.isPublic(type.getModifiers());
    }

    protected static boolean isPublic(Member member) {
        return Modifier.isPublic(member.getModifiers());
    }

    protected static boolean isNotPublic(Class<?> type) {
        return !isPublic(type);
    }

    protected static boolean isNotPublic(Member member) {
        return !isPublic(member);
    }


    protected static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    protected static boolean isNotStatic(Member member) {
        return !isStatic(member);
    }


    @Contract("_ -> param1")
    @SuppressWarnings("deprecation")
    protected static <T extends AccessibleObject & Member> T makeAccessible(T member) {
        if (!member.isAccessible()) {
            if (!isNotPublic(member)
                || !isNotPublic(member.getDeclaringClass())
            ) {
                member.setAccessible(true);
            }
        }
        return member;
    }

}
