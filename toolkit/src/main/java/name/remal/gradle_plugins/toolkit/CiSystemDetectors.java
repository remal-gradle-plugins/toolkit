package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ConfigurationCacheSafeSystem.getConfigurationCacheSafeBooleanEnv;
import static name.remal.gradle_plugins.toolkit.ConfigurationCacheSafeSystem.getConfigurationCacheSafeRequiredFileEnv;
import static name.remal.gradle_plugins.toolkit.ConfigurationCacheSafeSystem.isConfigurationCacheSafeNotEmptyEnv;

import com.google.auto.service.AutoService;
import java.io.File;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnExternalDependency;
import org.jspecify.annotations.Nullable;

/**
 * See
 * <a href="https://beerandserversdontmix.com/2021/05/20/build-system-integration-with-environment-variables/">https://beerandserversdontmix.com/2021/05/20/build-system-integration-with-environment-variables/</a>.
 */
@ReliesOnExternalDependency
@NoArgsConstructor(access = PRIVATE)
abstract class CiSystemDetectors {

    /**
     * See
     * <a href="https://www.jenkins.io/doc/book/pipeline/jenkinsfile/#using-environment-variables">https://www.jenkins.io/doc/book/pipeline/jenkinsfile/#using-environment-variables</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class Jenkins implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (isConfigurationCacheSafeNotEmptyEnv("JENKINS_URL")) {
                return ImmutableCiSystem.builder()
                    .name("Jenkins")
                    .buildDir(getConfigurationCacheSafeRequiredFileEnv("WORKSPACE"))
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://docs.travis-ci.com/user/environment-variables/#default-environment-variables">https://docs.travis-ci.com/user/environment-variables/#default-environment-variables</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class Travis implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (getConfigurationCacheSafeBooleanEnv("CI")
                || getConfigurationCacheSafeBooleanEnv("TRAVIS")
            ) {
                return ImmutableCiSystem.builder()
                    .name("Travis CI")
                    .buildDir(getConfigurationCacheSafeRequiredFileEnv("TRAVIS_BUILD_DIR"))
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html">https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class AwsCodeBuild implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (getConfigurationCacheSafeBooleanEnv("CODEBUILD_CI")) {
                return ImmutableCiSystem.builder()
                    .name("AWS CodeBuild")
                    .buildDir(getConfigurationCacheSafeRequiredFileEnv("CODEBUILD_SRC_DIR"))
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://www.jetbrains.com/help/teamcity/2022.10/predefined-build-parameters.html">https://www.jetbrains.com/help/teamcity/2022.10/predefined-build-parameters.html</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class Teamcity implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (isConfigurationCacheSafeNotEmptyEnv("TEAMCITY_VERSION")) {
                return ImmutableCiSystem.builder()
                    .name("Teamcity")
                    .buildDir((File) null) // no way to determine build dir
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://circleci.com/docs/variables/#built-in-environment-variables">https://circleci.com/docs/variables/#built-in-environment-variables</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class CircleCi implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (getConfigurationCacheSafeBooleanEnv("CIRCLECI")) {
                return ImmutableCiSystem.builder()
                    .name("Circle CI")
                    .buildDir(getConfigurationCacheSafeRequiredFileEnv("CIRCLE_WORKING_DIRECTORY"))
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://docs.semaphoreci.com/ci-cd-environment/environment-variables/#semaphore-related">https://docs.semaphoreci.com/ci-cd-environment/environment-variables/#semaphore-related</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class SemaphoreCi implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (getConfigurationCacheSafeBooleanEnv("CI")
                && getConfigurationCacheSafeBooleanEnv("SEMAPHORE")
            ) {
                return ImmutableCiSystem.builder()
                    .name("Semaphore CI")
                    .buildDir(getConfigurationCacheSafeRequiredFileEnv("SEMAPHORE_GIT_DIR"))
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://docs.drone.io/pipeline/environment/reference/">https://docs.drone.io/pipeline/environment/reference/</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class DroneCi implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (isConfigurationCacheSafeNotEmptyEnv("CI")
                && getConfigurationCacheSafeBooleanEnv("DRONE")
            ) {
                return ImmutableCiSystem.builder()
                    .name("Drone CI")
                    .buildDir(getConfigurationCacheSafeRequiredFileEnv("DRONE_WORKSPACE"))
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://devcenter.heroku.com/articles/heroku-ci#immutable-environment-variables">https://devcenter.heroku.com/articles/heroku-ci#immutable-environment-variables</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class Heroku implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (isConfigurationCacheSafeNotEmptyEnv("HEROKU_TEST_RUN_ID")) {
                return ImmutableCiSystem.builder()
                    .name("Heroku")
                    .buildDir((File) null) // no way to determine build dir
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://www.appveyor.com/docs/environment-variables/">https://www.appveyor.com/docs/environment-variables/</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class AppveyorCi implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (getConfigurationCacheSafeBooleanEnv("CI")
                && getConfigurationCacheSafeBooleanEnv("APPVEYOR")
            ) {
                return ImmutableCiSystem.builder()
                    .name("Appveyor CI")
                    .buildDir(getConfigurationCacheSafeRequiredFileEnv("APPVEYOR_BUILD_FOLDER"))
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://docs.gitlab.com/ce/ci/variables/predefined_variables.html">https://docs.gitlab.com/ce/ci/variables/predefined_variables.html</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class GitLabCi implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (getConfigurationCacheSafeBooleanEnv("GITLAB_CI")) {
                var buildDir = getConfigurationCacheSafeRequiredFileEnv("CI_PROJECT_DIR");
                if (!buildDir.isAbsolute()) {
                    var buildsDir = getConfigurationCacheSafeRequiredFileEnv("CI_BUILDS_DIR");
                    buildDir = new File(buildsDir, buildDir.getPath());
                }
                return ImmutableCiSystem.builder()
                    .name("GitLab CI")
                    .buildDir(buildDir)
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables">https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class GitHubActions implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (getConfigurationCacheSafeBooleanEnv("CI")
                || getConfigurationCacheSafeBooleanEnv("GITHUB_ACTIONS")
            ) {
                return ImmutableCiSystem.builder()
                    .name("GitHub Actions")
                    .buildDir(getConfigurationCacheSafeRequiredFileEnv("GITHUB_WORKSPACE"))
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://learn.microsoft.com/en-us/azure/devops/pipelines/build/variables?view=azure-devops">https://learn.microsoft.com/en-us/azure/devops/pipelines/build/variables?view=azure-devops</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class AzurePipelines implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (isConfigurationCacheSafeNotEmptyEnv("SYSTEM_TEAMFOUNDATIONSERVERURI")) {
                return ImmutableCiSystem.builder()
                    .name("Azure Pipelines")
                    .buildDir(getConfigurationCacheSafeRequiredFileEnv("BUILD_REPOSITORY_LOCALPATH"))
                    .build();
            }
            return null;
        }
    }

    /**
     * See
     * <a href="https://support.atlassian.com/bitbucket-cloud/docs/variables-and-secrets/#Default-variables">https://support.atlassian.com/bitbucket-cloud/docs/variables-and-secrets/#Default-variables</a>.
     */
    @AutoService(CiSystemDetector.class)
    public static class Bitbucket implements CiSystemDetector {
        @Override
        @Nullable
        public CiSystem detect() {
            if (getConfigurationCacheSafeBooleanEnv("CI")
                || isConfigurationCacheSafeNotEmptyEnv("BITBUCKET_BUILD_NUMBER")
            ) {
                return ImmutableCiSystem.builder()
                    .name("Bitbucket")
                    .buildDir(getConfigurationCacheSafeRequiredFileEnv("BITBUCKET_CLONE_DIR"))
                    .build();
            }
            return null;
        }
    }

}
