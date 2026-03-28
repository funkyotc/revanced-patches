package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.CompositeMatch
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val spoofClientPatch = spoofClientPatch { clientIdOption, redirectUriOption, userAgentOption ->
    compatibleWith("com.rubenmayayo.reddit")

    val clientId by clientIdOption
    val redirectUri by redirectUriOption
    val userAgent by userAgentOption

    apply {
        getClientIdMethod.returnEarly(clientId!!)

        fun CompositeMatch.patch(replacementString: String) {
            val index = get(0)

            val stringRegister = method.getInstruction<OneRegisterInstruction>(index).registerA
            method.replaceInstruction(
                index,
                "const-string v$stringRegister, \"$replacementString\"",
            )
        }

        if (redirectUri != null) {
            listOf(
                loginActivityOnCreateMethodMatch,
                loginActivityAShouldOverrideUrlLoadingMethodMatch
            ).forEach { match ->
                match.patch(redirectUri!!)
            }
        }

        if (userAgent != null) buildUserAgentMethodMatch.patch(userAgent!!)
    }
}
