package name.remal.gradleplugins.toolkit;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

public abstract class ArchiveWriter implements Closeable {

    public abstract void writeEntry(String entryName, byte[] bytes) throws IOException;

    public final void writeEntry(String entryName, String string, Charset charset) throws IOException {
        writeEntry(entryName, string.getBytes(charset));
    }

    public final void writeEntry(String entryName, String string) throws IOException {
        writeEntry(entryName, string, UTF_8);
    }

}
