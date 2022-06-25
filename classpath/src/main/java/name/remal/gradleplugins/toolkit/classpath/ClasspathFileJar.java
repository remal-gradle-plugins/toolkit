package name.remal.gradleplugins.toolkit.classpath;

import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradleplugins.toolkit.LazyInitializer;

@SuppressWarnings("java:S2160")
final class ClasspathFileJar extends ClasspathFileBase {

    private static final String MANIFEST_RESOURCE_NAME = "META-INF/MANIFEST.MF";
    private static final Name MULTI_RELEASE = new Name("Multi-Release");
    private static final String MULTI_RELEASE_VERSIONS_ENTRIES_PREFIX = "META-INF/versions/";
    private static final int MULTI_RELEASE_MIN_VERSION = 9;

    ClasspathFileJar(File file, int jvmMajorCompatibilityVersion) {
        super(file, jvmMajorCompatibilityVersion);
    }


    @Override
    @SneakyThrows
    protected Set<String> getResourceNamesImpl() {
        try (val zipFile = new ZipFile(file)) {
            Set<String> resourceNames = new LinkedHashSet<>();
            val entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                val resourceName = entry.getName();
                if (!isMultiRelease()) {
                    resourceNames.add(resourceName);
                    continue;
                }

                val isVersioned = resourceName.startsWith(MULTI_RELEASE_VERSIONS_ENTRIES_PREFIX);
                if (!isVersioned) {
                    resourceNames.add(resourceName);
                    continue;
                }

                val versionEndPos = resourceName.indexOf('/', MULTI_RELEASE_VERSIONS_ENTRIES_PREFIX.length());
                if (versionEndPos < 0) {
                    continue;
                }

                final int version;
                try {
                    val versionString = resourceName.substring(
                        MULTI_RELEASE_VERSIONS_ENTRIES_PREFIX.length(),
                        versionEndPos
                    );
                    version = parseInt(versionString);
                } catch (NumberFormatException ignored) {
                    continue;
                }

                if (version < MULTI_RELEASE_MIN_VERSION || version > jvmMajorCompatibilityVersion) {
                    continue;
                }

                val unversionedResourceName = resourceName.substring(versionEndPos + 1);
                resourceNames.add(unversionedResourceName);
            }
            return resourceNames;
        }
    }


    @Nullable
    @Override
    @SneakyThrows
    protected InputStream openStreamImpl(String resourceName) {
        val zipFile = new ZipFile(file);
        try {
            InputStream inputStream = null;

            if (isMultiRelease()) {
                for (int version = jvmMajorCompatibilityVersion; MULTI_RELEASE_MIN_VERSION <= version; --version) {
                    val versionedResourceName = MULTI_RELEASE_VERSIONS_ENTRIES_PREFIX + version + '/' + resourceName;
                    val entry = zipFile.getEntry(versionedResourceName);
                    if (entry != null) {
                        inputStream = zipFile.getInputStream(entry);
                        break;
                    }
                }
            }

            if (inputStream == null) {
                val entry = zipFile.getEntry(resourceName);
                if (entry != null) {
                    inputStream = zipFile.getInputStream(entry);
                }
            }

            if (inputStream != null) {
                inputStream = new InputStreamCloseWrapper(inputStream) {
                    @Override
                    protected void additionalClose() throws Throwable {
                        zipFile.close();
                    }
                };
            }

            return inputStream;

        } catch (Throwable exception) {
            try {
                zipFile.close();
            } catch (Throwable e) {
                exception.addSuppressed(e);
            }
            throw exception;
        }
    }

    @Override
    @SneakyThrows
    public void forEachResource(ResourceProcessor processor) {
        if (isMultiRelease()) {
            super.forEachResource(processor);
            return;
        }

        try (val fileStream = newInputStream(file.toPath())) {
            try (val zipStream = new ZipInputStream(fileStream, UTF_8)) {
                while (true) {
                    val entry = zipStream.getNextEntry();
                    if (entry == null) {
                        break;
                    } else if (entry.isDirectory()) {
                        continue;
                    }

                    val resourceName = entry.getName();
                    val resourceStream = new InputStreamCloseWrapper(zipStream) {
                        @Override
                        protected boolean shouldCloseDelegate() {
                            return false;
                        }
                    };

                    val inputStreamSupplier = new ResourceInputStreamOpenerImpl(file, resourceName) {
                        @Override
                        protected InputStream openStreamImpl() {
                            return resourceStream;
                        }
                    };
                    try {
                        processor.process(file, resourceName, inputStreamSupplier);

                    } finally {
                        inputStreamSupplier.disable();
                    }
                }
            }
        }
    }


    //#region Utilities

    @SneakyThrows
    private boolean isMultiRelease() {
        return TRUE.equals(isMultiRelease.get());
    }

    private final LazyInitializer<Boolean> isMultiRelease = new LazyInitializer<Boolean>() {
        @Override
        @SneakyThrows
        protected Boolean create() {
            try (val zipFile = new ZipFile(file)) {
                val manifestEntry = zipFile.getEntry(MANIFEST_RESOURCE_NAME);
                if (manifestEntry != null) {
                    try (val manifestStream = zipFile.getInputStream(manifestEntry)) {
                        val manifest = new Manifest(manifestStream);
                        return parseBoolean(manifest.getMainAttributes().getValue(MULTI_RELEASE));
                    }
                }
            }

            return false;
        }
    };

    //#endregion

}
