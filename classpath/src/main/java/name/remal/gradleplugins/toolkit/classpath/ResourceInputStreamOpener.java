package name.remal.gradleplugins.toolkit.classpath;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.File;
import java.io.InputStream;

@FunctionalInterface
public interface ResourceInputStreamOpener {

    /**
     * <p>Opens {@link InputStream} for the resource.</p>
     * <p>{@link InputStream} can be opened only once.</p>
     * <p>Can't be used outside of {@link ClassProcessor#process(File, String, ResourceInputStreamOpener)} and
     * {@link ResourceProcessor#process(File, String, ResourceInputStreamOpener)}.</p>
     */
    @MustBeClosed
    InputStream openStream();

}
