package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.twitter.misc.extension.sharedExtensionPatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.logging.Logger

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/twitter/patches/links/CustomizeSharingLinkPatch;"

internal val domainNameOption = stringOption(
    default = "x.com",
    name = "Domain name",
    description = "The domain name to use when sharing links.",
    values = mapOf(
        "Default" to "x.com",
        "FxTwitter" to "fxtwitter.com",
    ),
    required = true,
) {
    // Do a courtesy check if the host can be resolved.
    // If it does not resolve, then print a warning but use the host anyway.
    // Unresolvable hosts should not be rejected, since the patching environment
    // may not allow network connections or the network may be down.
    try {
        InetAddress.getByName(it)
    } catch (_: UnknownHostException) {
        Logger.getLogger(this::class.java.name).warning(
            "Host \"$it\" did not resolve to any domain.",
        )
    } catch (_: Exception) {
        // Must ignore any kind of exception. Trying to resolve network
        // on Manager throws android.os.NetworkOnMainThreadException
    }

    true
}

@Suppress("unused")
val customizeSharingLinkPatch = bytecodePatch(
    name = "Customize sharing link",
    description = "Changes the domain name used when sharing links.",
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(
        "com.twitter.android"(
            "11.80.0-release.0",
            "12.8.0-release.0",
            "12.10.0-release.0",
        ),
    )

    val returnUsername by booleanOption(
        default = true,
        name = "Return username",
        description = "Whether to return the username in the link.",
    )

    val domainName by domainNameOption()

    apply {
        // Replace the isReturnUsernameEnabled in the link sharing extension methods.
        returnUsernameHelperMethod.returnEarly(returnUsername!!)

        // Replace the domain name in the link sharing extension methods.
        linkSharingDomainHelperMethod.returnEarly(domainName!!)

        // Formats share link such as sharing through XChat.
        linkInternalShareSheetMethodMatch.let {
            it.method.apply {
                val statusStringIndex = it[-2]
                val statusStringRegister = getInstruction<OneRegisterInstruction>(statusStringIndex).registerA

                val contextualPostIndex = it[-1]
                val contextualPostRegister = getInstruction<TwoRegisterInstruction>(contextualPostIndex).registerA

                addInstructions(
                    contextualPostIndex + 1,
                    """
                        invoke-static/range { v$contextualPostRegister .. v$contextualPostRegister }, $EXTENSION_CLASS_DESCRIPTOR->formatInternalShareSheetLink(Ljava/lang/Object;)Ljava/lang/String;
                        move-result-object v$statusStringRegister
                    """
                )
            }
        }

        // Formats share link such as "Copy link" or "Share via..." etc.
        linkExternalShareSheetMethodMatch.let {
            it.method.apply {
                val rootContextualPostIndex = it[0]
                val rootContextualPostRegister = getInstruction<OneRegisterInstruction>(rootContextualPostIndex).registerA

                val statusStringIndex = it[-1]
                val statusStringRegister = getInstruction<OneRegisterInstruction>(statusStringIndex).registerA

                addInstructions(
                    statusStringIndex + 1,
                    """
                        invoke-static/range { v$rootContextualPostRegister .. v$rootContextualPostRegister }, $EXTENSION_CLASS_DESCRIPTOR->formatExternalShareSheetLink(Ljava/lang/Object;)Ljava/lang/String;
                        move-result-object v$statusStringRegister
                    """
                )
            }
        }
    }
}
