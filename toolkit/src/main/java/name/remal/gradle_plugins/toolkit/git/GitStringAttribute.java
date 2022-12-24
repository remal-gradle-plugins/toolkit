package name.remal.gradle_plugins.toolkit.git;

import name.remal.gradle_plugins.toolkit.git.ImmutableGitStringAttribute.GitStringAttributeBuilder;
import org.immutables.value.Value;

@Value.Immutable
public interface GitStringAttribute extends GitAttribute {

    static GitStringAttributeBuilder newGitStringAttributeBuilder() {
        return ImmutableGitStringAttribute.builder();
    }


    String getValue();

}
