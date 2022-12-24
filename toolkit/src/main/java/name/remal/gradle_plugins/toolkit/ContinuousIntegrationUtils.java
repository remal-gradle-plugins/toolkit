package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;

import java.util.stream.Stream;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class ContinuousIntegrationUtils {

    @SuppressWarnings({"SimplifyStreamApiCallChains", "java:S4034"})
    public static boolean isRunningOnCi() {
        return Stream.of(
                // General
                "CI",

                // Jenkins CI
                "JENKINS_HOME",
                "JENKINS_URL",

                // AWS Codebuild CI
                "CODEBUILD_CI",

                // Codefresh CI
                "CF_BUILD_URL",
                "CF_BUILD_ID",

                // TeamCity CI
                "TEAMCITY_VERSION",

                // Circle CI
                "CIRCLECI",

                // Bitrise CI
                "BITRISE_IO",

                // Buildkite CI
                "BUILDKITE",

                // Heroku CI
                "HEROKU_TEST_RUN_BRANCH",

                // Wercker CI
                "WERCKER_GIT_BRANCH",

                // GitLab CI
                "GITLAB_CI",

                // GitHub Actions
                "GITHUB_ACTIONS",

                // Azure Pipelines
                "SYSTEM_TEAMFOUNDATIONSERVERURI",

                // Bitbucket Pipelines
                "BITBUCKET_BUILD_NUMBER",

                // Cirrus CI
                "CIRRUS_CI"
            )
            .map(System::getenv)
            .filter(ObjectUtils::isNotEmpty)
            .filter(not("0"::equals))
            .filter(not("false"::equalsIgnoreCase))
            .findAny()
            .isPresent();
    }

    public static boolean isNotRunningOnCi() {
        return !isRunningOnCi();
    }

}
