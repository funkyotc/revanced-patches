package app.revanced.patches.reddit.misc.tracking.url

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.formatShareLinkMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes("Ljava/lang/String;", "Ljava/util/Map;")
    instructions(method { toString() == $$"Landroid/net/Uri$Builder;->clearQuery()Landroid/net/Uri$Builder;" },)
}
