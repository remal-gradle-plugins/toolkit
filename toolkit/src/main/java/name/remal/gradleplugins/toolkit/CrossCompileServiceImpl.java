package name.remal.gradleplugins.toolkit;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class CrossCompileServiceImpl {

    String className;

    CrossCompileServiceDependencyVersion dependencyVersion;

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
