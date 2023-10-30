package name.remal.gradle_plugins.toolkit.generators;

import java.util.function.Consumer;
import javax.annotation.Nullable;

public class BaseJavaFileContent<
    Child extends BaseJavaFileContent<Child>
    > extends JavaLikeScriptFileContent<Child, JavaFileContent> {

    protected static JavaFileContent newBlock(String blockHeading, Consumer<JavaFileContent> blockAction) {
        return new JavaFileContent().appendBlock(blockHeading, blockAction);
    }

    protected static JavaFileContent newBlock(String blockHeading, @Nullable Object... blockChunks) {
        return new JavaFileContent().appendBlock(blockHeading, blockChunks);
    }


    @Override
    protected final JavaFileContent newBlock() {
        return new JavaFileContent();
    }

}
