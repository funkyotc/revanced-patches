package app.revanced.patches.pixiv.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch("Hide ads") {
    compatibleWith(
        "jp.pxv.android"(
            "6.188.0",
            "6.191.1"
        ),
    )

    apply {
        shouldShowAdsMethod.returnEarly(false)
    }
}