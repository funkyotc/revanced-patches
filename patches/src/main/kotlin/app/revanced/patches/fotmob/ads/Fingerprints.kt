package app.revanced.patches.fotmob.ads

import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.shouldDisplayAdsMethod by gettingFirstMethod("Ads disabled: user is in sanctioned country")