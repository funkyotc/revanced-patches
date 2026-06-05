package app.revanced.patches.viki.ads.videoads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideVideoAdsPatch = bytecodePatch(
    name = "Hide video ads"
) {
    compatibleWith("com.viki.android")

    apply {
        shouldLoadVideoAdsMethod.returnEarly()
    }
}
