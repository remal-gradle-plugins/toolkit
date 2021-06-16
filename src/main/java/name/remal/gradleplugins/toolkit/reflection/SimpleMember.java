package name.remal.gradleplugins.toolkit.reflection;

import static java.lang.reflect.Modifier.isPublic;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import org.jetbrains.annotations.Contract;

abstract class SimpleMember {

    @Contract("_ -> param1")
    @SuppressWarnings("deprecation")
    protected static <T extends AccessibleObject & Member> T makeAccessible(T member) {
        if (!member.isAccessible()) {
            if (!isPublic(member.getModifiers())
                || !isPublic(member.getDeclaringClass().getModifiers())
            ) {
                member.setAccessible(true);
            }
        }
        return member;
    }

}
