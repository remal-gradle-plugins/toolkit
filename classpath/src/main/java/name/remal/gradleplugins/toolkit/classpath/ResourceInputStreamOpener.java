package name.remal.gradleplugins.toolkit.classpath;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.InputStream;

@FunctionalInterface
public interface ResourceInputStreamOpener {

    @MustBeClosed
    InputStream openStream();

}
