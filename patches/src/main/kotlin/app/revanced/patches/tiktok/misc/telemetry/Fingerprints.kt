package app.revanced.patches.tiktok.misc.telemetry

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

// region ByteDance AppLog — primary telemetry pipeline.
// Class: com.bytedance.applog.AppLog
// All methods are public static void, delegating to a singleton.

internal val BytecodePatchContext.appLogInitMethod by gettingFirstMethodDeclaratively {
    name("init")
    definingClass("/AppLog;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes("Landroid/content/Context;")
}

internal val BytecodePatchContext.appLogStartMethod by gettingFirstMethodDeclaratively {
    name("start")
    definingClass("/AppLog;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes()
}

internal val BytecodePatchContext.appLogOnEventMethod by gettingFirstMethodDeclaratively {
    name("onEvent")
    definingClass("/AppLog;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes("Ljava/lang/String;")
}

internal val BytecodePatchContext.appLogOnEventV3StringMethod by gettingFirstMethodDeclaratively {
    name("onEventV3")
    definingClass("/AppLog;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes("Ljava/lang/String;")
}

internal val BytecodePatchContext.appLogOnEventV3JsonMethod by gettingFirstMethodDeclaratively {
    name("onEventV3")
    definingClass("/AppLog;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes("Ljava/lang/String;", "Lorg/json/JSONObject;")
}

internal val BytecodePatchContext.appLogOnEventV3BundleMethod by gettingFirstMethodDeclaratively {
    name("onEventV3")
    definingClass("/AppLog;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes("Ljava/lang/String;", "Landroid/os/Bundle;")
}

internal val BytecodePatchContext.appLogOnMiscEventMethod by gettingFirstMethodDeclaratively {
    name("onMiscEvent")
    definingClass("/AppLog;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes("Ljava/lang/String;", "Lorg/json/JSONObject;")
}

internal val BytecodePatchContext.appLogFlushMethod by gettingFirstMethodDeclaratively {
    name("flush")
    definingClass("/AppLog;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes()
}

internal val BytecodePatchContext.appLogFlushAsyncMethod by gettingFirstMethodDeclaratively {
    name("flushAsync")
    definingClass("/AppLog;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes()
}

internal val BytecodePatchContext.appLogOnActivityPauseMethod by gettingFirstMethodDeclaratively {
    name("onActivityPause")
    definingClass("/AppLog;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes()
}

// endregion

// region AppsFlyer — third-party attribution SDK.
// Collects IMEI, OAID, Android ID, GPS location, ad impressions.

internal val BytecodePatchContext.initAppsFlyerRunMethod by gettingFirstMethodDeclaratively(
    "XY8Lpakui8g4kBcposRgxA", // Hardcoded AppsFlyer SDK token.
) {
    returnType("V")
    parameterTypes("Landroid/content/Context;")
}

internal val BytecodePatchContext.appsFlyerLogEventMethod by gettingFirstMethodDeclaratively {
    name("logEvent")
    definingClass("/AppsFlyerLib;")
}

internal val BytecodePatchContext.appsFlyerLogLocationMethod by gettingFirstMethodDeclaratively {
    name("logLocation")
    definingClass("/AppsFlyerLib;")
}

// endregion

// region BDLocation — ByteDance's location SDK.
// Uploads GPS, WiFi beacons, and cell tower data every 5 minutes.

internal val BytecodePatchContext.bdLocationSetUploadMethod by gettingFirstMethodDeclaratively {
    name("setUpload")
    definingClass("/BDLocationConfig;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
    parameterTypes("Z")
}

internal val BytecodePatchContext.bdLocationIsUploadMethod by gettingFirstMethodDeclaratively {
    name("isUpload")
    definingClass("/BDLocationConfig;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes()
}

internal val BytecodePatchContext.bdLocationIsUploadGPSMethod by gettingFirstMethodDeclaratively {
    name("isUploadGPS")
    definingClass("/BDLocationConfig;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes()
}

internal val BytecodePatchContext.bdLocationIsUploadLocationMethod by gettingFirstMethodDeclaratively {
    name("isUploadLocation")
    definingClass("/BDLocationConfig;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes()
}

// endregion

// region Firebase Analytics — Google screen tracking.

internal val BytecodePatchContext.firebaseSetCurrentScreenMethod by gettingFirstMethodDeclaratively {
    name("setCurrentScreen")
    definingClass("/FirebaseAnalytics;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/app/Activity;", "Ljava/lang/String;", "Ljava/lang/String;")
}

// endregion

// region MonitorCrash — ByteDance crash reporting.
// Sends crash data with device ID, user ID, and app version.

internal val BytecodePatchContext.monitorCrashReportCustomErrMethod by gettingFirstMethodDeclaratively {
    name("reportCustomErr")
    definingClass("/MonitorCrash;")
    accessFlags(AccessFlags.PUBLIC)
    returnType("V")
    parameterTypes("Ljava/lang/String;", "Ljava/lang/String;", "Ljava/lang/Throwable;")
}

internal val BytecodePatchContext.monitorCrashReportEventMethod by gettingFirstMethodDeclaratively {
    name("reportEvent")
    definingClass("/MonitorCrash;")
    accessFlags(AccessFlags.PUBLIC)
    returnType("V")
    parameterTypes(
        "Ljava/lang/String;",
        "I",
        "Lorg/json/JSONObject;",
        "Lorg/json/JSONObject;",
        "Lorg/json/JSONObject;",
    )
}

// endregion
