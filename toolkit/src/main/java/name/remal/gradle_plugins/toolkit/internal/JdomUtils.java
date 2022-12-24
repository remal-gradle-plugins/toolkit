package name.remal.gradle_plugins.toolkit.internal;

import static lombok.AccessLevel.PRIVATE;
import static org.jdom2.Namespace.NO_NAMESPACE;
import static org.jdom2.input.sax.XMLReaders.NONVALIDATING;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.StringReader;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.NamespaceAware;
import org.jdom2.Parent;
import org.jdom2.input.SAXBuilder;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.xml.sax.InputSource;

@Internal
@NoArgsConstructor(access = PRIVATE)
public abstract class JdomUtils {

    public static SAXBuilder newNonValidatingSaxBuilder() {
        val saxBuilder = new SAXBuilder();
        saxBuilder.setXMLReaderFactory(NONVALIDATING);
        saxBuilder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        saxBuilder.setReuseParser(false);
        return saxBuilder;
    }

    @Contract("_ -> param1")
    @CanIgnoreReturnValue
    public static <T extends NamespaceAware> T withoutNamespaces(T node) {
        if (node instanceof Attribute) {
            ((Attribute) node).setNamespace(NO_NAMESPACE);

        } else if (node instanceof Element) {
            ((Element) node).setNamespace(NO_NAMESPACE);
        }

        if (node instanceof Parent) {
            for (val child : ((Parent) node).getContent()) {
                withoutNamespaces(child);
            }
        }

        if (node instanceof Element) {
            for (val child : ((Element) node).getAttributes()) {
                withoutNamespaces(child);
            }
        }

        return node;
    }

}
