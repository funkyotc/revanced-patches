package app.revanced.extension.twitter.patches.hook.json;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JsonParser {
    public static final JsonParser INSTANCE = new JsonParser();

    private JsonParser() {}

    public void hidePromotedMetadata(@NotNull JSONObject json) {
        handleTimeline(json, entry -> removeMatchingItems(entry, this::matchesPromotedEntryId));
    }

    public void hideRecommendedUsers(@NotNull JSONObject json) {
        handleTimeline(json, entry -> removeMatchingItems(entry, this::matchesWhoToFollowEntryId));
    }

    private void handleTimeline(@NotNull JSONObject json, @NotNull Predicate<JSONObject> removePredicate) {
        JSONArray instructions = findJSONArray(json, "instructions");
        if (instructions == null) return;

        forEach(instructions, instruction -> {
            JSONArray entries = instruction.optJSONArray("entries");
            if (entries == null) return;

            List<Integer> entryRemoveIndex = new ArrayList<>();
            forEachIndexed(entries, (entryIndex, entry) -> {
                if (removePredicate.test(entry)) {
                    Log.d("ReVanced", "Handle Timeline " + entryIndex + " " + entry);
                    entryRemoveIndex.add(entryIndex);
                }
            });
            Collections.reverse(entryRemoveIndex);
            entryRemoveIndex.forEach(entries::remove);
        });
    }

    private boolean removeMatchingItems(@NotNull JSONObject entry, @NotNull Predicate<JSONObject> matcher) {
        if (matcher.test(entry)) {
            return true;
        }

        JSONArray items = findJSONArray(entry, "items");
        if (items == null) {
            return false;
        }

        List<Integer> itemRemoveIndex = new ArrayList<>();
        forEachIndexed(items, (itemIndex, item) -> {
            if (matcher.test(item)) {
                itemRemoveIndex.add(itemIndex);
            }
        });
        Collections.reverse(itemRemoveIndex);
        itemRemoveIndex.forEach(items::remove);

        return false;
    }

    private boolean matchesPromotedEntryId(@NotNull JSONObject json) {
        String entryId = json.optString("entryId", json.optString("entry_id"));
        return entryId.contains("promoted-tweet");
    }

    private boolean matchesWhoToFollowEntryId(@NotNull JSONObject json) {
        String entryId = json.optString("entryId", json.optString("entry_id"));
        return entryId.startsWith("whoToFollow-")
                || entryId.startsWith("who-to-follow-")
                || entryId.startsWith("connect-module-")
                || entryId.startsWith("who-to-subscribe-");
    }

    /**
     * Searches for a JSONArray with the given key in the given JSONObject.
     * @param json
     * @param targetKey
     * @return
     */
    @Nullable
    private JSONArray findJSONArray(@Nullable JSONObject json, String targetKey) {
        if (json == null) return null;

        JSONArray array = json.optJSONArray(targetKey);
        if (array != null) return array;

        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            Object child = json.opt(keys.next());
            if (child instanceof JSONObject) {
                JSONArray result = findJSONArray((JSONObject) child, targetKey);
                if (result != null) return result;
            }
        }

        return null;
    }

    /**
     * Iterates over a JSONArray and performs the given action for each JSONObject element.
     *
     * @param jsonArray The JSONArray to iterate.
     * @param action The action to be performed for each element.
     */
    private void forEach(@NotNull JSONArray jsonArray, @NotNull Consumer<JSONObject> action) {
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
    private void forEachIndexed(@NotNull JSONArray jsonArray, @NotNull BiConsumer<Integer, JSONObject> action) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.opt(i);
            if (element instanceof JSONObject) {
                action.accept(i, (JSONObject) element);
            }
        }
    }
}
