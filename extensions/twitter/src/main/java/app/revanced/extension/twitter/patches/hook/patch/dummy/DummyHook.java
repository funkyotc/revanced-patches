package app.revanced.extension.twitter.patches.hook.patch.dummy;

import app.revanced.extension.twitter.patches.hook.json.BaseJsonHook;
import org.json.JSONObject;
import org.jetbrains.annotations.NotNull;

/**
 * Dummy hook to reserve a register in [JsonHookPatch.hooks] list.
 */
public final class DummyHook extends BaseJsonHook {
    public static final DummyHook INSTANCE = new DummyHook();

    private DummyHook() {
    }

    @Override
    public void apply(@NotNull JSONObject json) {
        // Do nothing.
    }
}