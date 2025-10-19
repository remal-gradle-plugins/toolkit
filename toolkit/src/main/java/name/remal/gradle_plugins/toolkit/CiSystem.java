package name.remal.gradle_plugins.toolkit;

import java.io.File;
import java.io.Serializable;
import java.util.Optional;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
public interface CiSystem extends Serializable {

    String getName();

    Optional<File> getBuildDir();

}
