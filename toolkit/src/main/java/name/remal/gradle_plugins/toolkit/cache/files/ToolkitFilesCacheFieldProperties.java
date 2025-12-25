package name.remal.gradle_plugins.toolkit.cache.files;

import static name.remal.gradle_plugins.toolkit.PropertiesUtils.loadProperties;
import static name.remal.gradle_plugins.toolkit.PropertiesUtils.storePropertiesToBytes;

import java.util.Properties;

public final class ToolkitFilesCacheFieldProperties extends ToolkitFilesCacheField<Properties> {

    public static ToolkitFilesCacheFieldProperties propertiesToolkitFilesCacheField(String id) {
        return new ToolkitFilesCacheFieldProperties(id);
    }

    private ToolkitFilesCacheFieldProperties(String id) {
        super(id, Properties.class);
    }

    @Override
    public byte[] serialize(Properties value) {
        return storePropertiesToBytes(value);
    }

    @Override
    public Properties deserialize(byte[] bytes) {
        return loadProperties(bytes);
    }

}
