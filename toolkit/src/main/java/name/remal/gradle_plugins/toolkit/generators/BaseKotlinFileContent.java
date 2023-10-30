package name.remal.gradle_plugins.toolkit.generators;

import java.util.function.Consumer;
import javax.annotation.Nullable;

public class BaseKotlinFileContent<
    Child extends BaseKotlinFileContent<Child>
    > extends JavaLikeScriptFileContent<Child, KotlinFileContent> {

    protected static KotlinFileContent newBlock(String blockHeading, Consumer<KotlinFileContent> blockAction) {
        return new KotlinFileContent().appendBlock(blockHeading, blockAction);
    }

    protected static KotlinFileContent newBlock(String blockHeading, @Nullable Object... blockChunks) {
        return new KotlinFileContent().appendBlock(blockHeading, blockChunks);
    }


    @Override
    protected final KotlinFileContent newBlock() {
        return new KotlinFileContent();
    }

    @Override
    protected final String getStatementDelimiter() {
        return "";
    }

    @Override
    protected String getStaticImportKeyword() {
        return "import";
    }

}
