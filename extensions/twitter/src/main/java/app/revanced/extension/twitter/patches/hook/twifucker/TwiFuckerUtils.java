package app.revanced.extension.twitter.patches.hook.twifucker;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TwiFuckerUtils {
    public static final TwiFuckerUtils INSTANCE = new TwiFuckerUtils();

    private TwiFuckerUtils() {
    }

    /**
     * Iterates over a JSONArray and performs the given action for each JSONObject element.
     *
     * @param jsonArray The JSONArray to iterate.
     * @param action The action to be performed for each element.
     */
    public void forEach(@NotNull JSONArray jsonArray, @NotNull Consumer<JSONObject> action) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.opt(i);
            if (element instanceof JSONObject) {
                action.accept((JSONObject) element);
            }
        }
    }

    /**
     * Iterates over a JSONArray and performs the given action for each JSONObject element,
     * providing the index of the element.
     *
     * @param jsonArray The JSONArray to iterate.
     * @param action The action to be performed for each element with its index.
     */
    public void forEachIndexed(@NotNull JSONArray jsonArray, @NotNull BiConsumer<Integer, JSONObject> action) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.opt(i);
            if (element instanceof JSONObject) {
                action.accept(i, (JSONObject) element);
            }
        }
    }
}