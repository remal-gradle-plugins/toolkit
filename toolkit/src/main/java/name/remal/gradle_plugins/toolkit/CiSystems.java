package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import com.google.auto.service.AutoService;
import java.io.File;
import lombok.NoArgsConstructor;
import lombok.val;

/**
 * See
 * <a href="https://beerandserversdontmix.com/2021/05/20/build-system-integration-with-environment-variables/">https://beerandserversdontmix.com/2021/05/20/build-system-integration-with-environment-variables/</a>.
 */
@NoArgsConstructor(access = PRIVATE)
abstract class CiSystems {


    /**
     * See
     * <a href="https://www.jenkins.io/doc/book/pipeline/jenkinsfile/#using-environment-variables">https://www.jenkins.io/doc/book/pipeline/jenkinsfile/#using-environment-variables</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemJenkins extends CiSystemBase {

        @Override
        public String getName() {
            return "Jenkins";
        }

        @Override
        public boolean isDetected() {
            return isNotEmptyEnv("JENKINS_URL");
        }

        @Override
        public File getBuildDirIfSupported() {
            return getRequiredFileEnv("WORKSPACE");
        }

    }


    /**
     * See
     * <a href="https://docs.travis-ci.com/user/environment-variables/#default-environment-variables">https://docs.travis-ci.com/user/environment-variables/#default-environment-variables</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemTravis extends CiSystemBase {

        @Override
        public String getName() {
            return "Travis CI";
        }

        @Override
        public boolean isDetected() {
            return getBooleanEnv("CI")
                && getBooleanEnv("TRAVIS");
        }

        @Override
        public File getBuildDirIfSupported() {
            return getRequiredFileEnv("TRAVIS_BUILD_DIR");
        }

    }


    /**
     * See
     * <a href="https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html">https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemAwsCodeBuild extends CiSystemBase {

        @Override
        public String getName() {
            return "AWS CodeBuild";
        }

        @Override
        public boolean isDetected() {
            return getBooleanEnv("CODEBUILD_CI");
        }

        @Override
        public File getBuildDirIfSupported() {
            return getRequiredFileEnv("CODEBUILD_SRC_DIR");
        }

    }


    /**
     * See
     * <a href="https://www.jetbrains.com/help/teamcity/2022.10/predefined-build-parameters.html">https://www.jetbrains.com/help/teamcity/2022.10/predefined-build-parameters.html</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemTeamcity extends CiSystemBase {

        @Override
        public String getName() {
            return "Teamcity";
        }

        @Override
        public boolean isDetected() {
            return isNotEmptyEnv("TEAMCITY_VERSION");
        }

    }


    /**
     * See
     * <a href="https://circleci.com/docs/variables/#built-in-environment-variables">https://circleci.com/docs/variables/#built-in-environment-variables</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemCircleCi extends CiSystemBase {

        @Override
        public String getName() {
            return "Circle CI";
        }

        @Override
        public boolean isDetected() {
            return getBooleanEnv("CI")
                && getBooleanEnv("CIRCLECI");
        }

        @Override
        public File getBuildDirIfSupported() {
            return getRequiredFileEnv("CIRCLE_WORKING_DIRECTORY");
        }

    }


    /**
     * See
     * <a href="https://docs.semaphoreci.com/ci-cd-environment/environment-variables/#semaphore-related">https://docs.semaphoreci.com/ci-cd-environment/environment-variables/#semaphore-related</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemSemaphoreCi extends CiSystemBase {

        @Override
        public String getName() {
            return "Semaphore CI";
        }

        @Override
        public boolean isDetected() {
            return getBooleanEnv("CI")
                && getBooleanEnv("SEMAPHORE");
        }

        @Override
        public File getBuildDirIfSupported() {
            return getRequiredFileEnv("SEMAPHORE_GIT_DIR");
        }

    }


    /**
     * See
     * <a href="https://docs.drone.io/pipeline/environment/reference/">https://docs.drone.io/pipeline/environment/reference/</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemDroneCi extends CiSystemBase {

        @Override
        public String getName() {
            return "Drone CI";
        }

        @Override
        public boolean isDetected() {
            return getBooleanEnv("CI")
                && getBooleanEnv("DRONE");
        }

        @Override
        public File getBuildDirIfSupported() {
            return getRequiredFileEnv("DRONE_WORKSPACE");
        }

    }


    /**
     * See
     * <a href="https://devcenter.heroku.com/articles/heroku-ci#immutable-environment-variables">https://devcenter.heroku.com/articles/heroku-ci#immutable-environment-variables</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemHeroku extends CiSystemBase {

        @Override
        public String getName() {
            return "Heroku";
        }

        @Override
        public boolean isDetected() {
            return getBooleanEnv("CI")
                && isNotEmptyEnv("HEROKU_TEST_RUN_ID");
        }

    }


    /**
     * See
     * <a href="https://www.appveyor.com/docs/environment-variables/">https://www.appveyor.com/docs/environment-variables/</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemAppveyorCi extends CiSystemBase {

        @Override
        public String getName() {
            return "Appveyor CI";
        }

        @Override
        public boolean isDetected() {
            return getBooleanEnv("CI")
                && getBooleanEnv("APPVEYOR");
        }

        @Override
        public File getBuildDirIfSupported() {
            return getRequiredFileEnv("APPVEYOR_BUILD_FOLDER");
        }

    }


    /**
     * See
     * <a href="https://docs.gitlab.com/ce/ci/variables/predefined_variables.html">https://docs.gitlab.com/ce/ci/variables/predefined_variables.html</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemGitLabCi extends CiSystemBase {

        @Override
        public String getName() {
            return "GitLab CI";
        }

        @Override
        public boolean isDetected() {
            return getBooleanEnv("CI")
                && getBooleanEnv("GITLAB_CI");
        }

        @Override
        public File getBuildDirIfSupported() {
            val buildDir = getRequiredFileEnv("CI_PROJECT_DIR");
            if (buildDir.isAbsolute()) {
                return buildDir;
            }

            val buildsDir = getRequiredFileEnv("CI_BUILDS_DIR");
            return new File(buildsDir, buildDir.getPath());
        }

    }


    /**
     * See
     * <a href="https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables">https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemGitHubActions extends CiSystemBase {

        @Override
        public String getName() {
            return "GitHub Actions";
        }

        @Override
        public boolean isDetected() {
            return getBooleanEnv("CI")
                && getBooleanEnv("GITHUB_ACTIONS");
        }

        @Override
        public File getBuildDirIfSupported() {
            return getRequiredFileEnv("GITHUB_WORKSPACE");
        }

    }


    /**
     * See
     * <a href="https://learn.microsoft.com/en-us/azure/devops/pipelines/build/variables?view=azure-devops">https://learn.microsoft.com/en-us/azure/devops/pipelines/build/variables?view=azure-devops</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemAzurePipelines extends CiSystemBase {

        @Override
        public String getName() {
            return "Azure Pipelines";
        }

        @Override
        public boolean isDetected() {
            return isNotEmptyEnv("SYSTEM_JOBID");
        }

        @Override
        public File getBuildDirIfSupported() {
            return getRequiredFileEnv("BUILD_REPOSITORY_LOCALPATH");
        }

    }


    /**
     * See
     * <a href="https://support.atlassian.com/bitbucket-cloud/docs/variables-and-secrets/#Default-variables">https://support.atlassian.com/bitbucket-cloud/docs/variables-and-secrets/#Default-variables</a>.
     */
    @AutoService(CiSystem.class)
    public static class CiSystemBitbucket extends CiSystemBase {

        @Override
        public String getName() {
            return "Bitbucket";
        }

        @Override
        public boolean isDetected() {
            return getBooleanEnv("CI")
                && isNotEmptyEnv("BITBUCKET_BUILD_NUMBER");
        }

        @Override
        public File getBuildDirIfSupported() {
            return getRequiredFileEnv("BITBUCKET_CLONE_DIR");
        }

    }


}
