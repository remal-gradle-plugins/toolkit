package name.remal.gradle_plugins.toolkit.issues;

import static name.remal.gradle_plugins.toolkit.JavaSerializationUtils.deserializeFrom;
import static name.remal.gradle_plugins.toolkit.JavaSerializationUtils.serializeToBytes;
import static name.remal.gradle_plugins.toolkit.issues.HtmlMessage.htmlMessageOf;
import static name.remal.gradle_plugins.toolkit.issues.TextMessage.textMessageOf;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.util.ServiceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MessageTest {

    @Nested
    class Serialization {

        @Test
        void textMessage() {
            var message = textMessageOf("text");
            var bytes = serializeToBytes(message);
            var deserialized = deserializeFrom(bytes, Message.class);
            assertThat(deserialized)
                .isEqualTo(message);
        }

        @Test
        void htmlMessage() {
            var message = htmlMessageOf("html");
            var bytes = serializeToBytes(message);
            var deserialized = deserializeFrom(bytes, Message.class);
            assertThat(deserialized)
                .isEqualTo(message);
        }

    }

    @Nested
    class Json {

        Gson gson;

        @BeforeEach
        void beforeEach() {
            var gsonBuilder = new GsonBuilder()
                .setPrettyPrinting();
            ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
            gson = gsonBuilder.create();
        }

        @Test
        void textMessage() {
            var message = textMessageOf("text");
            var json = gson.toJson(message);
            var deserialized = gson.fromJson(json, Message.class);
            assertThat(deserialized)
                .isEqualTo(message);
        }

        @Test
        void htmlMessage() {
            var message = htmlMessageOf("html");
            var json = gson.toJson(message);
            var deserialized = gson.fromJson(json, Message.class);
            assertThat(deserialized)
                .isEqualTo(message);
        }

    }

}
