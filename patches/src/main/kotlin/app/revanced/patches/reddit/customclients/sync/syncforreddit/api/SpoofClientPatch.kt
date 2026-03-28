package app.revanced.patches.reddit.customclients.sync.syncforreddit.api

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.extensions.stringReference
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.patches.reddit.customclients.sync.detection.piracy.disablePiracyDetectionPatch
import app.revanced.patches.shared.misc.string.replaceStringPatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import java.util.Base64

@Suppress("unused")
val spoofClientPatch = spoofClientPatch { clientIdOption, redirectUriOption, userAgentOption ->
    dependsOn(
        disablePiracyDetectionPatch,
        // Redirects from SSL to WWW domain are bugged causing auth problems.
        // Manually rewrite the URLs to fix this.
        replaceStringPatch("ssl.reddit.com", "www.reddit.com"),
    )

    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    val clientId by clientIdOption
    val redirectUri by redirectUriOption
    val userAgent by userAgentOption

    apply {
        getAuthorizationStringMethodMatch.immutableClassDef.getBearerTokenMethodMatch.method.apply {
            val auth = Base64.getEncoder().encodeToString("$clientId:".toByteArray(Charsets.UTF_8))
            returnEarly("Basic $auth")

            val occurrenceIndex = getAuthorizationStringMethodMatch[0]

            getAuthorizationStringMethodMatch.method.apply {
                val authorizationStringInstruction =
                    getInstruction<OneRegisterInstruction>(occurrenceIndex)
                val targetRegister = authorizationStringInstruction.registerA

                val newAuthorizationUrl =
                    authorizationStringInstruction.stringReference!!.string.replace(
                        "client_id=.*?&".toRegex(),
                        "client_id=$clientId&",
                    )

                replaceInstruction(
                    occurrenceIndex,
                    "const-string v$targetRegister, \"$newAuthorizationUrl\"",
                )
            }
        }

        if (redirectUri != null) getRedirectUriMethod.returnEarly(redirectUri!!)

        if (userAgent != null) getUserAgentMethod.returnEarly(userAgent!!)

        imgurImageAPIMethodMatch.let {
            val apiUrlIndex = it[0]

            it.method.replaceInstruction(
                apiUrlIndex,
                "const-string v1, \"https://api.imgur.com/3/image\"",
            )
        }

    }
}
