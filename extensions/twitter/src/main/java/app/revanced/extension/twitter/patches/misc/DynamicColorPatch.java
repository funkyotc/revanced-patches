package app.revanced.extension.twitter.patches.misc;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import app.revanced.extension.shared.Utils;

public final class DynamicColorPatch {
    private static final String TAG = "ReVanced";

    public static long getDynamicColor(long fallbackArgb) {
        // Dynamic color is only supported on Android 12+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return fallbackArgb;
        }

        try {
            Context context = Utils.getContext();
            if (context == null) {
                return fallbackArgb;
            }

            int resourceId = context.getResources().getIdentifier("twitter_blue", "color", context.getPackageName());
            if (resourceId == 0) {
                return fallbackArgb;
            }

            int colorInt = context.getColor(resourceId);
            return ((long) colorInt) & 0xFFFFFFFFL;

        } catch (Exception e) {
            Log.e(TAG, "Failed to resolve dynamic color via Utils context", e);
            return fallbackArgb;
        }
    }
}