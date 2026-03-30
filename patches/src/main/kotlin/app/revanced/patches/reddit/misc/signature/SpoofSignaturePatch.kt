package app.revanced.patches.reddit.misc.signature

import app.revanced.patcher.classDef
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.misc.extension.sharedExtensionPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/reddit/patches/SpoofSignaturePatch;"

@Suppress("unused")
val spoofSignaturePatch = bytecodePatch(
    name = "Spoof signature",
    description = "Spoofs the signature of the app to fix issues with receiving notifications."
) {
    compatibleWith("com.reddit.frontpage")

    dependsOn(sharedExtensionPatch)

    apply {
        applicationAttachBaseContextMethod.classDef.setSuperClass(EXTENSION_CLASS_DESCRIPTOR)
    }
}
