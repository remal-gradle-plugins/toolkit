package name.remal.gradleplugins.toolkit.testkit.functional;

import java.io.File;
import java.util.function.Consumer;
import lombok.Getter;
import org.jetbrains.annotations.Contract;

@Getter
abstract class BaseGradleProject<Child extends BaseGradleProject<Child>> {

    protected final File projectDir;
    protected final BuildFile buildFile;

    BaseGradleProject(File projectDir) {
        this.projectDir = projectDir.getAbsoluteFile();
        this.buildFile = new BuildFile(this.projectDir);
    }

    public final String getName() {
        return projectDir.getName();
    }

    @Override
    public final String toString() {
        return getName();
    }

    @Contract("_ -> this")
    @SuppressWarnings("unchecked")
    public final Child forBuildFile(Consumer<BuildFile> buildFileConsumer) {
        buildFileConsumer.accept(this.buildFile);
        return (Child) this;
    }

}
