package name.remal.gradle_plugins.toolkit.git;

import name.remal.gradle_plugins.toolkit.git.ImmutableGitBooleanAttribute.GitBooleanAttributeBuilder;
import org.immutables.value.Value;

@Value.Immutable
public interface GitBooleanAttribute extends GitAttribute {

    static GitBooleanAttributeBuilder newGitBooleanAttributeBuilder() {
        return ImmutableGitBooleanAttribute.builder();
    }


    boolean isSet();

}
