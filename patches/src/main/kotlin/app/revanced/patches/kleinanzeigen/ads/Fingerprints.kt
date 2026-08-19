package app.revanced.patches.kleinanzeigen.ads

import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.strings

internal val BytecodePatchContext.getLibertyInitMethod by gettingFirstMethod("KEY_LIBERTY_REFRESH_INTERVAL")
