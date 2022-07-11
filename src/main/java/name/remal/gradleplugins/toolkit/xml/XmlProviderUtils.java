package name.remal.gradleplugins.toolkit.xml;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.xml.XmlFormat.DEFAULT_XML_FORMAT;
import static name.remal.gradleplugins.toolkit.xml.XmlUtils.prettyXmlString;

import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.XmlProvider;

@NoArgsConstructor(access = PRIVATE)
public abstract class XmlProviderUtils {

    public static void replaceXmlProviderContent(XmlProvider xmlProvider, String content) {
        val stringBuilder = xmlProvider.asString();
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
