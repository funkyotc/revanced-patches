package app.revanced.patches.zwanoo.speedtest.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableAdsPatch = bytecodePatch("Disable ads") {
    compatibleWith("org.zwanoo.android.speedtest")

    apply {
        // Prevent the banner ad controller from reporting ads as enabled.
        bannerAdEnabledMethod.returnEarly(false)

        // Prevent the native ad view from being constructed.
        nativeAdCreateMethod.returnEarly()

        // Prevent all native ad sequences (including end-of-test interstitials) from starting.
        nativeAdExecuteMethod.returnEarly()

        // Prevent the shadow banner ads from showing.
        premiumStatusMethod.returnEarly(true)

        // Prevent ads from loading.
        loadAdMethod.returnEarly()
    }
}
