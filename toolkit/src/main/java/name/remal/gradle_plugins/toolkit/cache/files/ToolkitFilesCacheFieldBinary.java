package name.remal.gradle_plugins.toolkit.cache.files;

public final class ToolkitFilesCacheFieldBinary extends ToolkitFilesCacheField<byte[]> {

    public static ToolkitFilesCacheFieldBinary binaryToolkitFilesCacheField(String id) {
        return new ToolkitFilesCacheFieldBinary(id);
    }

    private ToolkitFilesCacheFieldBinary(String id) {
        super(id, byte[].class);
    }

    @Override
    public byte[] serialize(byte[] value) {
        return value;
    }

    @Override
    public byte[] deserialize(byte[] bytes) {
        return bytes;
    }

}
