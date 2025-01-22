package name.remal.gradle_plugins.toolkit.xml;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.xml.XmlFormat.DEFAULT_XML_FORMAT;
import static name.remal.gradle_plugins.toolkit.xml.XmlUtils.prettyXmlString;

import lombok.NoArgsConstructor;
import org.gradle.api.XmlProvider;

@NoArgsConstructor(access = PRIVATE)
public abstract class XmlProviderUtils {

    public static void replaceXmlProviderContent(XmlProvider xmlProvider, String content) {
        var stringBuilder = xmlProvider.asString();
        stringBuilder.replace(0, stringBuilder.length(), content);
    }

    public static void prettyXmlProvider(XmlProvider xmlProvider) {
        prettyXmlProvider(xmlProvider, DEFAULT_XML_FORMAT);
    }

    public static void prettyXmlProvider(XmlProvider xmlProvider, XmlFormat format) {
        String xmlString = xmlProvider.asString().toString();
        xmlString = prettyXmlString(xmlString, format);
        replaceXmlProviderContent(xmlProvider, xmlString);
    }

}
