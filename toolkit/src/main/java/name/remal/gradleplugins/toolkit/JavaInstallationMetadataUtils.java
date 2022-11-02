package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.gradle.api.JavaVersion;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.jetbrains.annotations.Contract;

@MinGradleVersion("6.7")
@NoArgsConstructor(access = PRIVATE)
public abstract class JavaInstallationMetadataUtils {

    @Nullable
    @Contract("null->null")
    @SuppressWarnings("ConstantConditions")
    public static JavaVersion getJavaInstallationVersionOf(@Nullable JavaInstallationMetadata metadata) {
        if (metadata == null) {
            return null;
        }

        JavaVersion javaVersion = Optional.ofNullable(metadata.getLanguageVersion())
            .map(JavaLanguageVersion::asInt)
            .map(JavaVersion::toVersion)
            .orElse(null);
        if (javaVersion != null) {
            return javaVersion;
        }

        return javaVersion;
    }

}
