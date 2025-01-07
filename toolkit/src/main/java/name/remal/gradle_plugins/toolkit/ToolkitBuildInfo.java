package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.build_time_constants.api.BuildTimeConstants.getStringProperty;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
abstract class ToolkitBuildInfo {
    public static final String TOOLKIT_GROUP_ID = getStringProperty("project.group");
    public static final String TOOLKIT_ARTIFACT_IT = getStringProperty("project.name");
    public static final String TOOLKIT_VERSION = getStringProperty("project.version");
}
