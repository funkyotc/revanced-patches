package app.revanced.extension.reddit.patches;

import android.net.Uri;

import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public class OpenLinksDirectlyPatch {
    /**
     * Injection point.
     * <p>
     * Parses the given Reddit redirect uri by extracting the redirect query URL.
     *
     * @param uri The Reddit redirect URI.
     * @return The redirect query URI.
     */
    public static Uri parseRedirectUri(Uri uri) {
        final String url = uri.getQueryParameter("url");
        if (Utils.isNotEmpty(url)) return Uri.parse(url);

        return uri;
    }
}
