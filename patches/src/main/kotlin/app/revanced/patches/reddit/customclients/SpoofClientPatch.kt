package app.revanced.patches.reddit.customclients

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.Option
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption

/**
 * Base class for patches that spoof the Reddit client.
 *
 * @param block The patch block. It is called with the client ID, redirect URI, and user agent options.
 * The client ID option is required, while the redirect URI and user agent options are optional.
 */
fun spoofClientPatch(
    block: BytecodePatchBuilder.(
        clientIdOption: Option<String>,
        redirectUriOption: Option<String>,
        userAgentOption: Option<String>
    ) -> Unit,
) = bytecodePatch(
    name = "Spoof client",
    description = "Restores functionality of the app by using custom client ID.",
) {
    block(
        stringOption(
            name = "Application client ID",
            default = "yH0aTnJEt6qUgGn835B4vg",
            values = mapOf(
                "RedReader" to "yH0aTnJEt6qUgGn835B4vg",
            ),
            description = "The Reddit OAuth application client ID. " +
                    "You can get a client ID from https://www.reddit.com/prefs/apps. " +
                    "The application type has to be \"Installed app\".",
            required = true,
        ),
        stringOption(
            "Application redirect URI",
            default = "redreader://rr_oauth_redir",
            values = mapOf(
                "RedReader" to "redreader://rr_oauth_redir",
            ),
            description = "The Reddit OAuth application redirect URI " +
                    "matching the Reddit application with the specified client ID.",
            required = false,
        ),
        stringOption(
            "Application user agent",
            default = "org.quantumbadger.redreader/1.25.1",
            values = mapOf(
                "RedReader" to "org.quantumbadger.redreader/1.25.1",
            ),
            description = "The Reddit OAuth application user agent, " +
                    "ideally matching the Reddit application with the specified client ID.",
            required = false
        )
    )
}
