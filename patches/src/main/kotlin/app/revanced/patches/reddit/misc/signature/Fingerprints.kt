package app.revanced.patches.reddit.misc.signature

import app.revanced.patcher.custom
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.applicationAttachBaseContextMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    name("attachBaseContext")
    parameterTypes("Landroid/content/Context;")
    custom { immutableClassDef.superclass == "Landroid/app/Application;" }
}
