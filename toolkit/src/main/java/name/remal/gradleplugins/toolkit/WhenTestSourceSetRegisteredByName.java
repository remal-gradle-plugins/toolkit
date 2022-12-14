package name.remal.gradleplugins.toolkit;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.getExtension;

import com.google.auto.service.AutoService;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

@AutoService(WhenTestSourceSetRegistered.class)
final class WhenTestSourceSetRegisteredByName implements WhenTestSourceSetRegistered {

    @Override
    public void registerAction(Project project, Action<SourceSet> action) {
        project.getPluginManager().withPlugin("java", __ -> {
            val sourceSets = getExtension(project, SourceSetContainer.class);
            sourceSets
                .matching(sourceSet -> {
                    val normalizedName = LOWER_CAMEL.to(LOWER_HYPHEN, sourceSet.getName());
                    return normalizedName.endsWith("-test")
                        || normalizedName.endsWith("-tests")
                        || normalizedName.endsWith("_test")
                        || normalizedName.endsWith("_tests")
                        ;
                })
                .all(action);
        });
    }

}
