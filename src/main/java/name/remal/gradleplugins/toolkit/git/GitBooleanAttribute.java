package name.remal.gradleplugins.toolkit.git;

import name.remal.gradleplugins.toolkit.git.ImmutableGitBooleanAttribute.GitBooleanAttributeBuilder;
import org.immutables.value.Value;

@Value.Immutable
public interface GitBooleanAttribute extends GitAttribute {

    static GitBooleanAttributeBuilder newGitBooleanAttributeBuilder() {
        return ImmutableGitBooleanAttribute.builder();
    }


    boolean isSet();

}
