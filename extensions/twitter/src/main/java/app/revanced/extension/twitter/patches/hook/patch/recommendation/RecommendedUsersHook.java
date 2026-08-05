package app.revanced.extension.twitter.patches.hook.patch.recommendation;

import app.revanced.extension.twitter.patches.hook.json.BaseJsonHook;
import app.revanced.extension.twitter.patches.hook.json.JsonParser;
import org.json.JSONObject;
import org.jetbrains.annotations.NotNull;

public final class RecommendedUsersHook extends BaseJsonHook {
    public static final RecommendedUsersHook INSTANCE = new RecommendedUsersHook();

    private RecommendedUsersHook() {
    }

    /**
     * Strips JSONObject from recommended users.
     *
     * @param json The JSONObject.
     */
    @Override
    public void apply(@NotNull JSONObject json) {
        JsonParser.INSTANCE.hideRecommendedUsers(json);
    }
}