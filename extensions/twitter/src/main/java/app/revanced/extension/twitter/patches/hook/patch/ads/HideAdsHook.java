package app.revanced.extension.twitter.patches.hook.patch.ads;

import app.revanced.extension.twitter.patches.hook.json.BaseJsonHook;
import app.revanced.extension.twitter.patches.hook.json.JsonParser;
import org.json.JSONObject;
import org.jetbrains.annotations.NotNull;

/**
 * Strips JSONObject from promoted ads.
 */
public final class HideAdsHook extends BaseJsonHook {
    public static final HideAdsHook INSTANCE = new HideAdsHook();

    private HideAdsHook() {
    }

    @Override
    public void apply(@NotNull JSONObject json) {
        JsonParser.INSTANCE.hidePromotedMetadata(json);
    }
}