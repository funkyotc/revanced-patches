package app.revanced.patches.viki.ads.videoads

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.shouldLoadVideoAdsMethod by gettingFirstMethodDeclaratively("getShowAdsUseCase")