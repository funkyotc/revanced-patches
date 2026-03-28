package app.revanced.patches.reddit.customclients.slide.api

import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.returnEarly
import java.util.logging.Logger

val spoofClientPatch = spoofClientPatch { clientIdOption, redirectUriOption, userAgentOption ->
    compatibleWith("me.ccrama.redditslide")

    val clientId by clientIdOption
    val redirectUri by redirectUriOption
    val userAgent by userAgentOption

    apply {
        getClientIdMethod.returnEarly(clientId!!)

        if (redirectUri != null || userAgent != null) {
            Logger.getLogger(this::class.java.name).warning(
                "Patching redirect URI and user agent is not supported for Slide. " +
                        "Only the client ID will be patched."
            )
        }
    }
}
