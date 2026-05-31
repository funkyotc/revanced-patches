package app.revanced.extension.twitter.patches.hook.json;

import org.json.JSONObject;
import org.jetbrains.annotations.NotNull;

public abstract class BaseJsonHook implements JsonHook {
    /**
     * Abstract method to be implemented by subclasses to modify the JSONObject.
     *
     * @param json The JSONObject to modify.
     */
    public abstract void apply(@NotNull JSONObject json);

    @Override
    @NotNull
    public JSONObject transform(@NotNull JSONObject json) {
        apply(json);
        return json;
    }
}