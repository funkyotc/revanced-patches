package app.revanced.patches.meta.ads

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.adInjectorMethod by gettingFirstMethodDeclaratively("Is ad pod")
