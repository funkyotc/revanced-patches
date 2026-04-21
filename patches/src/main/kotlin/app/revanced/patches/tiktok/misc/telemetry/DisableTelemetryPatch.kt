package app.revanced.patches.tiktok.misc.telemetry

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableTelemetryPatch = bytecodePatch(
    name = "Disable telemetry",
    description = "Disables ByteDance AppLog analytics, AppsFlyer attribution tracking, " +
        "BDLocation background uploads, Firebase Analytics, and crash reporting.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill"("36.5.4"),
        "com.zhiliaoapp.musically"("36.5.4"),
    )

    apply {
        // AppLog — stub all analytics methods so no events are collected or transmitted.
        listOf(
            appLogInitMethod,
            appLogStartMethod,
            appLogOnEventMethod,
            appLogOnEventV3StringMethod,
            appLogOnEventV3JsonMethod,
            appLogOnEventV3BundleMethod,
            appLogOnMiscEventMethod,
            appLogFlushMethod,
            appLogFlushAsyncMethod,
            appLogOnActivityPauseMethod,
        ).forEach { it.returnEarly() }

        // AppsFlyer — prevent SDK initialization and stub logging.
        listOf(
            initAppsFlyerRunMethod,
            appsFlyerLogEventMethod,
            appsFlyerLogLocationMethod,
        ).forEach { it.returnEarly() }

        // BDLocation — force all upload flags to false.
        // Override setUpload() to set all static flags to false, ignoring the parameter.
        bdLocationSetUploadMethod.addInstructions(
            0,
            """
                const/4 p0, 0x0
                sput-boolean p0, Lcom/bytedance/bdlocation/client/BDLocationConfig;->sIsUpload:Z
                sput-boolean p0, Lcom/bytedance/bdlocation/client/BDLocationConfig;->sIsUploadGPS:Z
                sput-boolean p0, Lcom/bytedance/bdlocation/client/BDLocationConfig;->sIsUploadLocation:Z
                sput-boolean p0, Lcom/bytedance/bdlocation/client/BDLocationConfig;->sIsUploadBaseSite:Z
                sput-boolean p0, Lcom/bytedance/bdlocation/client/BDLocationConfig;->sIsUploadWIFI:Z
                sput-boolean p0, Lcom/bytedance/bdlocation/client/BDLocationConfig;->sUploadMccAndSystemRegionInfo:Z
                return-void
            """,
        )

        // Force isUpload*() getters to return false.
        listOf(
            bdLocationIsUploadMethod,
            bdLocationIsUploadGPSMethod,
            bdLocationIsUploadLocationMethod,
        ).forEach { it.returnEarly() }

        // Firebase Analytics — stub screen tracking.
        firebaseSetCurrentScreenMethod.returnEarly()

        // MonitorCrash — prevent crash data transmission.
        monitorCrashReportCustomErrMethod.returnEarly()
        monitorCrashReportEventMethod.returnEarly()
    }
}
