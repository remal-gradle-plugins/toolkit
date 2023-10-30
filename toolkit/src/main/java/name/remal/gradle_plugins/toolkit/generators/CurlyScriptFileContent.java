package name.remal.gradle_plugins.toolkit.generators;

import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.StringUtils.indentString;

import lombok.val;

public abstract class CurlyScriptFileContent<
    Child extends CurlyScriptFileContent<Child, ?>,
    Block extends CurlyScriptFileContent<?, Block>
    > extends ScriptFileContent<Child, Block> {

    private static final int INDENT = 4;

    @Override
    protected final String composeBlock(String blockHeading, String blockContent) {
        val sb = new StringBuilder();
        if (isNotEmpty(blockHeading)) {
            sb.append(blockHeading).append(' ');
        }

        val indentedBlockContent = indentString(blockContent, INDENT);
        if (isNotEmpty(indentedBlockContent)) {
            sb.append("{\n")
                .append(indentedBlockContent)
                .append("\n}");
        } else {
            sb.append("{}");
        }

        return sb.toString();
    }

}
