package app.revanced.extension.twitter.patches.hook.json;

import app.revanced.extension.twitter.patches.hook.patch.Hook;
import org.json.JSONObject;
import org.jetbrains.annotations.NotNull;

public interface JsonHook extends Hook<JSONObject> {
    /**
     * Transform a JSONObject.
     *
     * @param json The JSONObject.
     * @return The transformed JSONObject.
     */
    @NotNull
    JSONObject transform(@NotNull JSONObject json);

    @Override
    @NotNull
    default JSONObject hook(@NotNull JSONObject type) {
        return transform(type);
    }
}