package app.revanced.extension.twitter.patches.hook.json;

import android.util.Log;
import app.revanced.extension.twitter.patches.hook.patch.dummy.DummyHook;
import app.revanced.extension.twitter.utils.json.JsonUtils;
import app.revanced.extension.twitter.utils.stream.StreamUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class JsonHookPatch {
    public static final JsonHookPatch INSTANCE = new JsonHookPatch();

    private static final List<JsonHook> hooks;

    static {
        hooks = new ArrayList<>();
        hooks.add(DummyHook.INSTANCE);
    }

    private JsonHookPatch() {
    }

    @NotNull
    public static InputStream parseJsonHook(@NotNull InputStream jsonInputStream) {
        JSONObject jsonObject;
        try {
            jsonObject = JsonUtils.parseJson(jsonInputStream);
        } catch (IOException | JSONException ignored) {
            return jsonInputStream;
        }

        for (JsonHook hook : hooks) {
            jsonObject = hook.hook(jsonObject);
        }

        return StreamUtils.INSTANCE.fromString(jsonObject.toString());
    }
}