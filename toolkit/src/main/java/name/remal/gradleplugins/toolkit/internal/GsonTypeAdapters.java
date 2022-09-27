package name.remal.gradleplugins.toolkit.internal;

import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import groovy.lang.GString;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nullable;
import lombok.val;
import org.codehaus.groovy.runtime.GStringImpl;

public class GsonTypeAdapters implements TypeAdapterFactory {

    private static final TypeAdapter<GString> G_STRING_TYPE_ADAPTER = new TypeAdapter<GString>() {
        @Override
        public void write(JsonWriter out, GString value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public GString read(JsonReader in) throws IOException {
            val value = in.nextString();
            return new GStringImpl(new Object[0], new String[]{value});
        }
    }.nullSafe();

    private static final TypeAdapter<Charset> CHARSET_TYPE_ADAPTER = new TypeAdapter<Charset>() {
        @Override
        public void write(JsonWriter out, Charset value) throws IOException {
            out.value(value.name());
        }

        @Override
        public Charset read(JsonReader in) throws IOException {
            return Charset.forName(in.nextString());
        }
    }.nullSafe();

    private static final TypeAdapter<File> FILE_TYPE_ADAPTER = new TypeAdapter<File>() {
        @Override
        public void write(JsonWriter out, File value) throws IOException {
            out.value(normalizePath(value.toPath()).toString());
        }

        @Override
        public File read(JsonReader in) throws IOException {
            return normalizePath(Paths.get(in.nextString())).toFile();
        }
    }.nullSafe();

    private static final TypeAdapter<Path> PATH_TYPE_ADAPTER = new TypeAdapter<Path>() {
        @Override
        public void write(JsonWriter out, Path value) throws IOException {
            out.value(normalizePath(value).toString());
        }

        @Override
        public Path read(JsonReader in) throws IOException {
            return normalizePath(Paths.get(in.nextString()));
        }
    }.nullSafe();

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        val rawType = type.getRawType();
        if (GString.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) G_STRING_TYPE_ADAPTER;
        } else if (rawType == Charset.class) {
            return (TypeAdapter<T>) CHARSET_TYPE_ADAPTER;
        } else if (rawType == File.class) {
            return (TypeAdapter<T>) FILE_TYPE_ADAPTER;
        } else if (rawType == Path.class) {
            return (TypeAdapter<T>) PATH_TYPE_ADAPTER;
        }
        return null;
    }

}
