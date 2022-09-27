package name.remal.gradleplugins.toolkit.issues;

import static name.remal.gradleplugins.toolkit.issues.Utils.compareOptionals;

import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.gradleplugins.toolkit.issues.ImmutableIssue.IssueBuilder;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.immutables.value.Value.Check;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@Value.Immutable
@Gson.TypeAdapters
public interface Issue extends Comparable<Issue> {

    static IssueBuilder newIssueBuilder() {
        return ImmutableIssue.builder();
    }

    static Issue newIssue(Consumer<IssueBuilder> configurer) {
        val builder = newIssueBuilder();
        configurer.accept(builder);
        return builder.build();
    }


    File getSourceFile();

    Message getMessage();

    @Nullable
    IssueSeverity getSeverity();

    @Nullable
    String getRule();

    @Nullable
    String getCategory();

    @Nullable
    Integer getStartLine();

    @Nullable
    Integer getStartColumn();

    @Nullable
    Integer getEndLine();

    @Nullable
    Integer getEndColumn();

    @Nullable
    String getConsistentId();

    @Nullable
    Message getDescription();


    @OverrideOnly
    @Check
    @SuppressWarnings("java:S3776")
    default void validate() {
        if (getStartColumn() != null && getStartLine() == null) {
            throw new IllegalStateException("Property startColumn can't be set without setting startLine");
        }
        if (getEndLine() != null && getStartLine() == null) {
            throw new IllegalStateException("Property endLine can't be set without setting startLine");
        }
        if (getEndColumn() != null && getEndLine() == null) {
            throw new IllegalStateException("Property endColumn can't be set without setting endLine");
        }

        if (getStartLine() != null && getStartLine() < 0) {
            throw new IllegalStateException("Property startLine can't be less than 0");
        }
        if (getStartColumn() != null && getStartColumn() < 0) {
            throw new IllegalStateException("Property startColumn can't be less than 0");
        }
        if (getEndLine() != null && getEndLine() < 0) {
            throw new IllegalStateException("Property endLine can't be less than 0");
        }
        if (getEndColumn() != null && getEndColumn() < 0) {
            throw new IllegalStateException("Property endColumn can't be less than 0");
        }

        if (getStartLine() != null && getEndLine() != null) {
            if (getStartLine() > getEndLine()) {
                throw new IllegalStateException("Property startLine can't be greater than endLine");
            }
            if (Objects.equals(getStartLine(), getEndLine())
                && compareOptionals(getStartColumn(), getEndColumn()) > 0
            ) {
                throw new IllegalStateException(
                    "Property startColumn can't be greater than endColumn if startLine equals to endLine"
                );
            }
        }
    }


    @Override
    default int compareTo(Issue other) {
        int result = compareOptionals(getSeverity(), other.getSeverity());
        if (result == 0) {
            result = getSourceFile().compareTo(other.getSourceFile());
        }
        if (result == 0) {
            result = compareOptionals(getStartLine(), other.getStartLine());
        }
        if (result == 0) {
            result = compareOptionals(getStartColumn(), other.getStartColumn());
        }
        if (result == 0) {
            result = -1 * compareOptionals(getEndLine(), other.getEndLine());
        }
        if (result == 0) {
            result = -1 * compareOptionals(getEndColumn(), other.getEndColumn());
        }
        if (result == 0) {
            result = compareOptionals(getCategory(), other.getCategory());
        }
        if (result == 0) {
            result = compareOptionals(getRule(), other.getRule());
        }
        return result;
    }

}
