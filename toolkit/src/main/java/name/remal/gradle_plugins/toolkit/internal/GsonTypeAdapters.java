package name.remal.gradle_plugins.toolkit.internal;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.gson.stream.JsonToken.NULL;
import static java.util.function.Function.identity;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import groovy.lang.GString;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.codehaus.groovy.runtime.GStringImpl;
import org.jspecify.annotations.Nullable;

public class GsonTypeAdapters implements TypeAdapterFactory {

    private static final Map<Class<?>, GenericTypeAdapter<?>> TYPE_ADAPTERS = Stream.of(
        new StringJsonValueTypeAdapter<>(
            GString.class,
            GString::toString,
            value -> new GStringImpl(new Object[0], new String[]{value})
        ),
        new StringJsonValueTypeAdapter<>(Charset.class, Charset::name, Charset::forName),
        new StringJsonValueTypeAdapter<>(
            File.class,
            value -> normalizeFile(value).getPath(),
            File::new
        ),
        new StringJsonValueTypeAdapter<>(
            Path.class,
            value -> normalizePath(value).toString(),
            Paths::get
        ),
        new StringJsonValueTypeAdapter<>(ZoneId.class, ZoneId::toString, ZoneId::of),
        new StringJsonValueTypeAdapter<>(ZoneOffset.class, ZoneOffset::toString, ZoneOffset::of),
        new StringJsonValueTypeAdapter<>(LocalDate.class, LocalDate::toString, LocalDate::parse),
        new StringJsonValueTypeAdapter<>(LocalTime.class, LocalTime::toString, LocalTime::parse),
        new StringJsonValueTypeAdapter<>(LocalDateTime.class, LocalDateTime::toString, LocalDateTime::parse),
        new StringJsonValueTypeAdapter<>(OffsetTime.class, OffsetTime::toString, OffsetTime::parse),
        new StringJsonValueTypeAdapter<>(Duration.class, Duration::toString, Duration::parse),
        new StringJsonValueTypeAdapter<>(Period.class, Period::toString, Period::parse),
        new StringJsonValueTypeAdapter<>(ZonedDateTime.class, ZonedDateTime::toString, ZonedDateTime::parse),
        new StringJsonValueTypeAdapter<>(
            OffsetDateTime.class,
            OffsetDateTime::toString,
            value -> {
                Throwable previousException;
                try {
                    return ZonedDateTime.parse(value).toOffsetDateTime();
                } catch (DateTimeParseException e) {
                    previousException = e;
                }
                try {
                    return OffsetDateTime.parse(value);
                } catch (DateTimeParseException e) {
                    e.addSuppressed(previousException);
                    throw e;
                }
            }
        ),
        new StringJsonValueTypeAdapter<>(
            Instant.class,
            Instant::toString,
            value -> {
                Throwable previousException;
                try {
                    return OffsetDateTime.parse(value).toInstant();
                } catch (DateTimeParseException e) {
                    previousException = e;
                }
                try {
                    return ZonedDateTime.parse(value).toInstant();
                } catch (DateTimeParseException e) {
                    e.addSuppressed(previousException);
                    previousException = e;
                }
                try {
                    return Instant.parse(value);
                } catch (DateTimeParseException e) {
                    e.addSuppressed(previousException);
                    throw e;
                }
            }
        )
    ).collect(toImmutableMap(GenericTypeAdapter::getType, identity()));


    @RequiredArgsConstructor
    private static class OptionalTypeAdapter<E> extends TypeAdapter<Optional<E>> {

        private final TypeAdapter<@Nullable E> delegate;

        @Override
        @SuppressWarnings({"java:S2789", "java:S2259", "OptionalAssignedToNull", "NullableOptional"})
        public void write(JsonWriter out, @Nullable Optional<E> value) throws IOException {
            if (value == null || value.isEmpty()) {
                out.nullValue();
                return;
            }

            delegate.write(out, value.get());
        }

        @Override
        public Optional<E> read(JsonReader in) throws IOException {
            if (in.peek() == NULL) {
                in.nextNull();
                return Optional.empty();
            }

            E value = delegate.read(in);
            return Optional.ofNullable(value);
        }

    }


    @Override
    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        var rawType = type.getRawType();

        for (var entry : TYPE_ADAPTERS.entrySet()) {
            if (entry.getKey().isAssignableFrom(rawType)) {
                return (TypeAdapter) entry.getValue();
            }
        }

        if (Optional.class.isAssignableFrom(rawType)) {
            final Type delegateType;
            var reflectionType = type.getType();
            if (reflectionType instanceof ParameterizedType) {
                delegateType = ((ParameterizedType) reflectionType).getActualTypeArguments()[0];
            } else {
                delegateType = Object.class;
            }
            var delegate = gson.getAdapter(TypeToken.get(delegateType));
            return (TypeAdapter) new OptionalTypeAdapter<>(delegate);
        }

        return null;
    }


    private static class StringJsonValueTypeAdapter<T> extends GenericTypeAdapter<T> {
        public StringJsonValueTypeAdapter(
            Class<T> type,
            JsonStringValueGetter<T> jsonValueGetter,
            JsonStringParser<T> jsonParser
        ) {
            super(
                type,
                jsonValueGetter::toJsonValue,
                in -> jsonParser.parse(in.nextString())
            );
        }
    }

    private interface JsonStringValueGetter<T> {
        @Nullable
        String toJsonValue(T object) throws Exception;
    }

    private interface JsonStringParser<T> {
        T parse(String value) throws Exception;
    }


    @RequiredArgsConstructor
    private static class GenericTypeAdapter<T> extends TypeAdapter<T> {

        @Getter
        private final Class<T> type;

        private final JsonValueGetter<T> jsonValueGetter;
        private final JsonParser<T> jsonParser;


        @Override
        public final void write(JsonWriter out, @Nullable T value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            try {
                Object jsonValue = jsonValueGetter.toJsonValue(value);
                if (jsonValue == null) {
                    out.nullValue();
                } else if (jsonValue instanceof String) {
                    out.value((String) jsonValue);
                } else if (jsonValue instanceof Boolean) {
                    out.value((Boolean) jsonValue);
                } else if (jsonValue instanceof Number) {
                    out.value((Number) jsonValue);
                } else {
                    throw new UnsupportedOperationException("Unsupported JSON value type: " + jsonValue.getClass());
                }
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        @Nullable
        public final T read(JsonReader in) throws IOException {
            if (in.peek() == NULL) {
                in.nextNull();
                return null;
            }

            try {
                return jsonParser.parse(in);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    private interface JsonValueGetter<T> {
        @Nullable
        Object toJsonValue(T object) throws Exception;
    }

    private interface JsonParser<T> {
        T parse(JsonReader in) throws Exception;
    }

}
