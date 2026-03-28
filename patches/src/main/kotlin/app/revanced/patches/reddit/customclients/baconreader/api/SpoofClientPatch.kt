package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.CompositeMatch
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.patches.shared.misc.string.replaceStringPatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

val spoofClientPatch = spoofClientPatch { clientIdOption, redirectUriOption, userAgentOption ->
    dependsOn(
        // Redirects from SSL to WWW domain are bugged causing auth problems.
        // Manually rewrite the URLs to fix this.
        replaceStringPatch("ssl.reddit.com", "www.reddit.com"),
    )

    compatibleWith(
        "com.onelouder.baconreader",
        "com.onelouder.baconreader.premium",
    )

    val clientId by clientIdOption
    val redirectUri by redirectUriOption
    val userAgent by userAgentOption

    apply {
        fun CompositeMatch.patch(replacementString: String) {
            val index = get(0)

            val stringRegister =
                method.getInstruction<OneRegisterInstruction>(index).registerA
            method.replaceInstruction(
                index,
                "const-string v$stringRegister, \"$replacementString\"",
            )
        }

        getAuthorizationUrlMethodMatch.patch("client_id=$clientId")
        requestTokenMethodMatch.patch(clientId!!)

        if (redirectUri != null) {
            val redirectUri = redirectUri!!

            mapOf(
                getAuthorizeUrlMethodMatch to "redirect_uri=$redirectUri",
                runTaskMethodMatch to redirectUri,
                isRedirectUrlMethodMatch to redirectUri,
            ).forEach { (match, newString) ->
                match.patch(newString)
            }
        }

        if (userAgent != null) {
            getRestClientUserAgentMethod.returnEarly(userAgent!!)
            getRedditUserAgentMethod.returnEarly(userAgent!!)
        }
    }
}
