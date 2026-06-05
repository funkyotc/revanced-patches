package app.revanced.patches.angulus.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.pairip.license.disablePairIPLicenseCheckPatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch("Hide ads") {
    compatibleWith("com.drinkplusplus.angulus")

    dependsOn(disablePairIPLicenseCheckPatch)

    apply {
        // Always return 0 as the daily measurement count.
        getDailyMeasurementCountMethod.returnEarly(0)
    }
}
