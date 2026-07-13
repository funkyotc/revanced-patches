package app.revanced.patches.zwanoo.speedtest.misc

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideUpgradePromptsPatch = bytecodePatch("Hide upgrade prompts") {
    compatibleWith("org.zwanoo.android.speedtest")

    apply {
        showUpgradeDialogMethod.returnEarly()

        subscriptionExpiryMethod.method.returnEarly()
    }
}
