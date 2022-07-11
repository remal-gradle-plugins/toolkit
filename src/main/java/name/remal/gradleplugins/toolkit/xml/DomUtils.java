package name.remal.gradleplugins.toolkit.xml;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import com.google.common.collect.ImmutableList;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

@NoArgsConstructor(access = PRIVATE)
public abstract class DomUtils {

    public static Document getNodeOwnerDocument(Node node) {
        if (node instanceof Document) {
            return (Document) node;
        }

        val document = node.getOwnerDocument();
        if (document == null) {
            throw new IllegalArgumentException("Node doesn't belong to a document: " + node);
        }
        return document;
    }

    /**
     * @return parent node
     */
    @Contract("_,_->param1")
    public static <T extends Node> T appendElement(
        T parentNode,
        String childElementName
    ) {
        return appendElement(parentNode, childElementName, __ -> { });
    }

    /**
     * @return crated child element
     */
    @Contract("_,_,_->param1")
    public static <T extends Node> T appendElement(
        T parentNode,
        String childElementName,
        Consumer<Element> childElementConfigurer
    ) {
        val childElement = getNodeOwnerDocument(parentNode).createElement(childElementName);
        childElementConfigurer.accept(childElement);
        parentNode.appendChild(childElement);
        return parentNode;
    }

    @Contract("_,_->param1")
    public static <T extends Node> T setNodeText(T parentNode, String text) {
        removeAllChildNodes(parentNode);
        val textNode = getNodeOwnerDocument(parentNode).createTextNode(text);
        parentNode.appendChild(textNode);
        return parentNode;
    }

    public static String getNodeText(Node node) {
        val sb = new StringBuilder();
        traverseNodeDescendants(node, child -> {
            if (child instanceof Text) {
                val text = (Text) child;
                sb.append(text.getWholeText());
            }
        });
        return sb.toString();
    }

    /**
     * Live modifiable list of node children
     */
    @SuppressWarnings("java:S3776")
    public static List<Node> getNodeChildren(Node node) {
        return new AbstractList<Node>() {
            @Override
            public Node get(int index) {
                if (index == 0) {
                    val firstChild = node.getFirstChild();
                    if (firstChild == null) {
                        throw new IndexOutOfBoundsException("Index 0 out of bounds for length 0");
                    }
                    return firstChild;
                }

                int size = size();
                if (index < 0 || index >= size) {
                    throw new IndexOutOfBoundsException(format("Index %d out of bounds for length %d", index, size));
                }

                return node.getChildNodes().item(index);
            }

            @Override
            @Nullable
            public Node set(int index, Node element) {
                int size = size();
                if (index < 0 || index > size) {
                    throw new IndexOutOfBoundsException(format("Index %d out of bounds for length %d", index, size));
                }

                if (index == size) {
                    node.appendChild(element);
                    return null;

                } else {
                    val oldChild = get(index);
                    if (element != oldChild) {
                        node.replaceChild(oldChild, element);
                    }
                    return oldChild;
                }
            }

            @Override
            public void add(int index, Node element) {
                int size = size();
                if (index < 0 || index > size) {
                    throw new IndexOutOfBoundsException(format("Index %d out of bounds for length %d", index, size));
                }

                if (index == size) {
                    node.appendChild(element);

                } else {
                    val refChild = get(index);
                    node.insertBefore(element, refChild);
                }
            }

            @Override
            public Node remove(int index) {
                val child = get(index);
                node.removeChild(child);
                return child;
            }

            @Override
            public int size() {
                return node.getChildNodes().getLength();
            }

            @Override
            public boolean isEmpty() {
                return node.getFirstChild() == null;
            }

            /**
             * The standard implementation is broken with {@link NodeList}. Let's use the simplest algorithm -
             * <a hrefr="https://www.geeksforgeeks.org/bubble-sort/">bubble sort</a>, as this algorithm uses a simple
             * swapping, that is easy to implement with the DOM model.
             */
            @Override
            public void sort(@Nullable Comparator<? super Node> comparator) {
                if (comparator == null) {
                    throw new IllegalArgumentException(
                        "Parameter comparator can't be null, as natural ordering is not supported for DOM nodes"
                    );
                }

                val childNodes = node.getChildNodes();
                val size = childNodes.getLength();
                if (size <= 1) {
                    return;
                }

                for (int i = 0; i < size - 1; ++i) {
                    boolean swapped = false;
                    for (int j = 0; j < size - i - 1; ++j) {
                        val node1 = childNodes.item(j);
                        val node2 = childNodes.item(j + 1);
                        if (comparator.compare(node1, node2) > 0) {
                            node.insertBefore(node2, node1);
                            swapped = true;
                        }
                    }
                    if (!swapped) {
                        break;
                    }
                }
            }
        };
    }

    /**
     * Get all a parent's descendants (all children at any level below the parent - excludes the parent itself).
     */
    @Unmodifiable
    public static List<Node> getNodeDescendants(Node node) {
        List<Node> result = new ArrayList<>();
        traverseNodeDescendants(node, result::add);
        return ImmutableList.copyOf(result);
    }

    /**
     * Get all a parent's descendants (all children at any level below the parent - excludes the parent itself).
     */
    @Unmodifiable
    public static <T extends Node> List<T> getNodeDescendants(Node node, Class<T> childNodeType) {
        List<T> result = new ArrayList<>();
        traverseNodeDescendants(node, childNodeType, result::add);
        return ImmutableList.copyOf(result);
    }

    /**
     * Traverse all a parent's descendants (all children at any level below the parent - excludes the parent itself).
     */
    public static void traverseNodeDescendants(Node parentNode, Consumer<Node> action) {
        val children = parentNode.getChildNodes();
        for (int index = 0; index < children.getLength(); ++index) {
            val child = children.item(index);
            action.accept(child);
            traverseNodeDescendants(child, action);
        }
    }

    /**
     * Traverse all a parent's descendants (all children at any level below the parent - excludes the parent itself).
     */
    public static <T extends Node> void traverseNodeDescendants(
        Node parentNode,
        Class<T> childNodeType,
        Consumer<T> action
    ) {
        traverseNodeDescendants(parentNode, child -> {
            if (childNodeType.isInstance(child)) {
                val typedChild = childNodeType.cast(child);
                action.accept(typedChild);
            }
        });
    }

    @Contract("_->param1")
    public static <T extends Node> T detachNode(T node) {
        val parentNode = node.getParentNode();
        if (parentNode != null) {
            parentNode.removeChild(node);
        }
        return node;
    }

    @Contract("_->param1")
    public static <T extends Node> T removeAllChildNodes(T parentNode) {
        Node child;
        while ((child = parentNode.getFirstChild()) != null) {
            parentNode.removeChild(child);
        }
        return parentNode;
    }

}
