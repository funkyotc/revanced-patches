package app.revanced.patches.instagram.misc.removeBuildExpiredPopup

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val removeBuildExpiredPopupPatch = bytecodePatch(
    name = "Remove build expired popup",
    description = "Removes the popup that appears after a while, when the app version ages.",
) {
    compatibleWith("com.instagram.android")

    apply {
        // Newer builds route the stale-build warning through a dedicated
        // lockout presenter method, so skipping that presenter cleanly
        // suppresses the popup.
        appUpdateLockoutPresenterMethod.returnEarly()
    }
}
