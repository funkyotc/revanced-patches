package app.revanced.patches.reddit.layout.disablescreenshotpopup

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val disableScreenshotPopupPatch = bytecodePatch(
    name = "Disable screenshot popup",
    description = "Disables the popup that shows up when taking a screenshot.",
) {
    compatibleWith("com.reddit.frontpage")

    apply {
        listOf(
            redditScreenshotTriggerSharingListenerMethodMatch,
            screenshotTakenBannerMethodMatch
        ).forEach { match ->
            match.let {
                it.method.apply {
                    val booleanIndex = it[1]
                    val booleanRegister =
                        getInstruction<OneRegisterInstruction>(booleanIndex).registerA

                    addInstructions(booleanIndex + 1, "const/4 v0, 0x0")
                }
            }
        }
    }
}
