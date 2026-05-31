package app.revanced.extension.twitter.patches.hook.twifucker;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TwiFucker {
    public static final TwiFucker INSTANCE = new TwiFucker();

    private TwiFucker() {}

    // region root
    @Nullable
    private JSONArray jsonGetInstructions(@NotNull JSONObject json) {
        return Optional.ofNullable(json.optJSONObject("timeline"))
                .map(obj -> obj.optJSONArray("instructions"))
                .orElse(null);
    }

    @Nullable
    private JSONObject jsonGetData(@NotNull JSONObject json) {
        return json.optJSONObject("data");
    }

    private void jsonCheckAndRemoveRecommendedUsers(@NotNull JSONObject json) {
        if (json.has("recommended_users")) {
            Log.d("ReVanced", "Handle recommended users: " + json);
            json.remove("recommended_users");
        }
    }

    private void jsonCheckAndRemoveThreads(@NotNull JSONObject json) {
        if (json.has("threads")) {
            Log.d("ReVanced", "Handle threads: " + json);
            json.remove("threads");
        }
    }
    // endregion

    // region data
    @Nullable
    private JSONArray dataGetInstructions(@NotNull JSONObject json) {
        return Optional.ofNullable(json.optJSONObject("user_result"))
                .map(obj -> obj.optJSONObject("result"))
                .map(obj -> obj.optJSONObject("timeline_response"))
                .map(obj -> obj.optJSONObject("timeline"))
                .or(() -> Optional.ofNullable(json.optJSONObject("timeline_response"))
                        .map(obj -> obj.optJSONObject("timeline")))
                .or(() -> Optional.ofNullable(json.optJSONObject("search"))
                        .map(obj -> obj.optJSONObject("timeline_response"))
                        .map(obj -> obj.optJSONObject("timeline")))
                .or(() -> Optional.ofNullable(json.optJSONObject("timeline_response")))
                .map(obj -> obj.optJSONArray("instructions"))
                .orElse(null);
    }

    private void dataCheckAndRemove(@NotNull JSONObject json) {
        Optional.ofNullable(dataGetInstructions(json))
                .ifPresent(instructions ->
                        TwiFuckerUtils.INSTANCE.forEach(instructions, instruction ->
                                instructionCheckAndRemove(instruction, this::entriesRemoveAnnoyance)
                        )
                );
    }

    @Nullable
    private JSONObject dataGetLegacy(@NotNull JSONObject json) {
        return Optional.ofNullable(json.optJSONObject("tweet_result"))
                .map(obj -> obj.optJSONObject("result"))
                .map(obj -> obj.has("tweet") ? obj.optJSONObject("tweet") : obj)
                .map(obj -> obj.optJSONObject("legacy"))
                .orElse(null);
    }
    // endregion

    // region entry
    private boolean entryHasPromotedMetadata(@NotNull JSONObject json) {
        return Optional.ofNullable(json.optJSONObject("content"))
                .map(obj -> obj.optJSONObject("item"))
                .map(obj -> obj.optJSONObject("content"))
                .map(obj -> obj.optJSONObject("tweet"))
                .map(obj -> obj.has("promotedMetadata"))
                .orElse(false) ||
                Optional.ofNullable(json.optJSONObject("content"))
                        .map(obj -> obj.optJSONObject("content"))
                        .map(obj -> obj.has("tweetPromotedMetadata"))
                        .orElse(false) ||
                Optional.ofNullable(json.optJSONObject("item"))
                        .map(obj -> obj.optJSONObject("content"))
                        .map(obj -> obj.has("tweetPromotedMetadata"))
                        .orElse(false);
    }

    @Nullable
    private JSONArray entryGetContentItems(@NotNull JSONObject json) {
        return Optional.ofNullable(json.optJSONObject("content"))
                .map(obj -> obj.optJSONArray("items"))
                .or(() -> Optional.ofNullable(json.optJSONObject("content"))
                        .map(obj -> obj.optJSONObject("timelineModule"))
                        .map(obj -> obj.optJSONArray("items")))
                .orElse(null);
    }

    private boolean entryIsTweetDetailRelatedTweets(@NotNull JSONObject json) {
        return json.optString("entryId").startsWith("tweetdetailrelatedtweets-");
    }

    @Nullable
    private JSONArray entryGetTrends(@NotNull JSONObject json) {
        return Optional.ofNullable(json.optJSONObject("content"))
                .map(obj -> obj.optJSONObject("timelineModule"))
                .map(obj -> obj.optJSONArray("items"))
                .orElse(null);
    }
    // endregion

    // region trend
    private boolean trendHasPromotedMetadata(@NotNull JSONObject json) {
        return Optional.ofNullable(json.optJSONObject("item"))
                .map(obj -> obj.optJSONObject("content"))
                .map(obj -> obj.optJSONObject("trend"))
                .map(obj -> obj.has("promotedMetadata"))
                .orElse(false);
    }

    private void trendRemoveAds(@NotNull JSONArray jsonArray) {
        List<Integer> trendRemoveIndex = new ArrayList<>();
        TwiFuckerUtils.INSTANCE.forEachIndexed(jsonArray, (trendIndex, trend) -> {
            if (trendHasPromotedMetadata(trend)) {
                Log.d("ReVanced", "Handle trends ads " + trendIndex + " " + trend);
                trendRemoveIndex.add(trendIndex);
            }
        });
        Collections.reverse(trendRemoveIndex);
        trendRemoveIndex.forEach(jsonArray::remove);
    }
    // endregion

    // region instruction
    @Nullable
    private JSONArray instructionTimelineAddEntries(@NotNull JSONObject json) {
        return json.optJSONArray("entries");
    }

    @Nullable
    private JSONArray instructionGetAddEntries(@NotNull JSONObject json) {
        return Optional.ofNullable(json.optJSONObject("addEntries"))
                .map(obj -> obj.optJSONArray("entries"))
                .orElse(null);
    }

    private void instructionCheckAndRemove(@NotNull JSONObject json, Consumer<JSONArray> action) {
        Optional.ofNullable(instructionTimelineAddEntries(json)).ifPresent(action);
        Optional.ofNullable(instructionGetAddEntries(json)).ifPresent(action);
    }
    // endregion

    // region entries
    private void entriesRemoveTimelineAds(@NotNull JSONArray jsonArray) {
        List<Integer> removeIndex = new ArrayList<>();
        TwiFuckerUtils.INSTANCE.forEachIndexed(jsonArray, (entryIndex, entry) -> {
            Optional.ofNullable(entryGetTrends(entry)).ifPresent(this::trendRemoveAds);

            if (entryHasPromotedMetadata(entry)) {
                Log.d("ReVanced", "Handle timeline ads " + entryIndex + " " + entry);
                removeIndex.add(entryIndex);
            }

            Optional.ofNullable(entryGetContentItems(entry)).ifPresent(contentItems -> {
                List<Integer> innerRemoveIndex = new ArrayList<>();
                TwiFuckerUtils.INSTANCE.forEachIndexed(contentItems, (itemIndex, item) -> {
                    if (entryHasPromotedMetadata(item)) {
                        Log.d("ReVanced", "Handle timeline replies ads " + entryIndex + " " + entry);
                        if (contentItems.length() == 1) {
                            removeIndex.add(entryIndex);
                        } else {
                            innerRemoveIndex.add(itemIndex);
                        }
                    }
                });
                Collections.reverse(innerRemoveIndex);
                innerRemoveIndex.forEach(contentItems::remove);
            });
        });
        Collections.reverse(removeIndex);
        removeIndex.forEach(jsonArray::remove);
    }

    private void entriesRemoveTweetDetailRelatedTweets(@NotNull JSONArray jsonArray) {
        List<Integer> removeIndex = new ArrayList<>();
        TwiFuckerUtils.INSTANCE.forEachIndexed(jsonArray, (entryIndex, entry) -> {
            if (entryIsTweetDetailRelatedTweets(entry)) {
                Log.d("ReVanced", "Handle tweet detail related tweets " + entryIndex + " " + entry);
                removeIndex.add(entryIndex);
            }
        });
        Collections.reverse(removeIndex);
        removeIndex.forEach(jsonArray::remove);
    }

    private void entriesRemoveAnnoyance(@NotNull JSONArray jsonArray) {
        entriesRemoveTimelineAds(jsonArray);
        entriesRemoveTweetDetailRelatedTweets(jsonArray);
    }

    private boolean entryIsWhoToFollow(@NotNull JSONObject json) {
        String entryId = json.optString("entryId");
        return entryId.startsWith("whoToFollow-") ||
                entryId.startsWith("who-to-follow-") ||
                entryId.startsWith("connect-module-") ||
                entryId.startsWith("who-to-subscribe-");
    }

    private boolean itemContainsPromotedUser(@NotNull JSONObject json) {
        return Optional.ofNullable(json.optJSONObject("item"))
                .map(obj -> obj.optJSONObject("content"))
                .map(obj -> obj.has("userPromotedMetadata"))
                .orElse(false) ||
                Optional.ofNullable(json.optJSONObject("item"))
                        .map(obj -> obj.optJSONObject("content"))
                        .map(obj -> obj.optJSONObject("user"))
                        .map(obj -> obj.has("userPromotedMetadata") || obj.has("promotedMetadata"))
                        .orElse(false);
    }

    private void entriesRemoveWhoToFollow(@NotNull JSONArray jsonArray) {
        List<Integer> entryRemoveIndex = new ArrayList<>();
        TwiFuckerUtils.INSTANCE.forEachIndexed(jsonArray, (entryIndex, entry) -> {
            if (!entryIsWhoToFollow(entry)) return;

            Log.d("ReVanced", "Handle whoToFollow " + entryIndex + " " + entry);
            entryRemoveIndex.add(entryIndex);

            Optional.ofNullable(entryGetContentItems(entry)).ifPresent(items -> {
                List<Integer> userRemoveIndex = new ArrayList<>();
                TwiFuckerUtils.INSTANCE.forEachIndexed(items, (index, item) -> {
                    if (itemContainsPromotedUser(item)) {
                        Log.d("ReVanced", "Handle whoToFollow promoted user " + index + " " + item);
                        userRemoveIndex.add(index);
                    }
                });
                Collections.reverse(userRemoveIndex);
                userRemoveIndex.forEach(items::remove);
            });
        });
        Collections.reverse(entryRemoveIndex);
        entryRemoveIndex.forEach(jsonArray::remove);
    }
    // endregion

    public void hideRecommendedUsers(@NotNull JSONObject json) {
        filterInstructions(json, this::entriesRemoveWhoToFollow);
        jsonCheckAndRemoveRecommendedUsers(json);
    }

    public void hidePromotedAds(@NotNull JSONObject json) {
        filterInstructions(json, this::entriesRemoveAnnoyance);
        Optional.ofNullable(jsonGetData(json)).ifPresent(this::dataCheckAndRemove);
    }

    private void filterInstructions(@NotNull JSONObject json, Consumer<JSONArray> action) {
        Optional.ofNullable(jsonGetInstructions(json))
                .ifPresent(instructions ->
                        TwiFuckerUtils.INSTANCE.forEach(instructions, instruction ->
                                instructionCheckAndRemove(instruction, action)
                        )
                );
    }
}