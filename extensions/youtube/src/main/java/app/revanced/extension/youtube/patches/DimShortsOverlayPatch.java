package app.revanced.extension.youtube.patches;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsetsController;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;
import app.revanced.extension.youtube.shared.ShortsPlayerState;

@SuppressWarnings("unused")
public class DimShortsOverlayPatch {

    private static int reelTimeBarId = 0;

    /**
     * Injection point.
     */
    public static void dimShortsPlayerOverlay(View shortsOverlayView) {
        shortsOverlayView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            private ViewTreeObserver registeredObserver;
            private ViewTreeObserver.OnPreDrawListener preDrawListener;
            private Window window;
            private boolean wasImmersive = false;
            private boolean reelTimeBarListenerAdded = false;

            @Override
            public void onViewAttachedToWindow(View v) {
                // reel_watch_player is the video surface; the buttons overlay sits in its parent.
                ViewParent parent = v.getParent();
                final View target = (parent instanceof View) ? (View) parent : v;

                Logger.printDebug(() -> "DimShorts overlay: target=" + target.getClass().getName());

                window = getWindow(target);

                if (reelTimeBarId == 0) {
                    reelTimeBarId = v.getContext().getResources()
                            .getIdentifier("reel_time_bar", "id", v.getContext().getPackageName());
                }

                preDrawListener = () -> {
                    int opacity = Settings.SHORTS_OVERLAY_OPACITY.get();
                    if (opacity < 0 || opacity > 100) opacity = 100;
                    final float alpha = opacity / 100.0f;
                    if (target.getAlpha() != alpha) {
                        target.setAlpha(alpha);
                    }

                    // reel_time_bar is added dynamically to the window; poll until found then
                    // attach a dedicated dim listener so it dims with the rest of the overlay.
                    if (!reelTimeBarListenerAdded && window != null && reelTimeBarId != 0) {
                        View rtb = window.getDecorView().findViewById(reelTimeBarId);
                        if (rtb != null) {
                            addShortsAwareDimListener(rtb);
                            reelTimeBarListenerAdded = true;
                        }
                    }

                    // Only call hide/show when the desired state changes to avoid per-frame calls.
                    boolean shouldBeImmersive = Settings.SHORTS_IMMERSIVE_MODE.get();
                    if (window != null && shouldBeImmersive != wasImmersive) {
                        wasImmersive = shouldBeImmersive;
                        setStatusBarHidden(window, shouldBeImmersive);
                    }

                    return true;
                };
                registeredObserver = target.getViewTreeObserver();
                registeredObserver.addOnPreDrawListener(preDrawListener);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (preDrawListener != null && registeredObserver != null
                        && registeredObserver.isAlive()) {
                    registeredObserver.removeOnPreDrawListener(preDrawListener);
                }
                if (window != null) {
                    setStatusBarHidden(window, false);
                    wasImmersive = false;
                }
                reelTimeBarListenerAdded = false;
                preDrawListener = null;
                registeredObserver = null;
                window = null;
            }
        });
    }

    private static Window getWindow(View view) {
        Context ctx = view.getContext();
        while (ctx instanceof ContextWrapper) {
            if (ctx instanceof Activity) {
                return ((Activity) ctx).getWindow();
            }
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        return null;
    }

    private static void setStatusBarHidden(Window window, boolean hide) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller == null) return;
            if (hide) {
                controller.hide(android.view.WindowInsets.Type.statusBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            } else {
                controller.show(android.view.WindowInsets.Type.statusBars());
            }
        } else {
            View decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            if (hide) {
                flags |= View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else {
                flags &= ~(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            decorView.setSystemUiVisibility(flags);
        }
    }

    /**
     * Injection point.
     */
    public static void dimShortsToolbarButton(String enumString, View buttonView) {
        Logger.printDebug(() -> "DimShorts toolbar: enum=" + enumString);
        addShortsAwareDimListener(buttonView);
    }

    /**
     * Injection point.
     */
    public static void navigationTabCreated(NavigationButton button, View tabView) {
        addShortsAwareDimListener(tabView);
    }

    private static void addShortsAwareDimListener(View view) {
        View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
            private ViewTreeObserver registeredObserver;
            private ViewTreeObserver.OnPreDrawListener preDrawListener;

            @Override
            public void onViewAttachedToWindow(View v) {
                if (preDrawListener != null) return;
                preDrawListener = () -> {
                    final float targetAlpha;
                    if (ShortsPlayerState.isOpen()) {
                        int opacity = Settings.SHORTS_OVERLAY_OPACITY.get();
                        if (opacity < 0 || opacity > 100) opacity = 100;
                        targetAlpha = opacity / 100.0f;
                    } else {
                        targetAlpha = 1.0f;
                    }
                    if (v.getAlpha() != targetAlpha) {
                        v.setAlpha(targetAlpha);
                    }
                    return true;
                };
                registeredObserver = v.getViewTreeObserver();
                registeredObserver.addOnPreDrawListener(preDrawListener);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (preDrawListener != null && registeredObserver != null
                        && registeredObserver.isAlive()) {
                    registeredObserver.removeOnPreDrawListener(preDrawListener);
                }
                preDrawListener = null;
                registeredObserver = null;
            }
        };

        view.addOnAttachStateChangeListener(listener);
        // Nav bar tabs are already attached when this hook fires; manually trigger registration.
        if (view.isAttachedToWindow()) {
            listener.onViewAttachedToWindow(view);
        }
    }
}
