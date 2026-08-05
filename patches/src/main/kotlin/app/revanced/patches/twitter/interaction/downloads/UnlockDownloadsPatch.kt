package app.revanced.patches.twitter.interaction.downloads

import app.revanced.patcher.extensions.*
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val unlockDownloadsPatch = bytecodePatch(
    name = "Unlock downloads",
    description = "Unlocks the ability to download any video. GIFs can be downloaded via the menu on long press.",
) {
    compatibleWith(
        "com.twitter.android"(
            "12.8.0-release.0",
            "12.10.0-release.0",
        )
    )

    apply {
        /**
         * Allow downloads for non-premium users.
         * Return early makes the method return true for all users.
         * This method returns a boolean value that indicates whether the user can download the video of subscriptionsFeatures Interface.
         *
         * X has two identical methods, one without "legacy" and one with "legacy".
         * Don't know what legacy actually does, but it's patched anyway.
         */
        val subscriptionsFeaturesDefiningClass = subscriptionsFeaturesMethodMatch.definingClass.substringBefore("$") + ";"
        getCanDownloadVideoMethodMatch(subscriptionsFeaturesDefiningClass, false).method.returnEarly(true)
        getCanDownloadVideoMethodMatch(subscriptionsFeaturesDefiningClass, true).method.returnEarly(true)

        // Some media videos have different download button that directly uses subscriptionsFeatures.
        getMediaGalleryDownloadMethodMatch(subscriptionsFeaturesDefiningClass).let {
            it.method.apply {
                listOf(
                    0 to false, // Don't fall back to offline video.
                    2 to true, // Make user can download video.
                ).forEach { (index, boolean) ->
                    val canUserDownloadVideoIndex = it[index] + 1
                    val canUserDownloadVideoRegister = getInstruction<OneRegisterInstruction>(canUserDownloadVideoIndex).registerA
                    val bit = if (boolean) 1 else 0
                    replaceInstruction(canUserDownloadVideoIndex, "const/4 v$canUserDownloadVideoRegister, 0x$bit")
                }
            }
        }

        // Download action for long-press download button.
        getPostMediaActionMethodMatch(subscriptionsFeaturesDefiningClass).let {
            it.method.apply {
                // Create download button for GIF.
                val isDownloadableIndex = it[8] + 1
                val isDownloadableRegister = getInstruction<OneRegisterInstruction>(isDownloadableIndex).registerA
                replaceInstruction(isDownloadableIndex, "const/4 v$isDownloadableRegister, 0x1")

                // Replace the boolean that blocks non-premium users from download.
                val canUserDownloadVideoIndex = it[-1] + 1
                val canUserDownloadVideoRegister = getInstruction<OneRegisterInstruction>(canUserDownloadVideoIndex).registerA
                replaceInstruction(canUserDownloadVideoIndex, "const/4 v$canUserDownloadVideoRegister, 0x1")
            }
        }
    }
}
