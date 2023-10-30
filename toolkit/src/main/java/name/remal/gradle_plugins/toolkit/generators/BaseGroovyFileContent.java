package name.remal.gradle_plugins.toolkit.generators;

import java.util.function.Consumer;
import javax.annotation.Nullable;

public class BaseGroovyFileContent<
    Child extends BaseGroovyFileContent<Child>
    > extends JavaLikeScriptFileContent<Child, GroovyFileContent> {

    protected static GroovyFileContent newBlock(String blockHeading, Consumer<GroovyFileContent> blockAction) {
        return new GroovyFileContent().appendBlock(blockHeading, blockAction);
    }

    protected static GroovyFileContent newBlock(String blockHeading, @Nullable Object... blockChunks) {
        return new GroovyFileContent().appendBlock(blockHeading, blockChunks);
    }


    @Override
    protected final GroovyFileContent newBlock() {
        return new GroovyFileContent();
    }

    @Override
    protected final String getStatementDelimiter() {
        return "";
    }

}
