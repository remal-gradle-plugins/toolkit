package name.remal.gradle_plugins.toolkit.generators;

import static java.util.stream.Collectors.joining;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.ForOverride;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.val;
import org.jetbrains.annotations.Contract;

public abstract class JavaLikeScriptFileContent<
    Child extends JavaLikeScriptFileContent<Child, ?>,
    Block extends JavaLikeScriptFileContent<?, Block>
    > extends CurlyScriptFileContent<Child, Block> {

    @ForOverride
    protected String getStatementDelimiter() {
        return ";";
    }


    @ForOverride
    protected String getPackageKeyword() {
        return "package";
    }

    @Nullable
    private String packageName;

    @Nullable
    public final String getPackageName() {
        return packageName;
    }

    public final void setPackageName(@Nullable String packageName) {
        onPackageNameChanged(this.packageName, packageName);
        this.packageName = packageName;
    }

    @ForOverride
    @OverridingMethodsMustInvokeSuper
    protected void onPackageNameChanged(@Nullable String oldPackageName, @Nullable String newPackageName) {
    }

    {
        chunks.add((Supplier<String>) () -> {
            val packageName = getPackageName();
            if (isNotEmpty(packageName)) {
                return getPackageKeyword() + " " + packageName + getStatementDelimiter() + "\n\n";
            }
            return null;
        });
    }


    @ForOverride
    protected String getStaticImportKeyword() {
        return "import static";
    }

    private final Set<String> staticImports = new TreeSet<>();

    public final Set<String> getStaticImports() {
        return staticImports;
    }

    @Contract("_,_->this")
    @CanIgnoreReturnValue
    public final Child addStaticImport(Class<?> clazz, String member) {
        return addStaticImport(clazz.getCanonicalName(), member);
    }

    @Contract("_,_->this")
    @CanIgnoreReturnValue
    public final Child addStaticImport(String className, String member) {
        staticImports.add(className + '.' + member);
        return getSelf();
    }

    {
        chunks.add((Supplier<String>) () -> {
            val staticImports = getStaticImports();
            if (isNotEmpty(staticImports)) {
                return staticImports.stream()
                    .map(it -> getStaticImportKeyword() + " " + it + getStatementDelimiter())
                    .collect(joining("\n", "", "\n"));
            }
            return null;
        });
    }


    @ForOverride
    protected String getImportKeyword() {
        return "import";
    }

    private final Set<String> imports = new TreeSet<>();

    public final Set<String> getImports() {
        return imports;
    }

    @Contract("_->this")
    @CanIgnoreReturnValue
    public final Child addImport(Class<?> clazz) {
        return addImport(clazz.getCanonicalName());
    }

    @Contract("_->this")
    @CanIgnoreReturnValue
    public final Child addImport(String className) {
        imports.add(className);
        return getSelf();
    }

    {
        chunks.add((Supplier<String>) () -> {
            val imports = getImports();
            if (isNotEmpty(imports)) {
                return imports.stream()
                    .map(it -> getImportKeyword() + " " + it + getStatementDelimiter())
                    .collect(joining("\n", "", "\n"));
            }
            return null;
        });
    }


    @Override
    @OverridingMethodsMustInvokeSuper
    protected void postProcessBlock(Block block) {
        super.postProcessBlock(block);
        Optional.ofNullable(block.getPackageName()).ifPresent(this::setPackageName);
        getStaticImports().addAll(block.getStaticImports());
        getImports().addAll(block.getImports());
    }

}
