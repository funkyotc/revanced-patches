package app.revanced.patches.reddit.customclients.joeyforreddit.api

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.patches.reddit.customclients.sync.detection.piracy.disablePiracyDetectionPatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

val spoofClientPatch = spoofClientPatch { clientIdOption, redirectUriOption, userAgentOption ->
    dependsOn(disablePiracyDetectionPatch)

    compatibleWith(
        "o.o.joey",
        "o.o.joey.pro",
        "o.o.joey.dev",
    )

    val clientId by clientIdOption
    val redirectUri by redirectUriOption
    val userAgent by userAgentOption

    apply {
        getClientIdMethod.returnEarly(clientId!!)

        if (redirectUri != null) {
            val redirectUri = redirectUri!!

            oauthHelperConstructorMethodMatch.let {
                it.method.apply {
                    val index = it[0]
                    val stringRegister =
                        getInstruction<FiveRegisterInstruction>(index).registerC

                    addInstructions(
                        index,
                        "const-string v$stringRegister, \"$redirectUri\""
                    )
                }

            }

            // Required, to override a hardcoded check for what appears to be the client id.
            oauthContainsCodeMethodMatch.let {
                it.method.apply {
                    val index = it[0]
                    val register = getInstruction<OneRegisterInstruction>(index).registerA

                    replaceInstruction(index, "const/4 v$register, 0x1")
                }
            }
        }

        if (userAgent != null) authUtilityUserAgentMethod.returnEarly(userAgent!!)
    }
}
