package app.revanced.extension.twitter.utils.json;

import app.revanced.extension.twitter.utils.stream.StreamUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import org.jetbrains.annotations.NotNull;

public final class JsonUtils {
    public static final JsonUtils INSTANCE = new JsonUtils();

    private JsonUtils() {
    }

    /**
     * Parses a JSON object from an input stream.
     *
     * @param jsonInputStream The input stream to parse.
     * @return The parsed JSONObject.
     * @throws IOException If an I/O error occurs.
     * @throws JSONException If the stream content is not a valid JSON.
     */
    @NotNull
    public static JSONObject parseJson(@NotNull InputStream jsonInputStream) throws IOException, JSONException {
        return new JSONObject(StreamUtils.toString(jsonInputStream));
    }
}