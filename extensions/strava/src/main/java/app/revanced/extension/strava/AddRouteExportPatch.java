package app.revanced.extension.strava;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import app.revanced.extension.shared.Utils;

public final class AddRouteExportPatch {

    public static void showExportDialog(Activity activity) {
        try {
            Intent intent = activity.getIntent();
            if (intent == null) return;
            
            Bundle extras = intent.getExtras();
            if (extras == null) return;
            
            String routeId = null;
            
            // Scan all Intent extras to find the Route ID.
            for (String key : extras.keySet()) {
                Object val = extras.get(key);
                if (val != null) {
                    String strVal = val.toString();
                    
                    // Check if we have a "SavedRoute" or "SuggestedRoute" object.
                    if (val.getClass().getName().contains("SavedRoute")) {
                        try {
                            for (java.lang.reflect.Field field : val.getClass().getDeclaredFields()) {
                                field.setAccessible(true);
                                if (field.getType() == long.class) {
                                    long id = field.getLong(val);
                                    if (id > 0) {
                                        routeId = String.valueOf(id);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {}
                    }
                    
                    if (routeId != null) break;
                    
                    // Fallback: regex search in the text content.
                    Matcher m = Pattern.compile("routes/([0-9]+)").matcher(strVal);
                    if (m.find()) {
                        routeId = m.group(1);
                        break;
                    }
                }
            }
            
            if (routeId != null) {
                final String finalRouteId = routeId;
                final String finalUrl = "https://www.strava.com/routes/" + finalRouteId;
                
                // Wait 500ms to allow Strava's BottomSheet to open,
                // then display the popup on top of it.
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (activity.isDestroyed() || activity.isFinishing()) return;
                    
                    new AlertDialog.Builder(activity)
                        .setTitle("Share options")
                        .setMessage("Do you want to export this route or use the share menu?")
                        .setPositiveButton("Export GPX", (dialog, which) -> {
                            Utils.openLink(finalUrl + "/export_gpx");
                            activity.finish();
                        })
                        .setNeutralButton("Export TCX", (dialog, which) -> {
                            Utils.openLink(finalUrl + "/export_tcx");
                            activity.finish();
                        })
                        .setNegativeButton("Share", (dialog, which) -> {
                            // Close the popup, Strava's share menu is already displayed underneath.
                        })
                        .show();
                }, 500);
            }
        } catch (Exception e) {
            // Silently ignore errors to avoid crashes on other types of shares.
        }
    }
}
