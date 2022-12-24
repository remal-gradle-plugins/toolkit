package name.remal.gradle_plugins.toolkit;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class CrossCompileServiceImpl {

    String className;

    CrossCompileServiceDependencyVersion dependencyVersion;

    public boolean isFallback() {
        return getDependencyVersion().getVersion() == null;
    }

    @Override
    public String toString() {
        return getClassName()
            + " ("
            + "dependency=" + getDependencyVersion().getDependency()
            + ", version=" + getDependencyVersion().getVersion()
            + ", earlierIncluded=" + getDependencyVersion().isEarlierIncluded()
            + ", selfIncluded=" + getDependencyVersion().isSelfIncluded()
            + ", laterIncluded=" + getDependencyVersion().isLaterIncluded()
            + ')';
    }

}
