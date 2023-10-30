package name.remal.gradle_plugins.toolkit.generators;

import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;

import com.google.common.reflect.TypeToken;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.ForOverride;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import java.lang.reflect.ParameterizedType;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Contract;

public abstract class ScriptFileContent<
    Child extends ScriptFileContent<Child, ?>,
    Block extends ScriptFileContent<?, Block>
    > extends TextFileContent<Child> {

    protected abstract String composeBlock(String blockHeading, String blockContent);


    @ForOverride
    @SneakyThrows
    @SuppressWarnings("unchecked")
    protected Block newBlock() {
        val thisType = TypeToken.of(this.getClass()).getSupertype(ScriptFileContent.class).getType();
        val typeArgument = ((ParameterizedType) thisType).getActualTypeArguments()[0];
        val blockClass = TypeToken.of(typeArgument).getRawType();
        return (Block) blockClass.getConstructor().newInstance();
    }

    @ForOverride
    @OverridingMethodsMustInvokeSuper
    protected void postProcessBlock(Block block) {
    }

    @Contract("_,_->this")
    @CanIgnoreReturnValue
    public final Child appendBlock(String blockHeading, Consumer<Block> blockAction) {
        append((Supplier<String>) () -> {
            val block = newBlock();
            blockAction.accept(block);
            postProcessBlock(block);
            val blockContent = block.getContent();
            return composeBlock(blockHeading, blockContent);
        });
        return getSelf();
    }

    @Contract("_,_->this")
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked")
    public final Child appendBlock(String blockHeading, @Nullable Object... blockChunks) {
        final Consumer<Block> blockAction;
        if (isEmpty(blockChunks)) {
            blockAction = __ -> { };
        } else if (blockChunks.length == 1 && blockChunks[0] instanceof Consumer<?>) {
            blockAction = (Consumer<Block>) blockChunks[0];
        } else {
            blockAction = block -> block.append(blockChunks);
        }

        return appendBlock(blockHeading, blockAction);
    }

}
