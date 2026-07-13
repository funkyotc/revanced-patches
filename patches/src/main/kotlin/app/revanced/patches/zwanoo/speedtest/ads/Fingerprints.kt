package app.revanced.patches.zwanoo.speedtest.ads

import app.revanced.patcher.accessFlags
import app.revanced.patcher.allOf
import app.revanced.patcher.anyStaticField
import app.revanced.patcher.custom
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patcher.type
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal val BytecodePatchContext.bannerAdEnabledMethod by gettingFirstMethodDeclaratively {
    definingClass("Lcom/ookla/speedtest/bannerad/")
    returnType("Z")
    accessFlags(AccessFlags.PUBLIC)
    custom {
        immutableClassDef.anyStaticField {
            (initialValue as? StringEncodedValue)?.value == "BannerAd"
        }
    }
}

internal val BytecodePatchContext.nativeAdCreateMethod by gettingFirstMethodDeclaratively {
    definingClass("Lcom/ookla/speedtest/nativead/google/")
    returnType("V")
    accessFlags(AccessFlags.PROTECTED)
    instructions(allOf(Opcode.NEW_INSTANCE(), type("Lcom/google/android/gms/ads/AdManagerAdView;")))
}

internal val BytecodePatchContext.nativeAdExecuteMethod by gettingFirstMethodDeclaratively("Request not idle") {
    definingClass("Lcom/ookla/speedtest/nativead/")
    name("execute")
    returnType("V")
    accessFlags(AccessFlags.PUBLIC)
}

internal val BytecodePatchContext.premiumStatusMethod by gettingFirstMethodDeclaratively("VERIFIED") {
    definingClass("Lcom/ookla/speedtest/purchase/")
    returnType("Z")
    accessFlags(AccessFlags.PUBLIC)
}

internal val BytecodePatchContext.loadAdMethod by gettingFirstMethodDeclaratively {
    definingClass("Lcom/google/android/gms/ads/BaseAdView;")
    name("loadAd")
    returnType("V")
    parameterTypes("Lcom/google/android/gms/ads/AdRequest;")
    accessFlags(AccessFlags.PUBLIC)
}