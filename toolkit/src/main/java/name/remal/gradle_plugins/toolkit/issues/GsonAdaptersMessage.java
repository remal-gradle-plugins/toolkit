package name.remal.gradle_plugins.toolkit.issues;

import static com.google.gson.stream.JsonToken.BEGIN_OBJECT;
import static name.remal.gradle_plugins.toolkit.issues.HtmlMessage.htmlMessageOf;
import static name.remal.gradle_plugins.toolkit.issues.TextMessage.textMessageOf;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.jspecify.annotations.Nullable;

public class GsonAdaptersMessage implements TypeAdapterFactory {

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (Message.class.isAssignableFrom(type.getRawType())) {
            return (TypeAdapter<T>) new MessageTypeAdapter(gson);
        }

        return null;
    }


    private static class MessageTypeAdapter extends TypeAdapter<Message> {

        private final TypeAdapter<String> stringTypeAdapter;

        public MessageTypeAdapter(Gson gson) {
            this.stringTypeAdapter = gson.getAdapter(String.class);
        }

        @Override
        public void write(JsonWriter out, @Nullable Message value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            if (value instanceof HtmlMessage) {
                out.beginObject();
                out.name("type").value("html");
                out.name("value").value(value.getValue());
                out.endObject();
                return;
            }

            out.value(value.renderAsText());
        }

        @Override
        @Nullable
        public Message read(JsonReader in) throws IOException {
            if (in.peek() == BEGIN_OBJECT) {
                in.beginObject();
                try {
                    String type = null;
                    String value = null;
                    while (in.hasNext()) {
                        var attr = in.nextName();
                        switch (attr) {
                            case "type":
                                type = stringTypeAdapter.read(in);
                                break;
                            case "value":
                            case "message":
                                value = stringTypeAdapter.read(in);
                                break;
                            default:
                                // do nothing
                        }
                    }
                    if (value == null) {
                        return null;
                    }

                    if ("html".equalsIgnoreCase(type)) {
                        return htmlMessageOf(value);
                    }

                    return textMessageOf(value);

                } finally {
                    in.endObject();
                }
            }

            var text = stringTypeAdapter.read(in);
            return text != null ? textMessageOf(text) : null;
        }

    }

}
