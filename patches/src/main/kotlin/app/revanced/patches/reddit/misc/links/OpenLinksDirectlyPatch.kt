package app.revanced.patches.reddit.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.firstMethod
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction


private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/reddit/patches/OpenLinksDirectlyPatch;"

@Suppress("unused")
val openLinksDirectlyPatch = bytecodePatch(
    name = "Open links directly",
    description = "Opens URLs directly without the redirect page."
) {
    compatibleWith("com.reddit.frontpage")

    dependsOn(sharedExtensionPatch)

    apply {
        customReportsMethodMatch.let {
            firstMethod(
                it.method.getInstruction<ReferenceInstruction>(it[2]).methodReference!!
            ).addInstructions(
                0,
                """
                    invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->parseRedirectUri(Landroid/net/Uri;)Landroid/net/Uri;
                    move-result-object p2
                """
            )
        }
    }
}
